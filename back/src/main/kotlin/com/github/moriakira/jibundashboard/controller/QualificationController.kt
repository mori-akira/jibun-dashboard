package com.github.moriakira.jibundashboard.controller

import com.github.moriakira.jibundashboard.component.CurrentAuth
import com.github.moriakira.jibundashboard.generated.api.QualificationApi
import com.github.moriakira.jibundashboard.generated.model.Qualification
import com.github.moriakira.jibundashboard.generated.model.QualificationId
import com.github.moriakira.jibundashboard.service.QualificationModel
import com.github.moriakira.jibundashboard.service.QualificationService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import java.net.URI
import java.time.LocalDate
import java.util.UUID

class QualificationController(
    private val currentAuth: CurrentAuth,
    private val qualificationService: QualificationService,
) : QualificationApi {

    override fun getQualification(
        qualificationName: String?,
        status: List<String>?,
        rank: List<String>?,
        organization: String?,
        acquiredDateFrom: LocalDate?,
        acquiredDateTo: LocalDate?,
        expirationDateFrom: LocalDate?,
        expirationDateTo: LocalDate?,
    ): ResponseEntity<List<Qualification>> {
        val list = qualificationService.listByConditions(
            userId = currentAuth.userId,
            qualificationName = qualificationName,
            statuses = status,
            ranks = rank,
            organization = organization,
            acquiredDateFrom = acquiredDateFrom?.toString(),
            acquiredDateTo = acquiredDateTo?.toString(),
            expirationDateFrom = expirationDateFrom?.toString(),
            expirationDateTo = expirationDateTo?.toString(),
        )
        return ResponseEntity.ok(list.map { it.toApi() })
    }

    @Suppress("ReturnCount")
    override fun putQualification(qualification: Qualification?): ResponseEntity<QualificationId> {
        requireNotNull(qualification) { "Request body is required." }
        if (qualification.qualificationId != null) {
            val model = qualificationService.getByQualificationId(qualification.qualificationId.toString())
                ?: return ResponseEntity.notFound().build()
            if (model.userId != currentAuth.userId) return ResponseEntity.notFound().build()
        }
        val qualificationId = qualificationService.put(qualification.toModel())
        val status = if (qualification.qualificationId == null) HttpStatus.CREATED else HttpStatus.OK
        return ResponseEntity.status(status).body(QualificationId(qualificationId = UUID.fromString(qualificationId)))
    }

    @Suppress("ReturnCount")
    override fun getQualificationById(qualificationId: UUID): ResponseEntity<Qualification> {
        val model =
            qualificationService.getByQualificationId(qualificationId.toString()) ?: return ResponseEntity.notFound()
                .build()
        if (model.userId != currentAuth.userId) return ResponseEntity.notFound().build()
        return ResponseEntity.ok(model.toApi())
    }

    @Suppress("ReturnCount")
    override fun deleteQualification(qualificationId: UUID): ResponseEntity<Unit> {
        val model =
            qualificationService.getByQualificationId(qualificationId.toString()) ?: return ResponseEntity.noContent()
                .build()
        if (model.userId != currentAuth.userId) return ResponseEntity.noContent().build()
        qualificationService.deleteByQualificationId(currentAuth.userId, qualificationId.toString())
        return ResponseEntity.noContent().build()
    }

    private fun QualificationModel.toApi(): Qualification = Qualification(
        qualificationId = UUID.fromString(this.qualificationId),
        qualificationName = this.qualificationName,
        abbreviation = this.abbreviation,
        version = this.version,
        status = Qualification.Status.forValue(this.status),
        rank = Qualification.Rank.forValue(this.rank),
        organization = this.organization,
        acquiredDate = this.acquiredDate?.let { LocalDate.parse(it) },
        expirationDate = this.expirationDate?.let { LocalDate.parse(it) },
        officialUrl = URI.create(this.officialUrl),
        certificationUrl = this.certificationUrl?.let { URI.create(it) },
        badgeUrl = this.badgeUrl?.let { URI.create(it) },
    )

    private fun Qualification.toModel(): QualificationModel = QualificationModel(
        qualificationId = this.qualificationId?.toString() ?: UUID.randomUUID().toString(),
        userId = currentAuth.userId,
        qualificationName = this.qualificationName,
        abbreviation = this.abbreviation,
        version = this.version,
        status = this.status.value,
        rank = this.rank.value,
        organization = this.organization,
        acquiredDate = this.acquiredDate?.toString(),
        expirationDate = this.expirationDate?.toString(),
        officialUrl = this.officialUrl.toString(),
        certificationUrl = this.certificationUrl?.toString(),
        badgeUrl = this.badgeUrl?.toString(),
    )
}
