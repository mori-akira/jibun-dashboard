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
 * 国際化リソース
 * @param localeCode ロケールコード
 * @param messages メッセージ
 */
data class I18n(

    @get:Pattern(regexp="^[a-z]{2}$")
    @Schema(example = "null", description = "ロケールコード")
    @get:JsonProperty("localeCode") val localeCode: kotlin.String? = null,

    @Schema(example = "null", description = "メッセージ")
    @get:JsonProperty("messages") val messages: kotlin.collections.Map<kotlin.String, kotlin.String>? = null
    ) {

}

