package com.github.moriakira.jibundashboard.controller

import com.github.moriakira.jibundashboard.component.CurrentAuth
import com.github.moriakira.jibundashboard.generated.model.VocabularyBase
import com.github.moriakira.jibundashboard.generated.model.VocabularyTag
import com.github.moriakira.jibundashboard.generated.model.VocabularyTagBase
import com.github.moriakira.jibundashboard.service.VocabularyModel
import com.github.moriakira.jibundashboard.service.VocabularyService
import com.github.moriakira.jibundashboard.service.VocabularyTagModel
import com.github.moriakira.jibundashboard.service.VocabularyTagService
import io.kotest.assertions.throwables.shouldThrow
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

        fun vocabModel(
            id: String = "11111111-1111-1111-1111-111111111111",
            userId: String = "u1",
            tags: List<VocabularyTagModel> = listOf(
                VocabularyTagModel("22222222-2222-2222-2222-222222222222", "u1", "kotlin"),
            ),
        ) = VocabularyModel(
            vocabularyId = id,
            userId = userId,
            name = "Kotlin Coroutine",
            description = "Async programming",
            tags = tags,
            createdDateTime = "2025-01-01T00:00:00Z",
            updatedDateTime = "2025-01-01T00:00:00Z",
        )

        fun tagModel(
            id: String = "22222222-2222-2222-2222-222222222222",
            userId: String = "u1",
        ) = VocabularyTagModel(
            vocabularyTagId = id,
            userId = userId,
            vocabularyTag = "kotlin",
        )

        // --- Vocabulary ---

        "getVocabularies: 条件指定で一覧を返す" {
            every {
                vocabularyService.listByConditions("u1", "Kotlin", listOf("kotlin"))
            } returns listOf(vocabModel())

            val res = controller.getVocabularies(vocabularyName = "Kotlin", tags = listOf("kotlin"))

            res.statusCode shouldBe HttpStatus.OK
            res.body!!.shouldHaveSize(1)
            res.body!![0].name shouldBe "Kotlin Coroutine"
            verify(exactly = 1) { vocabularyService.listByConditions("u1", "Kotlin", listOf("kotlin")) }
        }

        "postVocabularies: 新規作成で 201 & create 呼び出し" {
            every { vocabularyService.create(any()) } returns "11111111-1111-1111-1111-111111111111"

            val req = VocabularyBase(
                name = "Kotlin Coroutine",
                description = null,
                tags = null,
            )

            val res = controller.postVocabularies(req)

            res.statusCode shouldBe HttpStatus.CREATED
            res.body!!.vocabularyId.toString() shouldBe "11111111-1111-1111-1111-111111111111"
            verify(exactly = 1) { vocabularyService.create(any()) }
            verify(exactly = 0) { vocabularyService.put(any()) }
        }

        "postVocabularies: body が null なら IllegalArgumentException" {
            shouldThrow<IllegalArgumentException> { controller.postVocabularies(null) }
            verify(exactly = 0) { vocabularyService.create(any()) }
        }

        "getVocabulariesById: 所有者なら 200" {
            val id = "eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee"
            every { vocabularyService.getByVocabularyIdForUser(id, "u1") } returns vocabModel(id = id)

            val res = controller.getVocabulariesById(UUID.fromString(id))

            res.statusCode shouldBe HttpStatus.OK
            res.body!!.vocabularyId.toString() shouldBe id
        }

        "getVocabulariesById: 見つからなければ 404" {
            val id = "ffffffff-ffff-ffff-ffff-ffffffffffff"
            every { vocabularyService.getByVocabularyIdForUser(id, "u1") } returns null

            val res = controller.getVocabulariesById(UUID.fromString(id))

            res.statusCode shouldBe HttpStatus.NOT_FOUND
        }

        "putVocabulariesById: 更新で 200 (所有者OK)" {
            val id = "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"
            every { vocabularyService.getByVocabularyIdForUser(id, "u1") } returns vocabModel(id = id)
            every { vocabularyService.put(any()) } returns id

            val req = VocabularyBase(name = "Updated", description = null, tags = null)

            val res = controller.putVocabulariesById(UUID.fromString(id), req)

            res.statusCode shouldBe HttpStatus.OK
            res.body!!.vocabularyId.toString() shouldBe id
            verify(exactly = 1) { vocabularyService.put(any()) }
        }

        "putVocabulariesById: 更新対象が無ければ 404、put は呼ばれない" {
            val id = "cccccccc-cccc-cccc-cccc-cccccccccccc"
            every { vocabularyService.getByVocabularyIdForUser(id, "u1") } returns null

            val req = VocabularyBase(name = "Updated", description = null, tags = null)

            val res = controller.putVocabulariesById(UUID.fromString(id), req)

            res.statusCode shouldBe HttpStatus.NOT_FOUND
            verify(exactly = 0) { vocabularyService.put(any()) }
        }

        "putVocabulariesById: body が null なら IllegalArgumentException" {
            val id = UUID.fromString("aaaaaaaa-1111-2222-3333-bbbbbbbbbbbb")
            shouldThrow<IllegalArgumentException> { controller.putVocabulariesById(id, null) }
            verify(exactly = 0) { vocabularyService.put(any()) }
        }

        "deleteVocabulariesById: 所有者なら削除して 204" {
            val id = "14141414-1414-1414-1414-141414141414"
            every { vocabularyService.getByVocabularyIdForUser(id, "u1") } returns vocabModel(id = id)

            val res = controller.deleteVocabulariesById(UUID.fromString(id))

            res.statusCode shouldBe HttpStatus.NO_CONTENT
            verify(exactly = 1) { vocabularyService.delete("u1", id) }
        }

        "deleteVocabulariesById: 見つからなければ 404" {
            val id = "12121212-1212-1212-1212-121212121212"
            every { vocabularyService.getByVocabularyIdForUser(id, "u1") } returns null

            val res = controller.deleteVocabulariesById(UUID.fromString(id))

            res.statusCode shouldBe HttpStatus.NOT_FOUND
            verify(exactly = 0) { vocabularyService.delete(any(), any()) }
        }

        // --- VocabularyTag ---

        "getVocabularyTags: 条件指定で一覧を返す" {
            every { vocabularyTagService.listByConditions("u1", "kotlin") } returns listOf(tagModel())

            val res = controller.getVocabularyTags(vocabularyTag = "kotlin")

            res.statusCode shouldBe HttpStatus.OK
            res.body!!.shouldHaveSize(1)
            res.body!![0].vocabularyTag shouldBe "kotlin"
            verify(exactly = 1) { vocabularyTagService.listByConditions("u1", "kotlin") }
        }

        "postVocabularyTags: 新規作成で 201 & create 呼び出し" {
            every { vocabularyTagService.create(any()) } returns "22222222-2222-2222-2222-222222222222"

            val req = VocabularyTagBase(vocabularyTag = "kotlin")

            val res = controller.postVocabularyTags(req)

            res.statusCode shouldBe HttpStatus.CREATED
            res.body!!.vocabularyTagId.toString() shouldBe "22222222-2222-2222-2222-222222222222"
            verify(exactly = 1) { vocabularyTagService.create(any()) }
            verify(exactly = 0) { vocabularyTagService.put(any()) }
        }

        "postVocabularyTags: body が null なら IllegalArgumentException" {
            shouldThrow<IllegalArgumentException> { controller.postVocabularyTags(null) }
            verify(exactly = 0) { vocabularyTagService.create(any()) }
        }

        "getVocabularyTagsById: 所有者なら 200" {
            val id = "33333333-3333-3333-3333-333333333333"
            every { vocabularyTagService.getByVocabularyTagIdForUser(id, "u1") } returns tagModel(id = id)

            val res = controller.getVocabularyTagsById(UUID.fromString(id))

            res.statusCode shouldBe HttpStatus.OK
            res.body!!.vocabularyTagId.toString() shouldBe id
        }

        "getVocabularyTagsById: 見つからなければ 404" {
            val id = "44444444-4444-4444-4444-444444444444"
            every { vocabularyTagService.getByVocabularyTagIdForUser(id, "u1") } returns null

            val res = controller.getVocabularyTagsById(UUID.fromString(id))

            res.statusCode shouldBe HttpStatus.NOT_FOUND
        }

        "putVocabularyTagsById: 更新で 200 (所有者OK)" {
            val id = "55555555-5555-5555-5555-555555555555"
            every { vocabularyTagService.getByVocabularyTagIdForUser(id, "u1") } returns tagModel(id = id)
            every { vocabularyTagService.put(any()) } returns id

            val req = VocabularyTagBase(vocabularyTag = "scala")

            val res = controller.putVocabularyTagsById(UUID.fromString(id), req)

            res.statusCode shouldBe HttpStatus.OK
            res.body!!.vocabularyTagId.toString() shouldBe id
            verify(exactly = 1) { vocabularyTagService.put(any()) }
        }

        "putVocabularyTagsById: 更新対象が無ければ 404、put は呼ばれない" {
            val id = "66666666-6666-6666-6666-666666666666"
            every { vocabularyTagService.getByVocabularyTagIdForUser(id, "u1") } returns null

            val req = VocabularyTagBase(vocabularyTag = "scala")

            val res = controller.putVocabularyTagsById(UUID.fromString(id), req)

            res.statusCode shouldBe HttpStatus.NOT_FOUND
            verify(exactly = 0) { vocabularyTagService.put(any()) }
        }

        "putVocabularyTagsById: body が null なら IllegalArgumentException" {
            val id = UUID.fromString("77777777-7777-7777-7777-777777777777")
            shouldThrow<IllegalArgumentException> { controller.putVocabularyTagsById(id, null) }
            verify(exactly = 0) { vocabularyTagService.put(any()) }
        }

        "deleteVocabularyTagsById: 所有者なら削除して 204" {
            val id = "88888888-8888-8888-8888-888888888888"
            every { vocabularyTagService.getByVocabularyTagIdForUser(id, "u1") } returns tagModel(id = id)

            val res = controller.deleteVocabularyTagsById(UUID.fromString(id))

            res.statusCode shouldBe HttpStatus.NO_CONTENT
            verify(exactly = 1) { vocabularyTagService.delete("u1", id) }
        }

        "deleteVocabularyTagsById: 見つからなければ 404" {
            val id = "99999999-9999-9999-9999-999999999999"
            every { vocabularyTagService.getByVocabularyTagIdForUser(id, "u1") } returns null

            val res = controller.deleteVocabularyTagsById(UUID.fromString(id))

            res.statusCode shouldBe HttpStatus.NOT_FOUND
            verify(exactly = 0) { vocabularyTagService.delete(any(), any()) }
        }

        "getVocabularies: タグ embedded の変換を確認する" {
            val tagInModel = VocabularyTagModel("22222222-2222-2222-2222-222222222222", "u1", "kotlin")
            every { vocabularyService.listByConditions("u1", null, null) } returns
                listOf(vocabModel(tags = listOf(tagInModel)))

            val res = controller.getVocabularies(null, null)

            res.statusCode shouldBe HttpStatus.OK
            val vocab = res.body!![0]
            vocab.tags!!.shouldHaveSize(1)
            vocab.tags!!.first().vocabularyTag shouldBe "kotlin"
            vocab.tags!!.first().vocabularyTagId shouldBe UUID.fromString("22222222-2222-2222-2222-222222222222")
        }

        "postVocabularies: tags 付きリクエストで正しく変換する" {
            every { vocabularyService.create(any()) } returns "11111111-1111-1111-1111-111111111111"

            val req = VocabularyBase(
                name = "Kotlin Coroutine",
                description = "Async",
                tags = setOf(
                    VocabularyTag(
                        vocabularyTagId = UUID.fromString("22222222-2222-2222-2222-222222222222"),
                        vocabularyTag = "kotlin",
                    ),
                ),
            )

            val res = controller.postVocabularies(req)

            res.statusCode shouldBe HttpStatus.CREATED
            verify(exactly = 1) { vocabularyService.create(any()) }
        }
    })
