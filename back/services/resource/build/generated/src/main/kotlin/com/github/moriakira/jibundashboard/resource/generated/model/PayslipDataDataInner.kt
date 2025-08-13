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
 * 
 * @param key 明細キー
 * @param &#x60;data&#x60; 明細情報
 */
data class PayslipDataDataInner(

    @Schema(example = "null", required = true, description = "明細キー")
    @get:JsonProperty("key", required = true) val key: kotlin.String,

    @Schema(example = "null", required = true, description = "明細情報")
    @get:JsonProperty("data", required = true) val `data`: kotlin.Double
    ) {

}

