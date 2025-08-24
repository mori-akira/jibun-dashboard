package com.github.moriakira.jibundashboard.generated.model

import java.util.Objects
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonValue
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
 * エラー詳細
 * @param errorCode エラーコード
 * @param errorLevel エラーレベル
 * @param errorMessage エラーメッセージ
 * @param errorItem エラーアイテム
 */
data class ErrorDetail(

    @Schema(example = "null", required = true, description = "エラーコード")
    @get:JsonProperty("errorCode", required = true) val errorCode: kotlin.String,

    @Schema(example = "null", required = true, description = "エラーレベル")
    @get:JsonProperty("errorLevel", required = true) val errorLevel: ErrorDetail.ErrorLevel,

    @Schema(example = "null", required = true, description = "エラーメッセージ")
    @get:JsonProperty("errorMessage", required = true) val errorMessage: kotlin.String,

    @Schema(example = "null", description = "エラーアイテム")
    @get:JsonProperty("errorItem") val errorItem: kotlin.collections.List<kotlin.String>? = null
    ) {

    /**
    * エラーレベル
    * Values: DEBUG,INFO,WARN,ERROR,CRITICAL
    */
    enum class ErrorLevel(@get:JsonValue val value: kotlin.String) {

        DEBUG("DEBUG"),
        INFO("INFO"),
        WARN("WARN"),
        ERROR("ERROR"),
        CRITICAL("CRITICAL");

        companion object {
            @JvmStatic
            @JsonCreator
            fun forValue(value: kotlin.String): ErrorLevel {
                return values().first{it -> it.value == value}
            }
        }
    }

}
