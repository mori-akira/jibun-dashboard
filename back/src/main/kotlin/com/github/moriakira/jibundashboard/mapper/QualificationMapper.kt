package com.github.moriakira.jibundashboard.mapper

import com.github.moriakira.jibundashboard.generated.model.Qualification
import com.github.moriakira.jibundashboard.generated.model.QualificationBase
import com.github.moriakira.jibundashboard.service.QualificationModel
import java.net.URI
import java.time.LocalDate
import java.util.UUID

fun QualificationModel.toApi(): Qualification = Qualification(
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
    certificationAssetId = this.certificationAssetId?.let { UUID.fromString(it) },
)

fun QualificationBase.toModel(userId: String, qualificationId: String = ""): QualificationModel = QualificationModel(
    qualificationId = qualificationId,
    userId = userId,
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
    certificationAssetId = this.certificationAssetId?.toString(),
)
