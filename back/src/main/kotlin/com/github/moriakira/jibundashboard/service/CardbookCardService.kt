package com.github.moriakira.jibundashboard.service

import com.github.moriakira.jibundashboard.repository.CardbookCardItem
import com.github.moriakira.jibundashboard.repository.CardbookCardRepository
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.util.UUID

@Service
class CardbookCardService(
    private val repository: CardbookCardRepository,
) {
    fun listByCardbookId(cardbookId: String): List<CardbookCardModel> =
        repository.findByCardbookId(cardbookId).map { it.toDomain() }.sortedBy { it.createdDateTime }

    fun getByCardIdForUser(cardId: String, userId: String): CardbookCardModel? =
        repository.getByUserAndCardId(userId, cardId)?.toDomain()?.takeIf { it.userId == userId }

    fun create(model: CardbookCardModel): String {
        val cardId = UUID.randomUUID().toString()
        val now = OffsetDateTime.now().toString()
        val toSave = model.copy(cardId = cardId, createdDateTime = now, updatedDateTime = now)
        repository.put(toSave.toItem())
        return cardId
    }

    fun put(model: CardbookCardModel) {
        val existing = repository.getByUserAndCardId(model.userId, model.cardId)?.toDomain()
        val now = OffsetDateTime.now().toString()
        val toSave = model.copy(createdDateTime = existing?.createdDateTime ?: now, updatedDateTime = now)
        repository.put(toSave.toItem())
    }

    fun delete(userId: String, cardId: String) {
        repository.delete(userId, cardId)
    }

    private fun CardbookCardItem.toDomain(): CardbookCardModel =
        CardbookCardModel(
            cardId = this.cardId!!,
            cardbookId = this.cardbookId!!,
            userId = this.userId!!,
            front = this.front!!,
            back = this.back,
            createdDateTime = this.createdDateTime ?: "",
            updatedDateTime = this.updatedDateTime ?: this.createdDateTime ?: "",
        )

    private fun CardbookCardModel.toItem(): CardbookCardItem =
        CardbookCardItem().also { item ->
            item.cardId = this.cardId
            item.cardbookId = this.cardbookId
            item.userId = this.userId
            item.front = this.front
            item.back = this.back
            item.createdDateTime = this.createdDateTime
            item.updatedDateTime = this.updatedDateTime
        }
}

data class CardbookCardModel(
    val cardId: String,
    val cardbookId: String,
    val userId: String,
    val front: String,
    val back: String?,
    val createdDateTime: String = "",
    val updatedDateTime: String = "",
)
