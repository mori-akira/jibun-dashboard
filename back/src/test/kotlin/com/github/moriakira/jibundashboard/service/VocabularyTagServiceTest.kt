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

        fun item(id: String = "tag1", userId: String = "u1") =
            VocabularyTagItem().apply {
                vocabularyTagId = id
                this.userId = userId
                vocabularyTag = "kotlin"
                order = 1
            }

        "listByConditions: 変換して返す" {
            every { repository.findByUser("u1", null) } returns listOf(item("tag1"), item("tag2"))

            val res = service.listByConditions("u1")

            res.shouldHaveSize(2)
            res[0].vocabularyTagId shouldBe "tag1"
            res[0].vocabularyTag shouldBe "kotlin"
            verify(exactly = 1) { repository.findByUser("u1", null) }
        }

        "getByVocabularyTagIdForUser: 所有者なら返す、他ユーザなら null" {
            every { repository.getByVocabularyTagId("tag-1") } returns item(id = "tag-1", userId = "u1")
            every { repository.getByVocabularyTagId("tag-2") } returns item(id = "tag-2", userId = "other")

            service.getByVocabularyTagIdForUser("tag-1", "u1")!!.vocabularyTagId shouldBe "tag-1"
            service.getByVocabularyTagIdForUser("tag-2", "u1") shouldBe null
        }

        "put: モデルを保存して ID を返す" {
            val capt = slot<VocabularyTagItem>()
            every { repository.put(capture(capt)) } returns Unit

            val ret = service.put(
                VocabularyTagModel(vocabularyTagId = "tag-7", userId = "u7", vocabularyTag = "scala", order = 2),
            )

            ret shouldBe "tag-7"
            capt.captured.vocabularyTagId shouldBe "tag-7"
            capt.captured.vocabularyTag shouldBe "scala"
            capt.captured.order shouldBe 2
        }

        "create: UUID を採番して put する" {
            mockkStatic(UUID::class)
            val fixed = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
            every { UUID.randomUUID() } returns fixed
            val capt = slot<VocabularyTagItem>()
            every { repository.put(capture(capt)) } returns Unit

            val returnedId = service.create(
                VocabularyTagModel(vocabularyTagId = "", userId = "u1", vocabularyTag = "rust", order = 3),
            )

            returnedId shouldBe fixed.toString()
            capt.captured.vocabularyTagId shouldBe fixed.toString()

            unmockkStatic(UUID::class)
        }

        "delete: 削除を委譲" {
            service.delete("u1", "tag-1")
            verify(exactly = 1) { repository.delete("u1", "tag-1") }
        }
    })
