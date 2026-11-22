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
import com.github.moriakira.jibundashboard.generated.model.CardbookQuizHistoryBase
import com.github.moriakira.jibundashboard.generated.model.CardbookQuizHistoryId
import com.github.moriakira.jibundashboard.mapper.toApi
import com.github.moriakira.jibundashboard.mapper.toModel
import com.github.moriakira.jibundashboard.service.CardbookCardService
import com.github.moriakira.jibundashboard.service.CardbookCheckResultService
import com.github.moriakira.jibundashboard.service.CardbookQuizHistoryService
import com.github.moriakira.jibundashboard.service.CardbookService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
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
        val cardbookId = cardbookService.create(cardbookBase.toModel(currentAuth.userId))
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
        cardbookService.put(cardbookBase.toModel(currentAuth.userId, cardbookId.toString()))
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
        val cardId = cardbookCardService.create(cardbookCardBase.toModel(currentAuth.userId, cardbookId.toString()))
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
        cardbookCardService.put(cardbookCardBase.toModel(currentAuth.userId, cardbookId.toString(), cardId.toString()))
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
        val quizHistoryId = cardbookQuizHistoryService.create(cardbookQuizHistoryBase.toModel(currentAuth.userId))
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
}
