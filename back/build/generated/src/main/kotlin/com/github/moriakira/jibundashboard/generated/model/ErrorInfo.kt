package com.github.moriakira.jibundashboard.generated.model

import java.util.Objects
import com.fasterxml.jackson.annotation.JsonProperty
import com.github.moriakira.jibundashboard.generated.model.ErrorDetail
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
 * エラー情報
 * @param errors
 */
data class ErrorInfo(

    @field:Valid
    @Schema(example = "null", description = "")
    @get:JsonProperty("errors") val errors: kotlin.collections.List<ErrorDetail>? = null
    ) {

}
