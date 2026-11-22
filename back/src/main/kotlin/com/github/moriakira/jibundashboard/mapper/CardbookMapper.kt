package com.github.moriakira.jibundashboard.mapper

import com.github.moriakira.jibundashboard.generated.model.Cardbook
import com.github.moriakira.jibundashboard.generated.model.CardbookBase
import com.github.moriakira.jibundashboard.generated.model.CardbookCard
import com.github.moriakira.jibundashboard.generated.model.CardbookCardBase
import com.github.moriakira.jibundashboard.generated.model.CardbookCheckResult
import com.github.moriakira.jibundashboard.generated.model.CardbookQuizHistory
import com.github.moriakira.jibundashboard.generated.model.CardbookQuizHistoryAnswer
import com.github.moriakira.jibundashboard.generated.model.CardbookQuizHistoryBase
import com.github.moriakira.jibundashboard.service.CardbookCardModel
import com.github.moriakira.jibundashboard.service.CardbookCheckResultModel
import com.github.moriakira.jibundashboard.service.CardbookModel
import com.github.moriakira.jibundashboard.service.CardbookQuizHistoryAnswerModel
import com.github.moriakira.jibundashboard.service.CardbookQuizHistoryModel
import java.time.OffsetDateTime
import java.util.UUID

fun CardbookModel.toApi(): Cardbook = Cardbook(
    cardbookId = UUID.fromString(this.cardbookId),
    name = this.name,
    createdDateTime = OffsetDateTime.parse(this.createdDateTime),
    updatedDateTime = OffsetDateTime.parse(this.updatedDateTime),
)

fun CardbookBase.toModel(userId: String, cardbookId: String = ""): CardbookModel = CardbookModel(
    cardbookId = cardbookId,
    userId = userId,
    name = this.name,
)

fun CardbookCardModel.toApi(): CardbookCard = CardbookCard(
    cardId = UUID.fromString(this.cardId),
    cardbookId = UUID.fromString(this.cardbookId),
    front = this.front,
    back = this.back,
    createdDateTime = OffsetDateTime.parse(this.createdDateTime),
    updatedDateTime = OffsetDateTime.parse(this.updatedDateTime),
)

fun CardbookCardBase.toModel(userId: String, cardbookId: String, cardId: String = ""): CardbookCardModel =
    CardbookCardModel(
        cardId = cardId,
        cardbookId = cardbookId,
        userId = userId,
        front = this.front,
        back = this.back,
    )

fun CardbookQuizHistoryModel.toApi(): CardbookQuizHistory = CardbookQuizHistory(
    quizHistoryId = UUID.fromString(this.quizHistoryId),
    cardbookId = UUID.fromString(this.cardbookId),
    answeredAt = OffsetDateTime.parse(this.answeredAt),
    questionCount = this.questionCount,
    direction = CardbookQuizHistory.Direction.valueOf(this.direction),
    correctCount = this.correctCount,
    incorrectCount = this.incorrectCount,
    answers = this.answers.map { it.toApi() },
)

fun CardbookQuizHistoryAnswerModel.toApi(): CardbookQuizHistoryAnswer = CardbookQuizHistoryAnswer(
    cardId = UUID.fromString(this.cardId),
    result = CardbookQuizHistoryAnswer.Result.valueOf(this.result),
)

fun CardbookQuizHistoryBase.toModel(userId: String): CardbookQuizHistoryModel = CardbookQuizHistoryModel(
    quizHistoryId = "",
    userId = userId,
    cardbookId = this.cardbookId.toString(),
    answeredAt = "",
    questionCount = this.questionCount,
    direction = this.direction.value,
    correctCount = 0,
    incorrectCount = 0,
    answers = this.answers.map { it.toModel() },
)

fun CardbookQuizHistoryAnswer.toModel(): CardbookQuizHistoryAnswerModel = CardbookQuizHistoryAnswerModel(
    cardId = this.cardId.toString(),
    result = this.result.value,
)

fun CardbookCheckResultModel.toApi(): CardbookCheckResult = CardbookCheckResult(
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
