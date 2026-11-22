package com.github.moriakira.jibundashboard.mapper

import com.github.moriakira.jibundashboard.generated.model.PostVocabulariesRequest
import com.github.moriakira.jibundashboard.generated.model.Vocabulary
import com.github.moriakira.jibundashboard.generated.model.VocabularyCheckResult
import com.github.moriakira.jibundashboard.generated.model.VocabularyQuizHistory
import com.github.moriakira.jibundashboard.generated.model.VocabularyQuizHistoryAnswer
import com.github.moriakira.jibundashboard.generated.model.VocabularyQuizHistoryBase
import com.github.moriakira.jibundashboard.generated.model.VocabularyTag
import com.github.moriakira.jibundashboard.generated.model.VocabularyTagBase
import com.github.moriakira.jibundashboard.service.VocabularyCheckResultModel
import com.github.moriakira.jibundashboard.service.VocabularyModel
import com.github.moriakira.jibundashboard.service.VocabularyQuizHistoryAnswerModel
import com.github.moriakira.jibundashboard.service.VocabularyQuizHistoryModel
import com.github.moriakira.jibundashboard.service.VocabularyTagModel
import java.time.OffsetDateTime
import java.util.UUID

fun VocabularyModel.toApi(): Vocabulary = Vocabulary(
    vocabularyId = UUID.fromString(this.vocabularyId),
    name = this.name,
    description = this.description,
    tags = this.tags.map { it.toApi() }.toSet(),
    createdDateTime = OffsetDateTime.parse(this.createdDateTime),
    updatedDateTime = OffsetDateTime.parse(this.updatedDateTime),
)

fun PostVocabulariesRequest.toModel(userId: String, vocabularyId: String = ""): VocabularyModel = VocabularyModel(
    vocabularyId = vocabularyId,
    userId = userId,
    name = this.name,
    description = this.description,
    tagIds = this.tagIds?.map { it.toString() } ?: emptyList(),
)

fun VocabularyTagModel.toApi(): VocabularyTag = VocabularyTag(
    vocabularyTagId = UUID.fromString(this.vocabularyTagId),
    vocabularyTag = this.vocabularyTag,
    order = this.order,
)

fun VocabularyTagBase.toModel(userId: String, vocabularyTagId: String = ""): VocabularyTagModel = VocabularyTagModel(
    vocabularyTagId = vocabularyTagId,
    userId = userId,
    vocabularyTag = this.vocabularyTag,
    order = this.order,
)

fun VocabularyQuizHistoryModel.toApi(): VocabularyQuizHistory = VocabularyQuizHistory(
    quizHistoryId = UUID.fromString(this.quizHistoryId),
    answeredAt = OffsetDateTime.parse(this.answeredAt),
    tagIds = this.tagIds.map { UUID.fromString(it) },
    questionCount = this.questionCount,
    direction = VocabularyQuizHistory.Direction.valueOf(this.direction),
    correctCount = this.correctCount,
    incorrectCount = this.incorrectCount,
    answers = this.answers.map { it.toApi() },
)

fun VocabularyQuizHistoryAnswerModel.toApi(): VocabularyQuizHistoryAnswer = VocabularyQuizHistoryAnswer(
    vocabularyId = UUID.fromString(this.vocabularyId),
    result = VocabularyQuizHistoryAnswer.Result.valueOf(this.result),
)

fun VocabularyQuizHistoryBase.toModel(userId: String): VocabularyQuizHistoryModel = VocabularyQuizHistoryModel(
    quizHistoryId = "",
    userId = userId,
    answeredAt = "",
    tagIds = this.tagIds?.map { it.toString() } ?: emptyList(),
    questionCount = this.questionCount,
    direction = this.direction.value,
    correctCount = 0,
    incorrectCount = 0,
    answers = this.answers.map { it.toModel() },
)

fun VocabularyQuizHistoryAnswer.toModel(): VocabularyQuizHistoryAnswerModel = VocabularyQuizHistoryAnswerModel(
    vocabularyId = this.vocabularyId.toString(),
    result = this.result.value,
)

fun VocabularyCheckResultModel.toApi(): VocabularyCheckResult = VocabularyCheckResult(
    vocabularyCheckResultId = UUID.fromString(this.vocabularyCheckResultId),
    vocabularyId = UUID.fromString(this.vocabularyId),
    vocabularyName = this.vocabularyName,
    severity = VocabularyCheckResult.Severity.valueOf(this.severity),
    status = VocabularyCheckResult.Status.valueOf(this.status),
    report = this.report,
    vocabularyUpdatedAt = OffsetDateTime.parse(this.vocabularyUpdatedAt),
    checkedAt = OffsetDateTime.parse(this.checkedAt),
)
