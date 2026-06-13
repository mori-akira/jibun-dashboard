package com.github.moriakira.jibundashboard.service

import com.github.moriakira.jibundashboard.repository.CardbookQuizHistoryAnswerItem
import com.github.moriakira.jibundashboard.repository.CardbookQuizHistoryItem
import com.github.moriakira.jibundashboard.repository.CardbookQuizHistoryRepository
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

class CardbookQuizHistoryServiceTest :
    StringSpec({

        val repository = mockk<CardbookQuizHistoryRepository>(relaxed = true)
        val service = CardbookQuizHistoryService(repository)

        val fixedNow = OffsetDateTime.parse("2025-06-01T12:00:00Z")

        beforeTest {
            clearMocks(repository, answers = false, recordedCalls = true, childMocks = true)
        }

        fun answerItem(cardId: String, result: String) =
            CardbookQuizHistoryAnswerItem().apply {
                this.cardId = cardId
                this.result = result
            }

        fun item(id: String = "qh1", cardbookId: String = "cb1") =
            CardbookQuizHistoryItem().apply {
                quizHistoryId = id
                userId = "u1"
                this.cardbookId = cardbookId
                answeredAt = "2025-01-01T00:00:00Z"
                questionCount = 2
                direction = "FRONT_TO_BACK"
                correctCount = 1
                incorrectCount = 1
                answers = listOf(
                    answerItem("card1", "CORRECT"),
                    answerItem("card2", "INCORRECT"),
                )
            }

        fun model(id: String = "qh1") =
            CardbookQuizHistoryModel(
                quizHistoryId = id,
                userId = "u1",
                cardbookId = "cb1",
                answeredAt = "2025-01-01T00:00:00Z",
                questionCount = 2,
                direction = "FRONT_TO_BACK",
                correctCount = 1,
                incorrectCount = 1,
                answers = listOf(
                    CardbookQuizHistoryAnswerModel("card1", "CORRECT"),
                    CardbookQuizHistoryAnswerModel("card2", "INCORRECT"),
                ),
            )

        "listByUser: 変換して返す" {
            every { repository.findByUser("u1") } returns listOf(item("qh1"), item("qh2"))

            val res = service.listByUser("u1")

            res.shouldHaveSize(2)
            res[0].quizHistoryId shouldBe "qh1"
            res[0].answers.shouldHaveSize(2)
            res[0].answers[0].result shouldBe "CORRECT"
        }

        "listByUser: cardbookId フィルタで絞り込む" {
            every { repository.findByUser("u1") } returns listOf(
                item("qh1", cardbookId = "cb1"),
                item("qh2", cardbookId = "cb2"),
            )

            val res = service.listByUser("u1", cardbookId = "cb1")

            res.shouldHaveSize(1)
            res[0].quizHistoryId shouldBe "qh1"
        }

        "getByQuizHistoryIdForUser: 存在すれば返す、存在しなければ null" {
            every { repository.getByUserAndQuizHistoryId("u1", "qh1") } returns item("qh1")
            every { repository.getByUserAndQuizHistoryId("u1", "qh-missing") } returns null

            service.getByQuizHistoryIdForUser("qh1", "u1")!!.quizHistoryId shouldBe "qh1"
            service.getByQuizHistoryIdForUser("qh-missing", "u1") shouldBe null
        }

        "create: UUID を採番し answeredAt を設定し correct/incorrect を集計する" {
            mockkStatic(UUID::class)
            mockkStatic(OffsetDateTime::class)
            val fixedId = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd")
            every { UUID.randomUUID() } returns fixedId
            every { OffsetDateTime.now() } returns fixedNow
            val capt = slot<CardbookQuizHistoryItem>()
            every { repository.put(capture(capt)) } returns Unit

            val input = model(id = "").copy(
                quizHistoryId = "",
                answeredAt = "",
                correctCount = 0,
                incorrectCount = 0,
                answers = listOf(
                    CardbookQuizHistoryAnswerModel("card1", "CORRECT"),
                    CardbookQuizHistoryAnswerModel("card2", "INCORRECT"),
                    CardbookQuizHistoryAnswerModel("card3", "CORRECT"),
                ),
            )

            val returnedId = service.create(input)

            returnedId shouldBe fixedId.toString()
            capt.captured.quizHistoryId shouldBe fixedId.toString()
            capt.captured.answeredAt shouldBe fixedNow.toString()
            capt.captured.correctCount shouldBe 2
            capt.captured.incorrectCount shouldBe 1

            unmockkStatic(UUID::class)
            unmockkStatic(OffsetDateTime::class)
        }

        "delete: 削除を委譲" {
            service.delete("u1", "qh-1")
            verify(exactly = 1) { repository.delete("u1", "qh-1") }
        }
    })
