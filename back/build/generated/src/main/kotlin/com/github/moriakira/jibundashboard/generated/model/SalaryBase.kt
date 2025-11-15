package com.github.moriakira.jibundashboard.generated.model

import java.util.Objects
import com.fasterxml.jackson.annotation.JsonProperty
import com.github.moriakira.jibundashboard.generated.model.Overview
import com.github.moriakira.jibundashboard.generated.model.PayslipData
import com.github.moriakira.jibundashboard.generated.model.Structure
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
 * 給与基本情報
 * @param targetDate 対象年月日
 * @param overview
 * @param structure
 * @param payslipData
 */
data class SalaryBase(

    @field:Valid
    @Schema(example = "null", required = true, description = "対象年月日")
    @get:JsonProperty("targetDate", required = true) val targetDate: java.time.LocalDate,

    @field:Valid
    @Schema(example = "null", required = true, description = "")
    @get:JsonProperty("overview", required = true) val overview: Overview,

    @field:Valid
    @Schema(example = "null", required = true, description = "")
    @get:JsonProperty("structure", required = true) val structure: Structure,

    @field:Valid
    @Schema(example = "null", required = true, description = "")
    @get:JsonProperty("payslipData", required = true) val payslipData: kotlin.collections.List<PayslipData>
    ) {

}
