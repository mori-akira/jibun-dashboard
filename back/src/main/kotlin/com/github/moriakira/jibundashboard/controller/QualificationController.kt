package com.github.moriakira.jibundashboard.controller

import com.github.moriakira.jibundashboard.component.CurrentAuth
import com.github.moriakira.jibundashboard.generated.api.QualificationApi
import com.github.moriakira.jibundashboard.generated.model.Qualification
import com.github.moriakira.jibundashboard.generated.model.QualificationBase
import com.github.moriakira.jibundashboard.generated.model.QualificationId
import com.github.moriakira.jibundashboard.mapper.toApi
import com.github.moriakira.jibundashboard.mapper.toModel
import com.github.moriakira.jibundashboard.service.QualificationService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
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
    override fun postQualifications(qualificationBase: QualificationBase?): ResponseEntity<QualificationId> {
        requireNotNull(qualificationBase) { "Request body is required." }
        val qualificationId = qualificationService.create(qualificationBase.toModel(currentAuth.userId))
        return ResponseEntity.ok().body(QualificationId(UUID.fromString(qualificationId)))
    }

    @Suppress("ReturnCount")
    override fun getQualificationsById(qualificationId: UUID): ResponseEntity<Qualification> {
        val model = qualificationService.getByQualificationIdForUser(qualificationId.toString(), currentAuth.userId)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(model.toApi())
    }

    @Suppress("ReturnCount")
    override fun putQualificationsById(
        qualificationId: UUID,
        qualificationBase: QualificationBase?,
    ): ResponseEntity<QualificationId> {
        requireNotNull(qualificationBase) { "Request body is required." }
        qualificationService.getByQualificationIdForUser(qualificationId.toString(), currentAuth.userId)
            ?: return ResponseEntity.notFound().build()
        qualificationService.put(qualificationBase.toModel(currentAuth.userId, qualificationId.toString()))
        return ResponseEntity.ok().body(QualificationId(qualificationId))
    }

    @Suppress("ReturnCount")
    override fun deleteQualificationsById(qualificationId: UUID): ResponseEntity<Unit> {
        qualificationService.getByQualificationIdForUser(qualificationId.toString(), currentAuth.userId)
            ?: return ResponseEntity.notFound().build()
        qualificationService.delete(currentAuth.userId, qualificationId.toString())
        return ResponseEntity.noContent().build()
    }
}
