package com.github.moriakira.jibundashboard.service

import com.github.moriakira.jibundashboard.repository.CardbookCheckResultItem
import com.github.moriakira.jibundashboard.repository.CardbookCheckResultRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify

class CardbookCheckResultServiceTest :
    StringSpec({

        val repository = mockk<CardbookCheckResultRepository>(relaxed = true)
        val service = CardbookCheckResultService(repository)

        beforeTest {
            clearMocks(repository, answers = false, recordedCalls = true, childMocks = true)
        }

        @Suppress("LongParameterList")
        fun item(
            checkResultId: String = "r1",
            cardId: String = "c1",
            cardbookId: String = "cb1",
            userId: String = "u1",
            severity: String? = "HIGH",
            status: String = "UNCHECKED",
            checkedAt: String = "2025-06-01T00:00:00Z",
        ) = CardbookCheckResultItem().apply {
            this.cardbookCheckResultId = checkResultId
            this.cardId = cardId
            this.cardbookId = cardbookId
            this.userId = userId
            this.front = "ephemeral"
            this.severity = severity
            this.status = status
            this.report = "## report"
            this.cardUpdatedAt = "2025-05-01T00:00:00Z"
            this.checkedAt = checkedAt
        }

        "listByUser: checkedAt の降順で返す" {
            val older = item(checkResultId = "r1", cardId = "c1", checkedAt = "2025-01-01T00:00:00Z")
            val newer = item(checkResultId = "r2", cardId = "c2", checkedAt = "2025-06-01T00:00:00Z")
            every { repository.findByUser("u1") } returns listOf(older, newer)

            val res = service.listByUser("u1")

            res.shouldHaveSize(2)
            res[0].cardbookCheckResultId shouldBe "r2"
            res[1].cardbookCheckResultId shouldBe "r1"
        }

        "listByUser: severity が null のレコードは除外する" {
            val withIssue = item(checkResultId = "r1", severity = "HIGH")
            val clean = item(checkResultId = "r2", severity = null)
            every { repository.findByUser("u1") } returns listOf(withIssue, clean)

            val res = service.listByUser("u1")

            res.shouldHaveSize(1)
            res[0].cardbookCheckResultId shouldBe "r1"
        }

        "listByUser: cardbookId で絞り込みができる" {
            val a = item(checkResultId = "r1", cardbookId = "cb1")
            val b = item(checkResultId = "r2", cardbookId = "cb2")
            every { repository.findByUser("u1") } returns listOf(a, b)

            val res = service.listByUser("u1", cardbookId = "cb2")

            res.shouldHaveSize(1)
            res[0].cardbookCheckResultId shouldBe "r2"
        }

        "listByUser: checkedAtFrom/checkedAtTo で範囲絞り込みができる" {
            val old = item(checkResultId = "r1", checkedAt = "2025-03-01T00:00:00Z")
            val new = item(checkResultId = "r2", checkedAt = "2025-06-01T00:00:00Z")
            every { repository.findByUser("u1") } returns listOf(old, new)

            service.listByUser("u1", checkedAtFrom = "2025-04-01")
                .map { it.cardbookCheckResultId } shouldBe listOf("r2")
            service.listByUser("u1", checkedAtTo = "2025-04-01")
                .map { it.cardbookCheckResultId } shouldBe listOf("r1")
        }

        "listByUser: severities/statuses で絞り込みができる" {
            val high = item(checkResultId = "r1", severity = "HIGH", status = "UNCHECKED")
            val medium = item(checkResultId = "r2", severity = "MEDIUM", status = "CHECKED")
            every { repository.findByUser("u1") } returns listOf(high, medium)

            service.listByUser("u1", severities = listOf("HIGH"))
                .map { it.cardbookCheckResultId } shouldBe listOf("r1")
            service.listByUser("u1", statuses = listOf("CHECKED"))
                .map { it.cardbookCheckResultId } shouldBe listOf("r2")
        }

        "updateStatus: 所有者なら status を更新して保存する" {
            every { repository.getByCheckResultId("r1") } returns item()
            val capt = slot<CardbookCheckResultItem>()
            every { repository.put(capture(capt)) } returns Unit

            val result = service.updateStatus("r1", "u1", "CHECKED")

            result!!.status shouldBe "CHECKED"
            capt.captured.status shouldBe "CHECKED"
            verify(exactly = 1) { repository.put(any()) }
        }

        "updateStatus: 存在しない / 他ユーザの場合は null を返す" {
            every { repository.getByCheckResultId("missing") } returns null
            every { repository.getByCheckResultId("r1") } returns item(userId = "other")

            service.updateStatus("missing", "u1", "CHECKED") shouldBe null
            service.updateStatus("r1", "u1", "CHECKED") shouldBe null
            verify(exactly = 0) { repository.put(any()) }
        }
    })
