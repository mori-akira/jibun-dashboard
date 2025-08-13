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
 * 給与概要
 * @param grossIncome 額面
 * @param netIncome 手取り
 * @param operatingTime 稼働時間
 * @param overtime 残業時間
 * @param bonus ボーナス
 * @param bonusTakeHome ボーナス(手取り)
 */
data class Overview(

    @get:Min(0)
    @Schema(example = "null", required = true, description = "額面")
    @get:JsonProperty("grossIncome", required = true) val grossIncome: kotlin.Int,

    @get:Min(0)
    @Schema(example = "null", required = true, description = "手取り")
    @get:JsonProperty("netIncome", required = true) val netIncome: kotlin.Int,

    @get:DecimalMin("0")
    @Schema(example = "null", required = true, description = "稼働時間")
    @get:JsonProperty("operatingTime", required = true) val operatingTime: kotlin.Double,

    @get:DecimalMin("0")
    @Schema(example = "null", required = true, description = "残業時間")
    @get:JsonProperty("overtime", required = true) val overtime: kotlin.Double,

    @get:Min(0)
    @Schema(example = "null", required = true, description = "ボーナス")
    @get:JsonProperty("bonus", required = true) val bonus: kotlin.Int,

    @get:Min(0)
    @Schema(example = "null", required = true, description = "ボーナス(手取り)")
    @get:JsonProperty("bonusTakeHome", required = true) val bonusTakeHome: kotlin.Int
    ) {

}

