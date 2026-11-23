package com.github.moriakira.jibundashboard.mapper

import com.github.moriakira.jibundashboard.generated.model.SharedLink
import com.github.moriakira.jibundashboard.generated.model.SharedLinkBase
import com.github.moriakira.jibundashboard.service.SharedLinkModel
import java.net.URI
import java.time.LocalDate
import java.util.UUID

fun SharedLinkModel.toApi(): SharedLink = SharedLink(
    token = UUID.fromString(this.token),
    dataTypes = this.dataTypes.map { SharedLink.DataTypes.forValue(it) }.toSet(),
    expiresAt = LocalDate.parse(this.expiresAt),
    shareUrl = URI.create(this.shareUrl),
)

fun SharedLinkModel.toApiBase(): SharedLinkBase = SharedLinkBase(
    dataTypes = this.dataTypes.map { SharedLinkBase.DataTypes.forValue(it) }.toSet(),
    expiresAt = LocalDate.parse(this.expiresAt),
)
