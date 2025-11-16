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
 * 給与OCRタスクID
 * @param ocrTaskId 給与OCRタスクID
 */
data class SalaryOcrTaskId(

    @Schema(example = "null", description = "給与OCRタスクID")
    @get:JsonProperty("ocrTaskId") val ocrTaskId: java.util.UUID? = null
    ) {

}
