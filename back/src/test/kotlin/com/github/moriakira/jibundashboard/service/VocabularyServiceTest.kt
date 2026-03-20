package com.github.moriakira.jibundashboard.service

import com.github.moriakira.jibundashboard.repository.VocabularyItem
import com.github.moriakira.jibundashboard.repository.VocabularyRepository
import com.github.moriakira.jibundashboard.repository.VocabularyTagEmbed
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
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

        fun tagEmbed(id: String = "tag1", tag: String = "kotlin") =
            VocabularyTagEmbed().apply {
                vocabularyTagId = id
                vocabularyTag = tag
            }

        fun item(
            id: String = "v1",
            userId: String = "u1",
            name: String = "Kotlin",
            tags: List<VocabularyTagEmbed> = listOf(tagEmbed()),
        ) = VocabularyItem().apply {
            vocabularyId = id
            this.userId = userId
            this.name = name
            description = "A language"
            this.tags = tags
            createdDateTime = "2025-01-01T00:00:00Z"
            updatedDateTime = "2025-01-01T00:00:00Z"
        }

        fun model(
            id: String = "v1",
            userId: String = "u1",
            name: String = "Kotlin",
            tags: List<VocabularyTagModel> = listOf(VocabularyTagModel("tag1", "u1", "kotlin")),
        ) = VocabularyModel(
            vocabularyId = id,
            userId = userId,
            name = name,
            description = null,
            tags = tags,
        )

        "listByConditions: 条件なしで全件変換して返す" {
            every { repository.findByUser("u1", null) } returns listOf(
                item("v1"),
                item("v2", name = "Java", tags = listOf(tagEmbed("tag2", "java"))),
            )

            val res = service.listByConditions("u1")

            res.shouldHaveSize(2)
            res[0].vocabularyId shouldBe "v1"
            res[0].name shouldBe "Kotlin"
            res[0].tags.shouldHaveSize(1)
            res[0].tags[0].vocabularyTag shouldBe "kotlin"
            res[1].vocabularyId shouldBe "v2"
        }

        "listByConditions: vocabularyName でフィルタ委譲" {
            every { repository.findByUser("u1", "Kotlin") } returns listOf(item("v1"))

            val res = service.listByConditions("u1", vocabularyName = "Kotlin")

            res.shouldHaveSize(1)
            verify(exactly = 1) { repository.findByUser("u1", "Kotlin") }
        }

        "listByConditions: tags でインメモリフィルタ" {
            every { repository.findByUser("u1", null) } returns listOf(
                item("v1", tags = listOf(tagEmbed("tag1", "kotlin"))),
                item("v2", tags = listOf(tagEmbed("tag2", "java"))),
                item("v3", tags = emptyList()),
            )

            val res = service.listByConditions("u1", tags = listOf("kotlin"))

            res.shouldHaveSize(1)
            res[0].vocabularyId shouldBe "v1"
        }

        "listByConditions: tags が空なら全件返す" {
            every { repository.findByUser("u1", null) } returns listOf(item("v1"), item("v2"))

            val res = service.listByConditions("u1", tags = emptyList())

            res.shouldHaveSize(2)
        }

        "getByVocabularyId: 存在すれば返す、無ければ null" {
            every { repository.getByVocabularyId("v1") } returns item(id = "v1")
            every { repository.getByVocabularyId("nope") } returns null

            service.getByVocabularyId("v1")!!.vocabularyId shouldBe "v1"
            service.getByVocabularyId("nope") shouldBe null
        }

        "getByVocabularyIdForUser: 所有者なら返す" {
            every { repository.getByVocabularyId("v1") } returns item(id = "v1", userId = "u1")

            val result = service.getByVocabularyIdForUser("v1", "u1")

            result!!.vocabularyId shouldBe "v1"
        }

        "getByVocabularyIdForUser: 他ユーザなら null" {
            every { repository.getByVocabularyId("v2") } returns item(id = "v2", userId = "other")

            val result = service.getByVocabularyIdForUser("v2", "u1")

            result shouldBe null
        }

        "getByVocabularyIdForUser: 存在しなければ null" {
            every { repository.getByVocabularyId("nope") } returns null

            val result = service.getByVocabularyIdForUser("nope", "u1")

            result shouldBe null
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
            capt.captured.updatedDateTime shouldNotBe "2025-01-01T00:00:00Z"

            unmockkStatic(OffsetDateTime::class)
        }

        "put: モデルを保存して ID を返す" {
            mockkStatic(OffsetDateTime::class)
            every { OffsetDateTime.now() } returns fixedNow
            every { repository.getByVocabularyId("v-7") } returns null
            val capt = slot<VocabularyItem>()
            every { repository.put(capture(capt)) } returns Unit

            val ret = service.put(model(id = "v-7", name = "Rust"))

            ret shouldBe "v-7"
            capt.captured.vocabularyId shouldBe "v-7"
            capt.captured.name shouldBe "Rust"

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
