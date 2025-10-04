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
 * ボキャブラリー
 * @param name 名前
 * @param vocabularyId ボキャブラリーID
 * @param userId ユーザID
 * @param description 説明
 * @param tags タグ
 */
data class Vocabulary(

    @get:Size(max=100)
    @Schema(example = "null", required = true, description = "名前")
    @get:JsonProperty("name", required = true) val name: kotlin.String,

    @Schema(example = "null", description = "ボキャブラリーID")
    @get:JsonProperty("vocabularyId") val vocabularyId: java.util.UUID? = null,

    @Schema(example = "null", description = "ユーザID")
    @get:JsonProperty("userId") val userId: kotlin.String? = null,

    @get:Size(max=2000)
    @Schema(example = "null", description = "説明")
    @get:JsonProperty("description") val description: kotlin.String? = null,

    @get:Size(max=50)
    @Schema(example = "null", description = "タグ")
    @get:JsonProperty("tags") val tags: kotlin.collections.Set<kotlin.String>? = null
    ) {

}
