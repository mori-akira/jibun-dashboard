package com.github.moriakira.jibundashboard.controller

import com.github.moriakira.jibundashboard.component.CurrentAuth
import com.github.moriakira.jibundashboard.generated.api.CardbookApi
import com.github.moriakira.jibundashboard.generated.model.Cardbook
import com.github.moriakira.jibundashboard.generated.model.CardbookBase
import com.github.moriakira.jibundashboard.generated.model.CardbookCard
import com.github.moriakira.jibundashboard.generated.model.CardbookCardBase
import com.github.moriakira.jibundashboard.generated.model.CardbookCardId
import com.github.moriakira.jibundashboard.generated.model.CardbookCheckResult
import com.github.moriakira.jibundashboard.generated.model.CardbookCheckResultStatus
import com.github.moriakira.jibundashboard.generated.model.CardbookId
import com.github.moriakira.jibundashboard.generated.model.CardbookQuizHistory
import com.github.moriakira.jibundashboard.generated.model.CardbookQuizHistoryAnswer
import com.github.moriakira.jibundashboard.generated.model.CardbookQuizHistoryBase
import com.github.moriakira.jibundashboard.generated.model.CardbookQuizHistoryId
import com.github.moriakira.jibundashboard.service.CardbookCardModel
import com.github.moriakira.jibundashboard.service.CardbookCardService
import com.github.moriakira.jibundashboard.service.CardbookCheckResultModel
import com.github.moriakira.jibundashboard.service.CardbookCheckResultService
import com.github.moriakira.jibundashboard.service.CardbookModel
import com.github.moriakira.jibundashboard.service.CardbookQuizHistoryAnswerModel
import com.github.moriakira.jibundashboard.service.CardbookQuizHistoryModel
import com.github.moriakira.jibundashboard.service.CardbookQuizHistoryService
import com.github.moriakira.jibundashboard.service.CardbookService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

@Suppress("TooManyFunctions")
@RestController
class CardbookController(
    private val currentAuth: CurrentAuth,
    private val cardbookService: CardbookService,
    private val cardbookCardService: CardbookCardService,
    private val cardbookQuizHistoryService: CardbookQuizHistoryService,
    private val cardbookCheckResultService: CardbookCheckResultService,
) : CardbookApi {

    override fun getCardbooks(): ResponseEntity<List<Cardbook>> {
        val list = cardbookService.listByUser(currentAuth.userId)
        return ResponseEntity.ok(list.map { it.toApi() })
    }

    override fun postCardbooks(cardbookBase: CardbookBase?): ResponseEntity<CardbookId> {
        requireNotNull(cardbookBase) { "Request body is required." }
        val cardbookId = cardbookService.create(cardbookBase.toModel())
        return ResponseEntity.status(HttpStatus.CREATED).body(CardbookId(UUID.fromString(cardbookId)))
    }

    @Suppress("ReturnCount")
    override fun getCardbooksById(cardbookId: UUID): ResponseEntity<Cardbook> {
        val model =
            cardbookService.getByCardbookIdForUser(cardbookId.toString(), currentAuth.userId)
                ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(model.toApi())
    }

    @Suppress("ReturnCount")
    override fun putCardbooksById(cardbookId: UUID, cardbookBase: CardbookBase?): ResponseEntity<CardbookId> {
        requireNotNull(cardbookBase) { "Request body is required." }
        cardbookService.getByCardbookIdForUser(cardbookId.toString(), currentAuth.userId)
            ?: return ResponseEntity.notFound().build()
        cardbookService.put(cardbookBase.toModel(cardbookId.toString()))
        return ResponseEntity.ok(CardbookId(cardbookId))
    }

    @Suppress("ReturnCount")
    override fun deleteCardbooksById(cardbookId: UUID): ResponseEntity<Unit> {
        cardbookService.getByCardbookIdForUser(cardbookId.toString(), currentAuth.userId)
            ?: return ResponseEntity.notFound().build()
        cardbookService.delete(currentAuth.userId, cardbookId.toString())
        return ResponseEntity.noContent().build()
    }

    @Suppress("ReturnCount")
    override fun getCardbookCards(cardbookId: UUID): ResponseEntity<List<CardbookCard>> {
        cardbookService.getByCardbookIdForUser(cardbookId.toString(), currentAuth.userId)
            ?: return ResponseEntity.notFound().build()
        val list = cardbookCardService.listByCardbookId(cardbookId.toString())
        return ResponseEntity.ok(list.map { it.toApi() })
    }

    @Suppress("ReturnCount")
    override fun postCardbookCards(
        cardbookId: UUID,
        cardbookCardBase: CardbookCardBase?,
    ): ResponseEntity<CardbookCardId> {
        requireNotNull(cardbookCardBase) { "Request body is required." }
        cardbookService.getByCardbookIdForUser(cardbookId.toString(), currentAuth.userId)
            ?: return ResponseEntity.notFound().build()
        val cardId = cardbookCardService.create(cardbookCardBase.toModel(cardbookId.toString()))
        return ResponseEntity.status(HttpStatus.CREATED).body(CardbookCardId(UUID.fromString(cardId)))
    }

    @Suppress("ReturnCount")
    override fun putCardbookCardsById(
        cardbookId: UUID,
        cardId: UUID,
        cardbookCardBase: CardbookCardBase?,
    ): ResponseEntity<CardbookCardId> {
        requireNotNull(cardbookCardBase) { "Request body is required." }
        val card =
            cardbookCardService.getByCardIdForUser(cardId.toString(), currentAuth.userId)
                ?: return ResponseEntity.notFound().build()
        if (card.cardbookId != cardbookId.toString()) return ResponseEntity.notFound().build()
        cardbookCardService.put(cardbookCardBase.toModel(cardbookId.toString(), cardId.toString()))
        return ResponseEntity.ok(CardbookCardId(cardId))
    }

    @Suppress("ReturnCount")
    override fun deleteCardbookCardsById(cardbookId: UUID, cardId: UUID): ResponseEntity<Unit> {
        val card =
            cardbookCardService.getByCardIdForUser(cardId.toString(), currentAuth.userId)
                ?: return ResponseEntity.notFound().build()
        if (card.cardbookId != cardbookId.toString()) return ResponseEntity.notFound().build()
        cardbookCardService.delete(currentAuth.userId, cardId.toString())
        return ResponseEntity.noContent().build()
    }

    override fun getCardbookQuizHistories(cardbookId: UUID?): ResponseEntity<List<CardbookQuizHistory>> {
        val list = cardbookQuizHistoryService.listByUser(currentAuth.userId, cardbookId?.toString())
        return ResponseEntity.ok(list.map { it.toApi() })
    }

    override fun postCardbookQuizHistories(
        cardbookQuizHistoryBase: CardbookQuizHistoryBase?,
    ): ResponseEntity<CardbookQuizHistoryId> {
        requireNotNull(cardbookQuizHistoryBase) { "Request body is required." }
        val quizHistoryId = cardbookQuizHistoryService.create(cardbookQuizHistoryBase.toModel())
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(CardbookQuizHistoryId(UUID.fromString(quizHistoryId)))
    }

    @Suppress("ReturnCount")
    override fun deleteCardbookQuizHistoriesById(quizHistoryId: UUID): ResponseEntity<Unit> {
        cardbookQuizHistoryService.getByQuizHistoryIdForUser(quizHistoryId.toString(), currentAuth.userId)
            ?: return ResponseEntity.notFound().build()
        cardbookQuizHistoryService.delete(currentAuth.userId, quizHistoryId.toString())
        return ResponseEntity.noContent().build()
    }

    override fun getCardbookCheckResults(
        cardbookId: UUID?,
        checkedAtFrom: LocalDate?,
        checkedAtTo: LocalDate?,
        severities: List<String>?,
        statuses: List<String>?,
    ): ResponseEntity<List<CardbookCheckResult>> {
        val list = cardbookCheckResultService.listByUser(
            userId = currentAuth.userId,
            cardbookId = cardbookId?.toString(),
            checkedAtFrom = checkedAtFrom?.toString(),
            checkedAtTo = checkedAtTo?.toString(),
            severities = severities,
            statuses = statuses,
        )
        return ResponseEntity.ok(list.map { it.toApi() })
    }

    @Suppress("ReturnCount")
    override fun putCardbookCheckResultStatusById(
        checkResultId: UUID,
        cardbookCheckResultStatus: CardbookCheckResultStatus?,
    ): ResponseEntity<Unit> {
        requireNotNull(cardbookCheckResultStatus) { "Request body is required." }
        cardbookCheckResultService.updateStatus(
            checkResultId.toString(),
            currentAuth.userId,
            cardbookCheckResultStatus.status.value,
        ) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.noContent().build()
    }

    private fun CardbookCheckResultModel.toApi(): CardbookCheckResult =
        CardbookCheckResult(
            cardbookCheckResultId = UUID.fromString(this.cardbookCheckResultId),
            cardbookId = UUID.fromString(this.cardbookId),
            cardId = UUID.fromString(this.cardId),
            front = this.front,
            severity = CardbookCheckResult.Severity.valueOf(this.severity),
            status = CardbookCheckResult.Status.valueOf(this.status),
            report = this.report,
            cardUpdatedAt = OffsetDateTime.parse(this.cardUpdatedAt),
            checkedAt = OffsetDateTime.parse(this.checkedAt),
        )

    private fun CardbookModel.toApi(): Cardbook =
        Cardbook(
            cardbookId = UUID.fromString(this.cardbookId),
            name = this.name,
            createdDateTime = OffsetDateTime.parse(this.createdDateTime),
            updatedDateTime = OffsetDateTime.parse(this.updatedDateTime),
        )

    private fun CardbookBase.toModel(): CardbookModel =
        CardbookModel(
            cardbookId = "",
            userId = currentAuth.userId,
            name = this.name,
        )

    private fun CardbookBase.toModel(cardbookId: String): CardbookModel =
        CardbookModel(
            cardbookId = cardbookId,
            userId = currentAuth.userId,
            name = this.name,
        )

    private fun CardbookCardModel.toApi(): CardbookCard =
        CardbookCard(
            cardId = UUID.fromString(this.cardId),
            cardbookId = UUID.fromString(this.cardbookId),
            front = this.front,
            back = this.back,
            createdDateTime = OffsetDateTime.parse(this.createdDateTime),
            updatedDateTime = OffsetDateTime.parse(this.updatedDateTime),
        )

    private fun CardbookCardBase.toModel(cardbookId: String): CardbookCardModel =
        CardbookCardModel(
            cardId = "",
            cardbookId = cardbookId,
            userId = currentAuth.userId,
            front = this.front,
            back = this.back,
        )

    private fun CardbookCardBase.toModel(cardbookId: String, cardId: String): CardbookCardModel =
        CardbookCardModel(
            cardId = cardId,
            cardbookId = cardbookId,
            userId = currentAuth.userId,
            front = this.front,
            back = this.back,
        )

    private fun CardbookQuizHistoryModel.toApi(): CardbookQuizHistory =
        CardbookQuizHistory(
            quizHistoryId = UUID.fromString(this.quizHistoryId),
            cardbookId = UUID.fromString(this.cardbookId),
            answeredAt = OffsetDateTime.parse(this.answeredAt),
            questionCount = this.questionCount,
            direction = CardbookQuizHistory.Direction.valueOf(this.direction),
            correctCount = this.correctCount,
            incorrectCount = this.incorrectCount,
            answers = this.answers.map { it.toApi() },
        )

    private fun CardbookQuizHistoryAnswerModel.toApi(): CardbookQuizHistoryAnswer =
        CardbookQuizHistoryAnswer(
            cardId = UUID.fromString(this.cardId),
            result = CardbookQuizHistoryAnswer.Result.valueOf(this.result),
        )

    private fun CardbookQuizHistoryBase.toModel(): CardbookQuizHistoryModel =
        CardbookQuizHistoryModel(
            quizHistoryId = "",
            userId = currentAuth.userId,
            cardbookId = this.cardbookId.toString(),
            answeredAt = "",
            questionCount = this.questionCount,
            direction = this.direction.value,
            correctCount = 0,
            incorrectCount = 0,
            answers = this.answers.map { it.toModel() },
        )

    private fun CardbookQuizHistoryAnswer.toModel(): CardbookQuizHistoryAnswerModel =
        CardbookQuizHistoryAnswerModel(
            cardId = this.cardId.toString(),
            result = this.result.value,
        )
}
