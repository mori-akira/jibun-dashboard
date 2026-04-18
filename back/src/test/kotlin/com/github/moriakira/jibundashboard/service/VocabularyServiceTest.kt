package com.github.moriakira.jibundashboard.service

import com.github.moriakira.jibundashboard.repository.VocabularyItem
import com.github.moriakira.jibundashboard.repository.VocabularyRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import io.mockk.verify
import java.time.OffsetDateTime
import java.util.UUID

class VocabularyServiceTest :
    StringSpec({

        val repository = mockk<VocabularyRepository>(relaxed = true)
        val vocabularyTagService = mockk<VocabularyTagService>(relaxed = true)
        val service = VocabularyService(repository, vocabularyTagService)

        val fixedNow = OffsetDateTime.parse("2025-06-01T12:00:00Z")

        beforeTest {
            clearMocks(repository, answers = false, recordedCalls = true, childMocks = true)
            clearMocks(vocabularyTagService, answers = false, recordedCalls = true, childMocks = true)
        }

        fun item(id: String = "v1", userId: String = "u1") =
            VocabularyItem().apply {
                vocabularyId = id
                this.userId = userId
                name = "Kotlin"
                description = "A language"
                tagIds = listOf("tag1")
                createdDateTime = "2025-01-01T00:00:00Z"
                updatedDateTime = "2025-01-01T00:00:00Z"
            }

        fun model(id: String = "v1") =
            VocabularyModel(
                vocabularyId = id,
                userId = "u1",
                name = "Kotlin",
                description = null,
                tagIds = listOf("tag1"),
            )

        val resolvedTag = VocabularyTagModel("tag1", "u1", "kotlin", order = 1)

        "listByConditions: 変換して返す" {
            every { repository.findByUser("u1", null, null) } returns listOf(item("v1"), item("v2"))
            every { vocabularyTagService.findByIds("u1", listOf("tag1")) } returns listOf(resolvedTag)

            val res = service.listByConditions("u1")

            res.shouldHaveSize(2)
            res[0].tags[0].vocabularyTag shouldBe "kotlin"
        }

        "listByConditions: updatedDateTime の降順でソートされる" {
            val older = item("v1").also { it.updatedDateTime = "2025-01-01T00:00:00Z" }
            val newer = item("v2").also { it.updatedDateTime = "2025-06-01T00:00:00Z" }
            every { repository.findByUser("u1", null, null) } returns listOf(older, newer)
            every { vocabularyTagService.findByIds("u1", listOf("tag1")) } returns listOf(resolvedTag)

            val res = service.listByConditions("u1")

            res[0].vocabularyId shouldBe "v2"
            res[1].vocabularyId shouldBe "v1"
        }

        "listByConditions: tagIds でフィルタ" {
            every { repository.findByUser("u1", null, null) } returns listOf(
                item("v1").also { it.tagIds = listOf("t1") },
                item("v2").also { it.tagIds = listOf("t2") },
            )
            every { vocabularyTagService.findByIds("u1", listOf("t1")) } returns listOf(
                VocabularyTagModel("t1", "u1", "kotlin", 1),
            )

            val res = service.listByConditions("u1", tagIds = listOf("t1"))

            res.shouldHaveSize(1)
            res[0].vocabularyId shouldBe "v1"
        }

        "getByVocabularyIdForUser: 所有者なら返す、他ユーザなら null" {
            every { repository.getByVocabularyId("v1") } returns item(id = "v1", userId = "u1")
            every { repository.getByVocabularyId("v2") } returns item(id = "v2", userId = "other")
            every { vocabularyTagService.findByIds(any(), any()) } returns emptyList()

            service.getByVocabularyIdForUser("v1", "u1")!!.vocabularyId shouldBe "v1"
            service.getByVocabularyIdForUser("v2", "u1") shouldBe null
        }

        "put: 新規の場合は createdDateTime と updatedDateTime を設定する" {
            mockkStatic(OffsetDateTime::class)
            every { OffsetDateTime.now() } returns fixedNow
            every { repository.getByVocabularyId("v-new") } returns null
            val capt = slot<VocabularyItem>()
            every { repository.put(capture(capt)) } returns Unit

            service.put(model(id = "v-new"))

            capt.captured.createdDateTime shouldBe fixedNow.toString()
            capt.captured.updatedDateTime shouldBe fixedNow.toString()

            unmockkStatic(OffsetDateTime::class)
        }

        "put: 既存の場合は createdDateTime を保持し updatedDateTime を更新する" {
            mockkStatic(OffsetDateTime::class)
            every { OffsetDateTime.now() } returns fixedNow
            every { repository.getByVocabularyId("v-exist") } returns item(id = "v-exist")
            val capt = slot<VocabularyItem>()
            every { repository.put(capture(capt)) } returns Unit

            service.put(model(id = "v-exist"))

            capt.captured.createdDateTime shouldBe "2025-01-01T00:00:00Z"
            capt.captured.updatedDateTime shouldBe fixedNow.toString()

            unmockkStatic(OffsetDateTime::class)
        }

        "create: UUID を採番して put する" {
            mockkStatic(UUID::class)
            mockkStatic(OffsetDateTime::class)
            val fixed = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb")
            every { UUID.randomUUID() } returns fixed
            every { OffsetDateTime.now() } returns fixedNow
            every { repository.getByVocabularyId(fixed.toString()) } returns null
            val capt = slot<VocabularyItem>()
            every { repository.put(capture(capt)) } returns Unit

            val returnedId = service.create(model(id = ""))

            returnedId shouldBe fixed.toString()
            capt.captured.vocabularyId shouldBe fixed.toString()

            unmockkStatic(UUID::class)
            unmockkStatic(OffsetDateTime::class)
        }

        "delete: 削除を委譲" {
            service.delete("u1", "v-1")
            verify(exactly = 1) { repository.delete("u1", "v-1") }
        }
    })
