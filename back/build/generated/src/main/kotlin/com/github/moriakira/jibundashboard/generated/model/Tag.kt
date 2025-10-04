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
 * ボキャブラリータグ
 * @param tag タグ
 * @param tagId タグID
 */
data class Tag(

    @get:Size(min=1,max=64)
    @Schema(example = "null", required = true, description = "タグ")
    @get:JsonProperty("tag", required = true) val tag: kotlin.String,

    @Schema(example = "null", description = "タグID")
    @get:JsonProperty("tagId") val tagId: java.util.UUID? = null
    ) {

}
