package com.github.moriakira.jibundashboard.generated.model

import java.util.Objects
import com.fasterxml.jackson.annotation.JsonProperty
import com.github.moriakira.jibundashboard.generated.model.Tag
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
 * ボキャブラリー基本情報
 * @param name 名前
 * @param description 説明
 * @param tags タグ
 * @param createdDateTime 作成日時
 * @param updatedDateTime 更新日時
 */
data class VocabularyBase(

    @get:Size(max=128)
    @Schema(example = "null", required = true, description = "名前")
    @get:JsonProperty("name", required = true) val name: kotlin.String,

    @get:Size(max=2048)
    @Schema(example = "null", description = "説明")
    @get:JsonProperty("description") val description: kotlin.String? = null,

    @field:Valid
    @get:Size(max=64)
    @Schema(example = "null", description = "タグ")
    @get:JsonProperty("tags") val tags: kotlin.collections.Set<Tag>? = null,

    @Schema(example = "null", description = "作成日時")
    @get:JsonProperty("createdDateTime") val createdDateTime: java.time.OffsetDateTime? = null,

    @Schema(example = "null", description = "更新日時")
    @get:JsonProperty("updatedDateTime") val updatedDateTime: java.time.OffsetDateTime? = null
    ) {

}
