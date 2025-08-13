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
 * 給与構成
 * @param basicSalary 基本給
 * @param overtimePay 残業代
 * @param housingAllowance 家賃手当
 * @param positionAllowance 役職手当
 * @param other その他
 */
data class Structure(

    @get:Min(0)
    @Schema(example = "null", required = true, description = "基本給")
    @get:JsonProperty("basicSalary", required = true) val basicSalary: kotlin.Int,

    @get:Min(0)
    @Schema(example = "null", required = true, description = "残業代")
    @get:JsonProperty("overtimePay", required = true) val overtimePay: kotlin.Int,

    @get:Min(0)
    @Schema(example = "null", required = true, description = "家賃手当")
    @get:JsonProperty("housingAllowance", required = true) val housingAllowance: kotlin.Int,

    @get:Min(0)
    @Schema(example = "null", required = true, description = "役職手当")
    @get:JsonProperty("positionAllowance", required = true) val positionAllowance: kotlin.Int,

    @get:Min(0)
    @Schema(example = "null", required = true, description = "その他")
    @get:JsonProperty("other", required = true) val other: kotlin.Int
    ) {

}

