package com.github.moriakira.jibundashboard.resource.generated.model

import java.util.Objects
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import jakarta.validation.Valid
import io.swagger.v3.oas.annotations.media.Schema

/**
 * アップロードURL情報
 * @param fileId ファイルID
 * @param uploadUrl アップロードURL
 * @param expireDateTime 期限切れ日時
 */
data class UploadUrl(

    @Schema(example = "null", required = true, description = "ファイルID")
    @get:JsonProperty("fileId", required = true) val fileId: java.util.UUID,

    @field:Valid
    @Schema(example = "null", required = true, description = "アップロードURL")
    @get:JsonProperty("uploadUrl", required = true) val uploadUrl: java.net.URI,

    @Schema(example = "null", description = "期限切れ日時")
    @get:JsonProperty("expireDateTime") val expireDateTime: java.time.OffsetDateTime? = null
    ) {

}

