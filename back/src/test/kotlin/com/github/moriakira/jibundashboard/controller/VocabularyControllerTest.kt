package com.github.moriakira.jibundashboard.controller

import com.github.moriakira.jibundashboard.component.CurrentAuth
import com.github.moriakira.jibundashboard.generated.model.VocabularyRequest
import com.github.moriakira.jibundashboard.generated.model.VocabularyTagBase
import com.github.moriakira.jibundashboard.service.VocabularyModel
import com.github.moriakira.jibundashboard.service.VocabularyService
import com.github.moriakira.jibundashboard.service.VocabularyTagModel
import com.github.moriakira.jibundashboard.service.VocabularyTagService
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.http.HttpStatus
import java.util.UUID

class VocabularyControllerTest :
    StringSpec({

        val currentAuth = mockk<CurrentAuth>(relaxed = true)
        val vocabularyService = mockk<VocabularyService>(relaxed = true)
        val vocabularyTagService = mockk<VocabularyTagService>(relaxed = true)
        val controller = VocabularyController(currentAuth, vocabularyService, vocabularyTagService)

        beforeTest {
            every { currentAuth.userId } returns "u1"
            clearMocks(vocabularyService, answers = false, recordedCalls = true, childMocks = true)
            clearMocks(vocabularyTagService, answers = false, recordedCalls = true, childMocks = true)
        }

        fun vocabModel(id: String = "11111111-1111-1111-1111-111111111111") =
            VocabularyModel(
                vocabularyId = id,
                userId = "u1",
                name = "Kotlin Coroutine",
                description = "Async programming",
                tags = listOf(VocabularyTagModel("22222222-2222-2222-2222-222222222222", "u1", "kotlin", order = 1)),
                createdDateTime = "2025-01-01T00:00:00Z",
                updatedDateTime = "2025-01-01T00:00:00Z",
            )

        fun tagModel(id: String = "22222222-2222-2222-2222-222222222222") =
            VocabularyTagModel(vocabularyTagId = id, userId = "u1", vocabularyTag = "kotlin", order = 1)

        // --- Vocabulary ---

        "getVocabularies: 条件指定で一覧を返す" {
            val tagId = UUID.fromString("22222222-2222-2222-2222-222222222222")
            every {
                vocabularyService.listByConditions("u1", "Kotlin", null, listOf(tagId.toString()))
            } returns listOf(vocabModel())

            val res = controller.getVocabularies(vocabularyName = "Kotlin", description = null, tagIds = listOf(tagId))

            res.statusCode shouldBe HttpStatus.OK
            res.body!!.shouldHaveSize(1)
            res.body!![0].name shouldBe "Kotlin Coroutine"
        }

        "postVocabularies: 新規作成で 201" {
            every { vocabularyService.create(any()) } returns "11111111-1111-1111-1111-111111111111"

            val res = controller.postVocabularies(VocabularyRequest(name = "Kotlin Coroutine"))

            res.statusCode shouldBe HttpStatus.CREATED
            res.body!!.vocabularyId.toString() shouldBe "11111111-1111-1111-1111-111111111111"
        }

        "getVocabulariesById: 200 または 404" {
            val id = "eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee"
            val missingId = "ffffffff-ffff-ffff-ffff-ffffffffffff"
            every { vocabularyService.getByVocabularyIdForUser(id, "u1") } returns vocabModel(id = id)
            every { vocabularyService.getByVocabularyIdForUser(missingId, "u1") } returns null

            controller.getVocabulariesById(UUID.fromString(id)).statusCode shouldBe HttpStatus.OK
            controller.getVocabulariesById(UUID.fromString(missingId)).statusCode shouldBe HttpStatus.NOT_FOUND
        }

        "putVocabulariesById: 更新で 200 (所有者OK)" {
            val id = "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"
            every { vocabularyService.getByVocabularyIdForUser(id, "u1") } returns vocabModel(id = id)

            val res = controller.putVocabulariesById(UUID.fromString(id), VocabularyRequest(name = "Updated"))

            res.statusCode shouldBe HttpStatus.OK
            res.body!!.vocabularyId.toString() shouldBe id
            verify(exactly = 1) { vocabularyService.put(any()) }
        }

        "putVocabulariesById: 更新対象が無ければ 404、put は呼ばれない" {
            every { vocabularyService.getByVocabularyIdForUser(any(), any()) } returns null

            val res = controller.putVocabulariesById(
                UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc"),
                VocabularyRequest(name = "Updated"),
            )

            res.statusCode shouldBe HttpStatus.NOT_FOUND
            verify(exactly = 0) { vocabularyService.put(any()) }
        }

        "deleteVocabulariesById: 204 または 404" {
            val id = "14141414-1414-1414-1414-141414141414"
            val missingId = "12121212-1212-1212-1212-121212121212"
            every { vocabularyService.getByVocabularyIdForUser(id, "u1") } returns vocabModel(id = id)
            every { vocabularyService.getByVocabularyIdForUser(missingId, "u1") } returns null

            controller.deleteVocabulariesById(UUID.fromString(id)).statusCode shouldBe HttpStatus.NO_CONTENT
            controller.deleteVocabulariesById(UUID.fromString(missingId)).statusCode shouldBe HttpStatus.NOT_FOUND
        }

        // --- VocabularyTag ---

        "getVocabularyTags: 条件指定で一覧を返す" {
            every { vocabularyTagService.listByConditions("u1", "kotlin") } returns listOf(tagModel())

            val res = controller.getVocabularyTags(vocabularyTag = "kotlin")

            res.statusCode shouldBe HttpStatus.OK
            res.body!!.shouldHaveSize(1)
            res.body!![0].vocabularyTag shouldBe "kotlin"
        }

        "postVocabularyTags: 新規作成で 201" {
            every { vocabularyTagService.create(any()) } returns "22222222-2222-2222-2222-222222222222"

            val res = controller.postVocabularyTags(VocabularyTagBase(vocabularyTag = "kotlin", order = 1))

            res.statusCode shouldBe HttpStatus.CREATED
            res.body!!.vocabularyTagId.toString() shouldBe "22222222-2222-2222-2222-222222222222"
        }

        "getVocabularyTagsById: 所有者なら 200、見つからなければ 404" {
            val id = "33333333-3333-3333-3333-333333333333"
            val missingId = "44444444-4444-4444-4444-444444444444"
            every { vocabularyTagService.getByVocabularyTagIdForUser(id, "u1") } returns tagModel(id = id)
            every { vocabularyTagService.getByVocabularyTagIdForUser(missingId, "u1") } returns null

            controller.getVocabularyTagsById(UUID.fromString(id)).statusCode shouldBe HttpStatus.OK
            controller.getVocabularyTagsById(UUID.fromString(missingId)).statusCode shouldBe HttpStatus.NOT_FOUND
        }

        "putVocabularyTagsById: 更新で 200、更新対象が無ければ 404" {
            val id = "55555555-5555-5555-5555-555555555555"
            val missingId = "66666666-6666-6666-6666-666666666666"
            every { vocabularyTagService.getByVocabularyTagIdForUser(id, "u1") } returns tagModel(id = id)
            every { vocabularyTagService.getByVocabularyTagIdForUser(missingId, "u1") } returns null
            val req = VocabularyTagBase(vocabularyTag = "scala", order = 2)

            controller.putVocabularyTagsById(UUID.fromString(id), req).statusCode shouldBe HttpStatus.OK
            controller.putVocabularyTagsById(UUID.fromString(missingId), req).statusCode shouldBe HttpStatus.NOT_FOUND
        }

        "deleteVocabularyTagsById: 所有者なら 204、見つからなければ 404" {
            val id = "88888888-8888-8888-8888-888888888888"
            val missingId = "99999999-9999-9999-9999-999999999999"
            every { vocabularyTagService.getByVocabularyTagIdForUser(id, "u1") } returns tagModel(id = id)
            every { vocabularyTagService.getByVocabularyTagIdForUser(missingId, "u1") } returns null

            controller.deleteVocabularyTagsById(UUID.fromString(id)).statusCode shouldBe HttpStatus.NO_CONTENT
            controller.deleteVocabularyTagsById(UUID.fromString(missingId)).statusCode shouldBe HttpStatus.NOT_FOUND
        }
    })
