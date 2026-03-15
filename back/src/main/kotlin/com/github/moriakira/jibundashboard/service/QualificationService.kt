package com.github.moriakira.jibundashboard.service

import com.github.moriakira.jibundashboard.repository.QualificationItem
import com.github.moriakira.jibundashboard.repository.QualificationRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class QualificationService(
    private val qualificationRepository: QualificationRepository,
    private val userAssetService: UserAssetService,
) {
    companion object {
        private const val CERTIFICATION_ASSET_TYPE = "qualification-certifications"
    }
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

    fun put(model: QualificationModel): String {
        val existing = qualificationRepository.getByQualificationId(model.qualificationId)?.toDomain()
        if (model.certificationAssetId != existing?.certificationAssetId) {
            val newId = model.certificationAssetId?.let { UUID.fromString(it) }
            val oldId = existing?.certificationAssetId?.let { UUID.fromString(it) }
            when {
                newId != null -> userAssetService.copyFromUploads(CERTIFICATION_ASSET_TYPE, model.userId, newId)
                oldId != null -> userAssetService.delete(CERTIFICATION_ASSET_TYPE, model.userId, oldId)
            }
        }
        qualificationRepository.put(model.toItem())
        return model.qualificationId
    }

    fun delete(userId: String, qualificationId: String) {
        qualificationRepository.delete(userId, qualificationId)
    }

    private fun QualificationItem.toDomain(): QualificationModel = QualificationModel(
        qualificationId = this.qualificationId!!,
        userId = this.userId!!,
        order = this.order!!,
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
        certificationAssetId = this.certificationAssetId,
    )

    private fun QualificationModel.toItem(): QualificationItem = QualificationItem().also { item ->
        item.qualificationId = this.qualificationId
        item.userId = this.userId
        item.order = this.order
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
        item.certificationAssetId = this.certificationAssetId
    }
}

data class QualificationModel(
    val qualificationId: String,
    val userId: String,
    val order: Int,
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
    val certificationAssetId: String?,
)
