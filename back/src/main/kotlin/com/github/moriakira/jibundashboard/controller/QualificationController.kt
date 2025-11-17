package com.github.moriakira.jibundashboard.controller

import com.github.moriakira.jibundashboard.component.CurrentAuth
import com.github.moriakira.jibundashboard.generated.api.QualificationApi
import com.github.moriakira.jibundashboard.generated.model.Qualification
import com.github.moriakira.jibundashboard.generated.model.QualificationBase
import com.github.moriakira.jibundashboard.generated.model.QualificationId
import com.github.moriakira.jibundashboard.service.QualificationModel
import com.github.moriakira.jibundashboard.service.QualificationService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.net.URI
import java.time.LocalDate
import java.util.UUID

@RestController
class QualificationController(
    private val currentAuth: CurrentAuth,
    private val qualificationService: QualificationService,
) : QualificationApi {

    override fun getQualifications(
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
    override fun postQualification(
        qualificationBase: QualificationBase?,
    ): ResponseEntity<QualificationId> {
        // check
        requireNotNull(qualificationBase) { "Request body is required." }

        // execute
        val qualificationId = qualificationService.put(qualificationBase.toModel(UUID.randomUUID().toString()))
        return ResponseEntity.ok().body(QualificationId(UUID.fromString(qualificationId)))
    }

    @Suppress("ReturnCount")
    override fun getQualificationById(qualificationId: UUID): ResponseEntity<Qualification> {
        val model = qualificationService.getByQualificationId(qualificationId.toString())
            ?: return ResponseEntity.notFound().build()
        if (model.userId != currentAuth.userId) return ResponseEntity.notFound().build()
        return ResponseEntity.ok(model.toApi())
    }

    @Suppress("ReturnCount")
    override fun putQualification(
        qualificationId: UUID,
        qualificationBase: QualificationBase?,
    ): ResponseEntity<QualificationId> {
        // check
        requireNotNull(qualificationBase) { "Request body is required." }
        val model = qualificationService.getByQualificationId(qualificationId.toString())
            ?: return ResponseEntity.notFound().build()
        if (model.userId != currentAuth.userId) return ResponseEntity.notFound().build()

        // execute
        qualificationService.put(qualificationBase.toModel(qualificationId.toString()))
        return ResponseEntity.ok().body(QualificationId(qualificationId))
    }

    @Suppress("ReturnCount")
    override fun deleteQualification(qualificationId: UUID): ResponseEntity<Unit> {
        // check
        val model = qualificationService.getByQualificationId(qualificationId.toString())
            ?: return ResponseEntity.notFound().build()
        if (model.userId != currentAuth.userId) return ResponseEntity.notFound().build()

        // execute
        qualificationService.delete(currentAuth.userId, qualificationId.toString())
        return ResponseEntity.noContent().build()
    }

    private fun QualificationModel.toApi(): Qualification = Qualification(
        qualificationId = UUID.fromString(this.qualificationId),
        order = this.order,
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

    private fun QualificationBase.toModel(qualificationId: String): QualificationModel = QualificationModel(
        qualificationId = qualificationId,
        userId = currentAuth.userId,
        order = this.order,
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
