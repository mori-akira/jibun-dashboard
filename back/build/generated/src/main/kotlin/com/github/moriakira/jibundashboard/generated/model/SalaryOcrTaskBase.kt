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
 * 給与OCRタスク基本情報
 * @param status タスクステータス
 * @param targetDate 対象年月日
 * @param createdAt 作成日時
 * @param updatedAt 更新日時
 */
data class SalaryOcrTaskBase(

    @Schema(example = "null", required = true, description = "タスクステータス")
    @get:JsonProperty("status", required = true) val status: SalaryOcrTaskBase.Status,

    @field:Valid
    @Schema(example = "null", required = true, description = "対象年月日")
    @get:JsonProperty("targetDate", required = true) val targetDate: java.time.LocalDate,

    @Schema(example = "null", required = true, description = "作成日時")
    @get:JsonProperty("createdAt", required = true) val createdAt: java.time.OffsetDateTime,

    @Schema(example = "null", required = true, description = "更新日時")
    @get:JsonProperty("updatedAt", required = true) val updatedAt: java.time.OffsetDateTime
    ) {

    /**
    * タスクステータス
    * Values: PENDING,RUNNING,COMPLETED,FAILED
    */
    enum class Status(@get:JsonValue val value: kotlin.String) {

        PENDING("PENDING"),
        RUNNING("RUNNING"),
        COMPLETED("COMPLETED"),
        FAILED("FAILED");

        companion object {
            @JvmStatic
            @JsonCreator
            fun forValue(value: kotlin.String): Status {
                return values().first{it -> it.value == value}
            }
        }
    }

}
