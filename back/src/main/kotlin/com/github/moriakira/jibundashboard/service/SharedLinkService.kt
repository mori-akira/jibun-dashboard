package com.github.moriakira.jibundashboard.service

import com.github.moriakira.jibundashboard.exception.ForbiddenException
import com.github.moriakira.jibundashboard.exception.GoneException
import com.github.moriakira.jibundashboard.repository.SharedLinkItem
import com.github.moriakira.jibundashboard.repository.SharedLinkRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.util.UUID

@Service
class SharedLinkService(
    private val sharedLinkRepository: SharedLinkRepository,
    @param:Value("\${app.share.base-url}") private val baseUrl: String,
) {
    fun create(userId: String, dataTypes: List<String>, expiresAt: String): SharedLinkModel {
        val token = UUID.randomUUID().toString()
        val item = SharedLinkItem().also {
            it.token = token
            it.userId = userId
            it.dataTypes = dataTypes
            it.expiresAt = expiresAt
        }
        sharedLinkRepository.put(item)
        return item.toDomain()
    }

    fun listByUser(userId: String): List<SharedLinkModel> =
        sharedLinkRepository.findByUserId(userId).map { it.toDomain() }

    fun delete(token: String, userId: String) {
        val item = sharedLinkRepository.getByToken(token) ?: return
        if (item.userId != userId) throw ForbiddenException("Token does not belong to user.")
        sharedLinkRepository.delete(token)
    }

    @Suppress("ThrowsCount")
    fun validateAndGet(token: String, dataType: String): SharedLinkModel {
        val item = sharedLinkRepository.getByToken(token)
            ?: throw java.util.NoSuchElementException("Token not found.")
        if (LocalDate.now().isAfter(LocalDate.parse(item.expiresAt!!))) {
            throw GoneException("Token has expired.")
        }
        if (item.dataTypes?.contains(dataType) != true) {
            throw ForbiddenException("Data type '$dataType' is not included in this share link.")
        }
        return item.toDomain()
    }

    private fun SharedLinkItem.toDomain() = SharedLinkModel(
        token = this.token!!,
        userId = this.userId!!,
        dataTypes = this.dataTypes ?: emptyList(),
        expiresAt = this.expiresAt!!,
        shareUrl = "$baseUrl/share/${this.token}",
    )
}

data class SharedLinkModel(
    val token: String,
    val userId: String,
    val dataTypes: List<String>,
    val expiresAt: String,
    val shareUrl: String,
)
