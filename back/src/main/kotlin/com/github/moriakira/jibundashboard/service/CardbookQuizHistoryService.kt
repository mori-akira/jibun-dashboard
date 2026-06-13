package com.github.moriakira.jibundashboard.service

import com.github.moriakira.jibundashboard.repository.CardbookQuizHistoryAnswerItem
import com.github.moriakira.jibundashboard.repository.CardbookQuizHistoryItem
import com.github.moriakira.jibundashboard.repository.CardbookQuizHistoryRepository
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.util.UUID

@Service
class CardbookQuizHistoryService(
    private val repository: CardbookQuizHistoryRepository,
) {
    fun listByUser(userId: String, cardbookId: String? = null): List<CardbookQuizHistoryModel> =
        repository.findByUser(userId)
            .filter { cardbookId == null || it.cardbookId == cardbookId }
            .map { it.toDomain() }

    fun getByQuizHistoryIdForUser(quizHistoryId: String, userId: String): CardbookQuizHistoryModel? =
        repository.getByUserAndQuizHistoryId(userId, quizHistoryId)?.toDomain()

    fun create(model: CardbookQuizHistoryModel): String {
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

    private fun CardbookQuizHistoryItem.toDomain(): CardbookQuizHistoryModel =
        CardbookQuizHistoryModel(
            quizHistoryId = this.quizHistoryId!!,
            userId = this.userId!!,
            cardbookId = this.cardbookId!!,
            answeredAt = this.answeredAt!!,
            questionCount = this.questionCount!!,
            direction = this.direction!!,
            correctCount = this.correctCount!!,
            incorrectCount = this.incorrectCount!!,
            answers = (this.answers ?: emptyList()).map { it.toDomain() },
        )

    private fun CardbookQuizHistoryAnswerItem.toDomain(): CardbookQuizHistoryAnswerModel =
        CardbookQuizHistoryAnswerModel(
            cardId = this.cardId!!,
            result = this.result!!,
        )

    private fun CardbookQuizHistoryModel.toItem(): CardbookQuizHistoryItem =
        CardbookQuizHistoryItem().also { item ->
            item.quizHistoryId = this.quizHistoryId
            item.userId = this.userId
            item.cardbookId = this.cardbookId
            item.answeredAt = this.answeredAt
            item.questionCount = this.questionCount
            item.direction = this.direction
            item.correctCount = this.correctCount
            item.incorrectCount = this.incorrectCount
            item.answers = this.answers.map { it.toItem() }
        }

    private fun CardbookQuizHistoryAnswerModel.toItem(): CardbookQuizHistoryAnswerItem =
        CardbookQuizHistoryAnswerItem().also { item ->
            item.cardId = this.cardId
            item.result = this.result
        }
}

data class CardbookQuizHistoryModel(
    val quizHistoryId: String,
    val userId: String,
    val cardbookId: String,
    val answeredAt: String,
    val questionCount: Int,
    val direction: String,
    val correctCount: Int,
    val incorrectCount: Int,
    val answers: List<CardbookQuizHistoryAnswerModel>,
)

data class CardbookQuizHistoryAnswerModel(
    val cardId: String,
    val result: String,
)
