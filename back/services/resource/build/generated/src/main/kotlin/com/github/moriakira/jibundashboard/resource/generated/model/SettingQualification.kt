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
 * 資格設定
 * @param rankAColor
 * @param rankBColor
 * @param rankCColor
 * @param rankDColor
 */
data class SettingQualification(

    @get:Pattern(regexp="#([0-9a-fA-F]{3}|[0-9a-fA-F]{6}|[0-9a-fA-F]{8})")
    @Schema(example = "null", required = true, description = "")
    @get:JsonProperty("rankAColor", required = true) val rankAColor: kotlin.String,

    @get:Pattern(regexp="#([0-9a-fA-F]{3}|[0-9a-fA-F]{6}|[0-9a-fA-F]{8})")
    @Schema(example = "null", required = true, description = "")
    @get:JsonProperty("rankBColor", required = true) val rankBColor: kotlin.String,

    @get:Pattern(regexp="#([0-9a-fA-F]{3}|[0-9a-fA-F]{6}|[0-9a-fA-F]{8})")
    @Schema(example = "null", required = true, description = "")
    @get:JsonProperty("rankCColor", required = true) val rankCColor: kotlin.String,

    @get:Pattern(regexp="#([0-9a-fA-F]{3}|[0-9a-fA-F]{6}|[0-9a-fA-F]{8})")
    @Schema(example = "null", required = true, description = "")
    @get:JsonProperty("rankDColor", required = true) val rankDColor: kotlin.String
    ) {

}
