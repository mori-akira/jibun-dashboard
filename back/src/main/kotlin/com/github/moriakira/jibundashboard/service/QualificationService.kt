package com.github.moriakira.jibundashboard.service

import com.github.moriakira.jibundashboard.repository.QualificationItem
import com.github.moriakira.jibundashboard.repository.QualificationRepository
import org.springframework.stereotype.Service

@Service
class QualificationService(
    private val qualificationRepository: QualificationRepository,
) {
    fun listAll(userId: String) = qualificationRepository.query(userId).map { it.toDomain() }

    @Suppress("LongParameterList")
    fun listByConditions(
        userId: String,
        qualificationName: String? = null,
        statuses: List<String>? = null,
        ranks: List<String>? = null,
        acquiredDateFrom: String? = null,
        organization: String? = null,
        acquiredDateTo: String? = null,
        expirationDateFrom: String? = null,
        expirationDateTo: String? = null,
    ) = qualificationRepository.query(
        userId,
        qualificationName,
        statuses,
        ranks,
        acquiredDateFrom,
        organization,
        acquiredDateTo,
        expirationDateFrom,
        expirationDateTo,
    ).map { it.toDomain() }

    fun getByQualificationId(qualificationId: String): QualificationModel? =
        qualificationRepository.getByQualificationId(qualificationId)?.toDomain()

    fun deleteByQualificationId(userId: String, qualificationId: String) {
        qualificationRepository.delete(userId, qualificationId)
    }

    fun put(model: QualificationModel): String {
        qualificationRepository.put(model.toItem())
        return model.qualificationId
    }

    private fun QualificationItem.toDomain(): QualificationModel = QualificationModel(
        qualificationId = this.qualificationId!!,
        userId = this.userId!!,
        qualificationName = this.qualificationName!!,
        abbreviation = this.abbreviation,
        version = this.version,
        status = this.status!!,
        rank = this.rank!!,
        organization = this.organization!!,
        acquiredDate = this.acquiredDate,
        expirationDate = this.expirationDate,
        officialUrl = this.officialUrl!!,
        certificationUrl = this.certificationUrl,
        badgeUrl = this.badgeUrl,
    )

    private fun QualificationModel.toItem(): QualificationItem = QualificationItem().also { item ->
        item.qualificationId = this.qualificationId
        item.userId = this.userId
        item.qualificationName = this.qualificationName
        item.abbreviation = this.abbreviation
        item.version = this.version
        item.status = this.status
        item.rank = this.rank
        item.organization = this.organization
        item.acquiredDate = this.acquiredDate
        item.expirationDate = this.expirationDate
        item.officialUrl = this.officialUrl
        item.certificationUrl = this.certificationUrl
        item.badgeUrl = this.badgeUrl
    }
}

data class QualificationModel(
    val qualificationId: String,
    val userId: String,
    val qualificationName: String,
    val abbreviation: String?,
    val version: String?,
    val status: String,
    val rank: String,
    val organization: String,
    val acquiredDate: String?,
    val expirationDate: String?,
    val officialUrl: String,
    val certificationUrl: String?,
    val badgeUrl: String?,
)
