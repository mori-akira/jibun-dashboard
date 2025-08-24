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
 * 給与設定
 * @param financialYearStartMonth
 * @param transitionItemCount
 * @param compareDataColors
 */
data class SettingSalary(

    @get:Min(1)
    @get:Max(12)
    @Schema(example = "null", required = true, description = "")
    @get:JsonProperty("financialYearStartMonth", required = true) val financialYearStartMonth: kotlin.Int,

    @get:Min(1)
    @get:Max(10)
    @Schema(example = "null", required = true, description = "")
    @get:JsonProperty("transitionItemCount", required = true) val transitionItemCount: kotlin.Int,

    @get:Size(min=3,max=3)
    @Schema(example = "null", required = true, description = "")
    @get:JsonProperty("compareDataColors", required = true) val compareDataColors: kotlin.collections.List<kotlin.String>
    ) {

}
