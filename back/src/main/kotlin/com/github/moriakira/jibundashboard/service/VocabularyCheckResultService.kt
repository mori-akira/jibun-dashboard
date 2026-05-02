package com.github.moriakira.jibundashboard.service

import com.github.moriakira.jibundashboard.repository.VocabularyCheckResultItem
import com.github.moriakira.jibundashboard.repository.VocabularyCheckResultRepository
import org.springframework.stereotype.Service

@Service
class VocabularyCheckResultService(
    private val repository: VocabularyCheckResultRepository,
) {
    fun listByUser(userId: String): List<VocabularyCheckResultModel> =
        repository.findByUser(userId)
            .map { it.toDomain() }
            .sortedByDescending { it.checkedAt }

    fun updateStatus(checkResultId: String, userId: String, status: String): VocabularyCheckResultModel? {
        val item = repository.getByCheckResultId(checkResultId) ?: return null
        if (item.userId != userId) return null
        item.status = status
        repository.put(item)
        return item.toDomain()
    }

    private fun VocabularyCheckResultItem.toDomain(): VocabularyCheckResultModel =
        VocabularyCheckResultModel(
            vocabularyCheckResultId = this.vocabularyCheckResultId!!,
            vocabularyId = this.vocabularyId!!,
            userId = this.userId!!,
            vocabularyName = this.vocabularyName!!,
            severity = this.severity!!,
            status = this.status!!,
            report = this.report!!,
            vocabularyUpdatedAt = this.vocabularyUpdatedAt!!,
            checkedAt = this.checkedAt!!,
        )
}

data class VocabularyCheckResultModel(
    val vocabularyCheckResultId: String,
    val vocabularyId: String,
    val userId: String,
    val vocabularyName: String,
    val severity: String,
    val status: String,
    val report: String,
    val vocabularyUpdatedAt: String,
    val checkedAt: String,
)
