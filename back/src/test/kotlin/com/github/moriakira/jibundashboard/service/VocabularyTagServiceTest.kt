package com.github.moriakira.jibundashboard.service

import com.github.moriakira.jibundashboard.repository.VocabularyTagItem
import com.github.moriakira.jibundashboard.repository.VocabularyTagRepository
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
import java.util.UUID

class VocabularyTagServiceTest :
    StringSpec({

        val repository = mockk<VocabularyTagRepository>(relaxed = true)
        val service = VocabularyTagService(repository)

        beforeTest {
            clearMocks(repository, answers = false, recordedCalls = true, childMocks = true)
        }

        fun item(
            id: String = "tag1",
            userId: String = "u1",
            tag: String = "kotlin",
        ) = VocabularyTagItem().apply {
            vocabularyTagId = id
            this.userId = userId
            vocabularyTag = tag
        }

        "listByConditions: タグ名なしで全件委譲して変換する" {
            every { repository.findByUser("u1", null) } returns listOf(item("tag1"), item("tag2", tag = "java"))

            val res = service.listByConditions("u1")

            res.shouldHaveSize(2)
            res[0].vocabularyTagId shouldBe "tag1"
            res[0].vocabularyTag shouldBe "kotlin"
            res[1].vocabularyTagId shouldBe "tag2"
            verify(exactly = 1) { repository.findByUser("u1", null) }
        }

        "listByConditions: タグ名指定で委譲する" {
            every { repository.findByUser("u1", "kotlin") } returns listOf(item("tag1"))

            val res = service.listByConditions("u1", "kotlin")

            res.shouldHaveSize(1)
            res[0].vocabularyTagId shouldBe "tag1"
            verify(exactly = 1) { repository.findByUser("u1", "kotlin") }
        }

        "getByVocabularyTagId: 存在すれば返す、無ければ null" {
            every { repository.getByVocabularyTagId("tag-1") } returns item(id = "tag-1")
            every { repository.getByVocabularyTagId("nope") } returns null

            service.getByVocabularyTagId("tag-1")!!.vocabularyTagId shouldBe "tag-1"
            service.getByVocabularyTagId("nope") shouldBe null
        }

        "getByVocabularyTagIdForUser: 所有者なら返す" {
            every { repository.getByVocabularyTagId("tag-1") } returns item(id = "tag-1", userId = "u1")

            val result = service.getByVocabularyTagIdForUser("tag-1", "u1")

            result!!.vocabularyTagId shouldBe "tag-1"
            result.userId shouldBe "u1"
        }

        "getByVocabularyTagIdForUser: 他ユーザなら null" {
            every { repository.getByVocabularyTagId("tag-2") } returns item(id = "tag-2", userId = "other")

            val result = service.getByVocabularyTagIdForUser("tag-2", "u1")

            result shouldBe null
        }

        "getByVocabularyTagIdForUser: 存在しなければ null" {
            every { repository.getByVocabularyTagId("nope") } returns null

            val result = service.getByVocabularyTagIdForUser("nope", "u1")

            result shouldBe null
        }

        "put: モデルを保存して ID を返す" {
            val capt = slot<VocabularyTagItem>()
            every { repository.put(capture(capt)) } returns Unit

            val model = VocabularyTagModel(
                vocabularyTagId = "tag-7",
                userId = "u7",
                vocabularyTag = "scala",
            )

            val ret = service.put(model)

            ret shouldBe "tag-7"
            capt.captured.vocabularyTagId shouldBe "tag-7"
            capt.captured.userId shouldBe "u7"
            capt.captured.vocabularyTag shouldBe "scala"
        }

        "create: UUID を採番して put する" {
            mockkStatic(UUID::class)
            val fixed = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
            every { UUID.randomUUID() } returns fixed
            val capt = slot<VocabularyTagItem>()
            every { repository.put(capture(capt)) } returns Unit

            val model = VocabularyTagModel(vocabularyTagId = "", userId = "u1", vocabularyTag = "rust")
            val returnedId = service.create(model)

            returnedId shouldBe fixed.toString()
            capt.captured.vocabularyTagId shouldBe fixed.toString()

            unmockkStatic(UUID::class)
        }

        "delete: 削除を委譲" {
            service.delete("u1", "tag-1")
            verify(exactly = 1) { repository.delete("u1", "tag-1") }
        }
    })
