package com.github.moriakira.jibundashboard.service

import com.github.moriakira.jibundashboard.repository.VocabularyItem
import com.github.moriakira.jibundashboard.repository.VocabularyRepository
import com.github.moriakira.jibundashboard.repository.VocabularyTagEmbed
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
        val service = VocabularyService(repository)

        val fixedNow = OffsetDateTime.parse("2025-06-01T12:00:00Z")

        beforeTest {
            clearMocks(repository, answers = false, recordedCalls = true, childMocks = true)
        }

        fun item(id: String = "v1", userId: String = "u1") =
            VocabularyItem().apply {
                vocabularyId = id
                this.userId = userId
                name = "Kotlin"
                description = "A language"
                tags = listOf(
                    VocabularyTagEmbed().also {
                        it.vocabularyTagId = "tag1"
                        it.vocabularyTag = "kotlin"
                    },
                )
                createdDateTime = "2025-01-01T00:00:00Z"
                updatedDateTime = "2025-01-01T00:00:00Z"
            }

        fun model(id: String = "v1") =
            VocabularyModel(
                vocabularyId = id,
                userId = "u1",
                name = "Kotlin",
                description = null,
                tags = listOf(VocabularyTagModel("tag1", "u1", "kotlin")),
            )

        "listByConditions: 変換して返す" {
            every { repository.findByUser("u1", null) } returns listOf(item("v1"), item("v2"))

            val res = service.listByConditions("u1")

            res.shouldHaveSize(2)
            res[0].vocabularyId shouldBe "v1"
            res[0].tags[0].vocabularyTag shouldBe "kotlin"
        }

        "listByConditions: tags でインメモリフィルタ" {
            fun embed(id: String, tag: String) =
                VocabularyTagEmbed().also {
                    it.vocabularyTagId = id
                    it.vocabularyTag = tag
                }
            every { repository.findByUser("u1", null) } returns listOf(
                item("v1").also { it.tags = listOf(embed("t1", "kotlin")) },
                item("v2").also { it.tags = listOf(embed("t2", "java")) },
            )

            val res = service.listByConditions("u1", tags = listOf("kotlin"))

            res.shouldHaveSize(1)
            res[0].vocabularyId shouldBe "v1"
        }

        "getByVocabularyIdForUser: 所有者なら返す、他ユーザなら null" {
            every { repository.getByVocabularyId("v1") } returns item(id = "v1", userId = "u1")
            every { repository.getByVocabularyId("v2") } returns item(id = "v2", userId = "other")

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
