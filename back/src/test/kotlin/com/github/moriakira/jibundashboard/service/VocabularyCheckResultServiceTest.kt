package com.github.moriakira.jibundashboard.service

import com.github.moriakira.jibundashboard.repository.VocabularyCheckResultItem
import com.github.moriakira.jibundashboard.repository.VocabularyCheckResultRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify

class VocabularyCheckResultServiceTest :
    StringSpec({

        val repository = mockk<VocabularyCheckResultRepository>(relaxed = true)
        val service = VocabularyCheckResultService(repository)

        beforeTest {
            clearMocks(repository, answers = false, recordedCalls = true, childMocks = true)
        }

        fun item(
            checkResultId: String = "r1",
            vocabularyId: String = "v1",
            userId: String = "u1",
            severity: String? = "HIGH",
            status: String = "UNCHECKED",
            checkedAt: String = "2025-06-01T00:00:00Z",
        ) = VocabularyCheckResultItem().apply {
            this.vocabularyCheckResultId = checkResultId
            this.vocabularyId = vocabularyId
            this.userId = userId
            this.vocabularyName = "Kotlin"
            this.severity = severity
            this.status = status
            this.report = "## report"
            this.vocabularyUpdatedAt = "2025-05-01T00:00:00Z"
            this.checkedAt = checkedAt
        }

        "listByUser: checkedAt の降順で返す" {
            val older = item(checkResultId = "r1", vocabularyId = "v1", checkedAt = "2025-01-01T00:00:00Z")
            val newer = item(checkResultId = "r2", vocabularyId = "v2", checkedAt = "2025-06-01T00:00:00Z")
            every { repository.findByUser("u1") } returns listOf(older, newer)

            val res = service.listByUser("u1")

            res.shouldHaveSize(2)
            res[0].vocabularyCheckResultId shouldBe "r2"
            res[1].vocabularyCheckResultId shouldBe "r1"
        }

        "listByUser: severity が null のレコードは除外する" {
            val withIssue = item(checkResultId = "r1", severity = "HIGH")
            val clean = item(checkResultId = "r2", severity = null)
            every { repository.findByUser("u1") } returns listOf(withIssue, clean)

            val res = service.listByUser("u1")

            res.shouldHaveSize(1)
            res[0].vocabularyCheckResultId shouldBe "r1"
        }

        "updateStatus: 所有者なら status を更新して保存する" {
            every { repository.getByCheckResultId("r1") } returns item()
            val capt = slot<VocabularyCheckResultItem>()
            every { repository.put(capture(capt)) } returns Unit

            val result = service.updateStatus("r1", "u1", "CHECKED")

            result!!.status shouldBe "CHECKED"
            capt.captured.status shouldBe "CHECKED"
            verify(exactly = 1) { repository.put(any()) }
        }

        "updateStatus: チェック結果が存在しない場合は null を返す" {
            every { repository.getByCheckResultId("missing") } returns null

            val result = service.updateStatus("missing", "u1", "CHECKED")

            result shouldBe null
            verify(exactly = 0) { repository.put(any()) }
        }

        "updateStatus: 他ユーザのチェック結果は null を返す" {
            every { repository.getByCheckResultId("r1") } returns item(userId = "other")

            val result = service.updateStatus("r1", "u1", "CHECKED")

            result shouldBe null
            verify(exactly = 0) { repository.put(any()) }
        }
    })
