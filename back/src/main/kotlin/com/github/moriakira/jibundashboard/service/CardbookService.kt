package com.github.moriakira.jibundashboard.service

import com.github.moriakira.jibundashboard.repository.CardbookItem
import com.github.moriakira.jibundashboard.repository.CardbookRepository
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.util.UUID

@Service
class CardbookService(
    private val repository: CardbookRepository,
    private val cardbookCardService: CardbookCardService,
    private val cardbookQuizHistoryService: CardbookQuizHistoryService,
) {
    fun listByUser(userId: String): List<CardbookModel> =
        repository.findByUser(userId).map { it.toDomain() }.sortedByDescending { it.updatedDateTime }

    fun getByCardbookIdForUser(cardbookId: String, userId: String): CardbookModel? =
        repository.getByUserAndCardbookId(userId, cardbookId)?.toDomain()?.takeIf { it.userId == userId }

    fun create(model: CardbookModel): String = put(model.copy(cardbookId = UUID.randomUUID().toString()))

    fun put(model: CardbookModel): String {
        val existing = repository.getByUserAndCardbookId(model.userId, model.cardbookId)?.toDomain()
        val now = OffsetDateTime.now().toString()
        val toSave =
            model.copy(
                createdDateTime = existing?.createdDateTime ?: now,
                updatedDateTime = now,
            )
        repository.put(toSave.toItem())
        return model.cardbookId
    }

    // 子を先に消すことで、途中で失敗してもカードブックが残り再実行できる
    fun delete(userId: String, cardbookId: String) {
        cardbookCardService.deleteByCardbookId(userId, cardbookId)
        cardbookQuizHistoryService.deleteByCardbookId(userId, cardbookId)
        repository.delete(userId, cardbookId)
    }

    private fun CardbookItem.toDomain(): CardbookModel = CardbookModel(
        cardbookId = this.cardbookId!!,
        userId = this.userId!!,
        name = this.name!!,
        createdDateTime = this.createdDateTime ?: "",
        updatedDateTime = this.updatedDateTime ?: "",
    )

    private fun CardbookModel.toItem(): CardbookItem = CardbookItem().also { item ->
        item.cardbookId = this.cardbookId
        item.userId = this.userId
        item.name = this.name
        item.createdDateTime = this.createdDateTime
        item.updatedDateTime = this.updatedDateTime
    }
}

data class CardbookModel(
    val cardbookId: String,
    val userId: String,
    val name: String,
    val createdDateTime: String = "",
    val updatedDateTime: String = "",
)
