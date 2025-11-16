package com.github.moriakira.jibundashboard.generated.model

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
 *
 * @param targetDate 対象年月日
 * @param fileId ファイルID
 */
data class PostSalaryOcrTaskStartRequest(

    @field:Valid
    @Schema(example = "null", required = true, description = "対象年月日")
    @get:JsonProperty("targetDate", required = true) val targetDate: java.time.LocalDate,

    @Schema(example = "null", required = true, description = "ファイルID")
    @get:JsonProperty("fileId", required = true) val fileId: java.util.UUID
    ) {

}
