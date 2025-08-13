package com.github.moriakira.jibundashboard.resource.generated.model

import java.util.Objects
import com.fasterxml.jackson.annotation.JsonProperty
import com.github.moriakira.jibundashboard.resource.generated.model.PayslipDataDataInner
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
 * 給与明細
 * @param key 明細カテゴリキー
 * @param &#x60;data&#x60; 明細カテゴリ
 */
data class PayslipData(

    @Schema(example = "null", required = true, description = "明細カテゴリキー")
    @get:JsonProperty("key", required = true) val key: kotlin.String,

    @field:Valid
    @Schema(example = "null", required = true, description = "明細カテゴリ")
    @get:JsonProperty("data", required = true) val `data`: kotlin.collections.List<PayslipDataDataInner>
    ) {

}

