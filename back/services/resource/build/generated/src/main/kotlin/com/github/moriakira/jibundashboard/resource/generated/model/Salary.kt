package com.github.moriakira.jibundashboard.resource.generated.model

import java.util.Objects
import com.fasterxml.jackson.annotation.JsonProperty
import com.github.moriakira.jibundashboard.resource.generated.model.Overview
import com.github.moriakira.jibundashboard.resource.generated.model.PayslipData
import com.github.moriakira.jibundashboard.resource.generated.model.Structure
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
 * 給与情報
 * @param targetDate 対象年月日
 * @param overview 
 * @param structure 
 * @param payslipData 
 * @param salaryId 給与ID
 * @param userId ユーザID
 */
data class Salary(

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
    @get:JsonProperty("payslipData", required = true) val payslipData: kotlin.collections.List<PayslipData>,

    @Schema(example = "null", description = "給与ID")
    @get:JsonProperty("salaryId") val salaryId: java.util.UUID? = null,

    @Schema(example = "null", description = "ユーザID")
    @get:JsonProperty("userId") val userId: kotlin.String? = null
    ) {

}

