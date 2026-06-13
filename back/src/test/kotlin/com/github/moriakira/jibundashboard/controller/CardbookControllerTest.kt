package com.github.moriakira.jibundashboard.controller

import com.github.moriakira.jibundashboard.component.CurrentAuth
import com.github.moriakira.jibundashboard.generated.model.CardbookBase
import com.github.moriakira.jibundashboard.generated.model.CardbookCardBase
import com.github.moriakira.jibundashboard.generated.model.CardbookQuizHistoryAnswer
import com.github.moriakira.jibundashboard.generated.model.CardbookQuizHistoryBase
import com.github.moriakira.jibundashboard.service.CardbookCardModel
import com.github.moriakira.jibundashboard.service.CardbookCardService
import com.github.moriakira.jibundashboard.service.CardbookModel
import com.github.moriakira.jibundashboard.service.CardbookQuizHistoryAnswerModel
import com.github.moriakira.jibundashboard.service.CardbookQuizHistoryModel
import com.github.moriakira.jibundashboard.service.CardbookQuizHistoryService
import com.github.moriakira.jibundashboard.service.CardbookService
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.http.HttpStatus
import java.util.UUID

class CardbookControllerTest :
    StringSpec({

        val currentAuth = mockk<CurrentAuth>(relaxed = true)
        val cardbookService = mockk<CardbookService>(relaxed = true)
        val cardbookCardService = mockk<CardbookCardService>(relaxed = true)
        val cardbookQuizHistoryService = mockk<CardbookQuizHistoryService>(relaxed = true)
        val controller = CardbookController(
            currentAuth,
            cardbookService,
            cardbookCardService,
            cardbookQuizHistoryService,
        )

        beforeTest {
            every { currentAuth.userId } returns "u1"
            clearMocks(cardbookService, answers = false, recordedCalls = true, childMocks = true)
            clearMocks(cardbookCardService, answers = false, recordedCalls = true, childMocks = true)
            clearMocks(cardbookQuizHistoryService, answers = false, recordedCalls = true, childMocks = true)
        }

        fun cardbookModel(id: String = "11111111-1111-1111-1111-111111111111") =
            CardbookModel(
                cardbookId = id,
                userId = "u1",
                name = "My Cardbook",
                createdDateTime = "2025-01-01T00:00:00Z",
                updatedDateTime = "2025-01-01T00:00:00Z",
            )

        fun cardModel(
            id: String = "22222222-2222-2222-2222-222222222222",
            cardbookId: String = "11111111-1111-1111-1111-111111111111",
        ) = CardbookCardModel(
            cardId = id,
            cardbookId = cardbookId,
            userId = "u1",
            front = "Front text",
            back = "Back text",
            createdDateTime = "2025-01-01T00:00:00Z",
        )

        fun quizHistoryModel(id: String = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa") =
            CardbookQuizHistoryModel(
                quizHistoryId = id,
                userId = "u1",
                cardbookId = "11111111-1111-1111-1111-111111111111",
                answeredAt = "2025-01-01T00:00:00Z",
                questionCount = 2,
                direction = "FRONT_TO_BACK",
                correctCount = 1,
                incorrectCount = 1,
                answers = listOf(
                    CardbookQuizHistoryAnswerModel("22222222-2222-2222-2222-222222222222", "CORRECT"),
                    CardbookQuizHistoryAnswerModel("33333333-3333-3333-3333-333333333333", "INCORRECT"),
                ),
            )

        // --- Cardbook ---

        "getCardbooks: 一覧を返す" {
            every { cardbookService.listByUser("u1") } returns listOf(cardbookModel())

            val res = controller.getCardbooks()

            res.statusCode shouldBe HttpStatus.OK
            res.body!!.shouldHaveSize(1)
            res.body!![0].name shouldBe "My Cardbook"
        }

        "postCardbooks: 新規作成で 201" {
            every { cardbookService.create(any()) } returns "11111111-1111-1111-1111-111111111111"

            val res = controller.postCardbooks(CardbookBase(name = "My Cardbook"))

            res.statusCode shouldBe HttpStatus.CREATED
            res.body!!.cardbookId.toString() shouldBe "11111111-1111-1111-1111-111111111111"
        }

        "getCardbooksById: 200 または 404" {
            val id = "eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee"
            val missingId = "ffffffff-ffff-ffff-ffff-ffffffffffff"
            every { cardbookService.getByCardbookIdForUser(id, "u1") } returns cardbookModel(id = id)
            every { cardbookService.getByCardbookIdForUser(missingId, "u1") } returns null

            controller.getCardbooksById(UUID.fromString(id)).statusCode shouldBe HttpStatus.OK
            controller.getCardbooksById(UUID.fromString(missingId)).statusCode shouldBe HttpStatus.NOT_FOUND
        }

        "putCardbooksById: 更新で 200、put が 1 回だけ呼ばれる" {
            val id = "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"
            every { cardbookService.getByCardbookIdForUser(id, "u1") } returns cardbookModel(id = id)

            val res = controller.putCardbooksById(UUID.fromString(id), CardbookBase(name = "Updated"))

            res.statusCode shouldBe HttpStatus.OK
            res.body!!.cardbookId.toString() shouldBe id
            verify(exactly = 1) { cardbookService.put(any()) }
        }

        "putCardbooksById: 更新対象が無ければ 404、put は呼ばれない" {
            every { cardbookService.getByCardbookIdForUser(any(), any()) } returns null

            val res = controller.putCardbooksById(
                UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc"),
                CardbookBase(name = "Updated"),
            )

            res.statusCode shouldBe HttpStatus.NOT_FOUND
            verify(exactly = 0) { cardbookService.put(any()) }
        }

        "deleteCardbooksById: 204 または 404" {
            val id = "14141414-1414-1414-1414-141414141414"
            val missingId = "12121212-1212-1212-1212-121212121212"
            every { cardbookService.getByCardbookIdForUser(id, "u1") } returns cardbookModel(id = id)
            every { cardbookService.getByCardbookIdForUser(missingId, "u1") } returns null

            controller.deleteCardbooksById(UUID.fromString(id)).statusCode shouldBe HttpStatus.NO_CONTENT
            controller.deleteCardbooksById(UUID.fromString(missingId)).statusCode shouldBe HttpStatus.NOT_FOUND
        }

        // --- CardbookCard ---

        "getCardbookCards: カード帳が存在すればカード一覧を返す" {
            val cbId = "11111111-1111-1111-1111-111111111111"
            every { cardbookService.getByCardbookIdForUser(cbId, "u1") } returns cardbookModel(id = cbId)
            every { cardbookCardService.listByCardbookId(cbId) } returns listOf(cardModel())

            val res = controller.getCardbookCards(UUID.fromString(cbId))

            res.statusCode shouldBe HttpStatus.OK
            res.body!!.shouldHaveSize(1)
            res.body!![0].front shouldBe "Front text"
        }

        "getCardbookCards: カード帳が無ければ 404" {
            every { cardbookService.getByCardbookIdForUser(any(), any()) } returns null

            val res = controller.getCardbookCards(UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff"))
            res.statusCode shouldBe HttpStatus.NOT_FOUND
        }

        "postCardbookCards: 新規作成で 201" {
            val cbId = "11111111-1111-1111-1111-111111111111"
            every { cardbookService.getByCardbookIdForUser(cbId, "u1") } returns cardbookModel(id = cbId)
            every { cardbookCardService.create(any()) } returns "22222222-2222-2222-2222-222222222222"

            val res = controller.postCardbookCards(
                UUID.fromString(cbId),
                CardbookCardBase(front = "Front", back = "Back"),
            )

            res.statusCode shouldBe HttpStatus.CREATED
            res.body!!.cardId.toString() shouldBe "22222222-2222-2222-2222-222222222222"
        }

        "postCardbookCards: カード帳が無ければ 404" {
            every { cardbookService.getByCardbookIdForUser(any(), any()) } returns null

            val res = controller.postCardbookCards(
                UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff"),
                CardbookCardBase(front = "Front", back = "Back"),
            )

            res.statusCode shouldBe HttpStatus.NOT_FOUND
        }

        "putCardbookCardsById: 更新で 200" {
            val cbId = "11111111-1111-1111-1111-111111111111"
            val cardId = "22222222-2222-2222-2222-222222222222"
            every {
                cardbookCardService.getByCardIdForUser(cardId, "u1")
            } returns cardModel(id = cardId, cardbookId = cbId)

            val res = controller.putCardbookCardsById(
                UUID.fromString(cbId),
                UUID.fromString(cardId),
                CardbookCardBase(front = "Updated", back = "Updated back"),
            )

            res.statusCode shouldBe HttpStatus.OK
            res.body!!.cardId.toString() shouldBe cardId
        }

        "putCardbookCardsById: カードが無ければ 404" {
            every { cardbookCardService.getByCardIdForUser(any(), any()) } returns null

            val res = controller.putCardbookCardsById(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff"),
                CardbookCardBase(front = "Updated", back = "Updated back"),
            )

            res.statusCode shouldBe HttpStatus.NOT_FOUND
        }

        "putCardbookCardsById: cardbookId が一致しなければ 404" {
            val cardId = "22222222-2222-2222-2222-222222222222"
            every {
                cardbookCardService.getByCardIdForUser(cardId, "u1")
            } returns cardModel(id = cardId, cardbookId = "99999999-9999-9999-9999-999999999999")

            val res = controller.putCardbookCardsById(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                UUID.fromString(cardId),
                CardbookCardBase(front = "Updated", back = "Updated back"),
            )

            res.statusCode shouldBe HttpStatus.NOT_FOUND
        }

        "deleteCardbookCardsById: 204 または 404" {
            val cbId = "11111111-1111-1111-1111-111111111111"
            val cardId = "22222222-2222-2222-2222-222222222222"
            val missingCardId = "33333333-3333-3333-3333-333333333333"
            every {
                cardbookCardService.getByCardIdForUser(cardId, "u1")
            } returns cardModel(id = cardId, cardbookId = cbId)
            every { cardbookCardService.getByCardIdForUser(missingCardId, "u1") } returns null

            controller.deleteCardbookCardsById(UUID.fromString(cbId), UUID.fromString(cardId))
                .statusCode shouldBe HttpStatus.NO_CONTENT
            controller.deleteCardbookCardsById(UUID.fromString(cbId), UUID.fromString(missingCardId))
                .statusCode shouldBe HttpStatus.NOT_FOUND
        }

        "deleteCardbookCardsById: cardbookId が一致しなければ 404" {
            val cardId = "22222222-2222-2222-2222-222222222222"
            every {
                cardbookCardService.getByCardIdForUser(cardId, "u1")
            } returns cardModel(id = cardId, cardbookId = "99999999-9999-9999-9999-999999999999")

            val res = controller.deleteCardbookCardsById(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                UUID.fromString(cardId),
            )

            res.statusCode shouldBe HttpStatus.NOT_FOUND
        }

        // --- CardbookQuizHistory ---

        "getCardbookQuizHistories: 一覧を返す（cardbookId フィルタなし）" {
            every { cardbookQuizHistoryService.listByUser("u1", null) } returns listOf(quizHistoryModel())

            val res = controller.getCardbookQuizHistories(cardbookId = null)

            res.statusCode shouldBe HttpStatus.OK
            res.body!!.shouldHaveSize(1)
            res.body!![0].correctCount shouldBe 1
        }

        "getCardbookQuizHistories: cardbookId フィルタあり" {
            val cbId = UUID.fromString("11111111-1111-1111-1111-111111111111")
            every { cardbookQuizHistoryService.listByUser("u1", cbId.toString()) } returns listOf(quizHistoryModel())

            val res = controller.getCardbookQuizHistories(cardbookId = cbId)

            res.statusCode shouldBe HttpStatus.OK
            res.body!!.shouldHaveSize(1)
        }

        "postCardbookQuizHistories: 新規作成で 201" {
            every { cardbookQuizHistoryService.create(any()) } returns "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"
            val req = CardbookQuizHistoryBase(
                cardbookId = UUID.fromString("11111111-1111-1111-1111-111111111111"),
                questionCount = 1,
                direction = CardbookQuizHistoryBase.Direction.FRONT_TO_BACK,
                answers = listOf(
                    CardbookQuizHistoryAnswer(
                        cardId = UUID.fromString("22222222-2222-2222-2222-222222222222"),
                        result = CardbookQuizHistoryAnswer.Result.CORRECT,
                    ),
                ),
            )

            val res = controller.postCardbookQuizHistories(req)

            res.statusCode shouldBe HttpStatus.CREATED
            res.body!!.quizHistoryId.toString() shouldBe "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"
        }

        "deleteCardbookQuizHistoriesById: 204 または 404" {
            val id = "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"
            val missingId = "cccccccc-cccc-cccc-cccc-cccccccccccc"
            every {
                cardbookQuizHistoryService.getByQuizHistoryIdForUser(id, "u1")
            } returns quizHistoryModel(id = id)
            every { cardbookQuizHistoryService.getByQuizHistoryIdForUser(missingId, "u1") } returns null

            controller.deleteCardbookQuizHistoriesById(UUID.fromString(id)).statusCode shouldBe HttpStatus.NO_CONTENT
            controller
                .deleteCardbookQuizHistoriesById(UUID.fromString(missingId))
                .statusCode shouldBe HttpStatus.NOT_FOUND
        }
    })
