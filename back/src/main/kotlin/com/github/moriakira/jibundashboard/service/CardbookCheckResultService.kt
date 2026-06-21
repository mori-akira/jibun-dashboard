package com.github.moriakira.jibundashboard.service

import com.github.moriakira.jibundashboard.repository.CardbookCheckResultItem
import com.github.moriakira.jibundashboard.repository.CardbookCheckResultRepository
import org.springframework.stereotype.Service

@Service
class CardbookCheckResultService(
    private val repository: CardbookCheckResultRepository,
) {
    @Suppress("LongParameterList")
    fun listByUser(
        userId: String,
        cardbookId: String? = null,
        checkedAtFrom: String? = null,
        checkedAtTo: String? = null,
        severities: List<String>? = null,
        statuses: List<String>? = null,
    ): List<CardbookCheckResultModel> =
        repository.findByUser(userId)
            .filter { it.severity != null }
            .filter { cardbookId == null || it.cardbookId == cardbookId }
            .filter { checkedAtFrom == null || it.checkedAt!!.substring(0, 10) >= checkedAtFrom }
            .filter { checkedAtTo == null || it.checkedAt!!.substring(0, 10) <= checkedAtTo }
            .filter { severities == null || it.severity in severities }
            .filter { statuses == null || it.status in statuses }
            .map { it.toDomain() }
            .sortedByDescending { it.checkedAt }

    @Suppress("ReturnCount")
    fun updateStatus(checkResultId: String, userId: String, status: String): CardbookCheckResultModel? {
        val item = repository.getByCheckResultId(checkResultId) ?: return null
        if (item.userId != userId) return null
        item.status = status
        repository.put(item)
        return item.toDomain()
    }

    private fun CardbookCheckResultItem.toDomain(): CardbookCheckResultModel =
        CardbookCheckResultModel(
            cardbookCheckResultId = this.cardbookCheckResultId!!,
            cardbookId = this.cardbookId!!,
            cardId = this.cardId!!,
            userId = this.userId!!,
            front = this.front!!,
            severity = this.severity!!,
            status = this.status!!,
            report = this.report!!,
            cardUpdatedAt = this.cardUpdatedAt!!,
            checkedAt = this.checkedAt!!,
        )
}

data class CardbookCheckResultModel(
    val cardbookCheckResultId: String,
    val cardbookId: String,
    val cardId: String,
    val userId: String,
    val front: String,
    val severity: String,
    val status: String,
    val report: String,
    val cardUpdatedAt: String,
    val checkedAt: String,
)
