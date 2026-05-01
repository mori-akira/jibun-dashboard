package com.github.moriakira.jibundashboard.service

import com.github.moriakira.jibundashboard.repository.VocabularyQuizHistoryAnswerItem
import com.github.moriakira.jibundashboard.repository.VocabularyQuizHistoryItem
import com.github.moriakira.jibundashboard.repository.VocabularyQuizHistoryRepository
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.util.UUID

@Service
class VocabularyQuizHistoryService(
    private val repository: VocabularyQuizHistoryRepository,
) {
    fun listByUser(userId: String): List<VocabularyQuizHistoryModel> =
        repository.findByUser(userId).map { it.toDomain() }

    fun getByQuizHistoryIdForUser(quizHistoryId: String, userId: String): VocabularyQuizHistoryModel? =
        repository.getByUserAndQuizHistoryId(userId, quizHistoryId)?.toDomain()

    fun create(model: VocabularyQuizHistoryModel): String {
        val quizHistoryId = UUID.randomUUID().toString()
        val correctCount = model.answers.count { it.result == "CORRECT" }
        val incorrectCount = model.answers.count { it.result == "INCORRECT" }
        val toSave =
            model.copy(
                quizHistoryId = quizHistoryId,
                answeredAt = OffsetDateTime.now().toString(),
                correctCount = correctCount,
                incorrectCount = incorrectCount,
            )
        repository.put(toSave.toItem())
        return quizHistoryId
    }

    fun delete(userId: String, quizHistoryId: String) {
        repository.delete(userId, quizHistoryId)
    }

    private fun VocabularyQuizHistoryItem.toDomain(): VocabularyQuizHistoryModel =
        VocabularyQuizHistoryModel(
            quizHistoryId = this.quizHistoryId!!,
            userId = this.userId!!,
            answeredAt = this.answeredAt!!,
            tagIds = this.tagIds ?: emptyList(),
            questionCount = this.questionCount!!,
            direction = this.direction!!,
            correctCount = this.correctCount!!,
            incorrectCount = this.incorrectCount!!,
            answers = (this.answers ?: emptyList()).map { it.toDomain() },
        )

    private fun VocabularyQuizHistoryAnswerItem.toDomain(): VocabularyQuizHistoryAnswerModel =
        VocabularyQuizHistoryAnswerModel(
            vocabularyId = this.vocabularyId!!,
            result = this.result!!,
        )

    private fun VocabularyQuizHistoryModel.toItem(): VocabularyQuizHistoryItem =
        VocabularyQuizHistoryItem().also { item ->
            item.quizHistoryId = this.quizHistoryId
            item.userId = this.userId
            item.answeredAt = this.answeredAt
            item.tagIds = this.tagIds
            item.questionCount = this.questionCount
            item.direction = this.direction
            item.correctCount = this.correctCount
            item.incorrectCount = this.incorrectCount
            item.answers = this.answers.map { it.toItem() }
        }

    private fun VocabularyQuizHistoryAnswerModel.toItem(): VocabularyQuizHistoryAnswerItem =
        VocabularyQuizHistoryAnswerItem().also { item ->
            item.vocabularyId = this.vocabularyId
            item.result = this.result
        }
}

data class VocabularyQuizHistoryModel(
    val quizHistoryId: String,
    val userId: String,
    val answeredAt: String,
    val tagIds: List<String>,
    val questionCount: Int,
    val direction: String,
    val correctCount: Int,
    val incorrectCount: Int,
    val answers: List<VocabularyQuizHistoryAnswerModel>,
)

data class VocabularyQuizHistoryAnswerModel(
    val vocabularyId: String,
    val result: String,
)
