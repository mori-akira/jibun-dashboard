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
 * 資格情報
 * @param qualificationName 資格名
 * @param status ステータス
 * @param rank ランク
 * @param organization 発行組織
 * @param officialUrl 公式URL
 * @param qualificationId 資格ID
 * @param userId ユーザID
 * @param abbreviation 略称
 * @param version バージョン
 * @param acquiredDate 取得年月日
 * @param expirationDate 有効期限
 * @param certificationUrl 証明書URL
 * @param badgeUrl バッジURL
 */
data class Qualification(

    @get:Size(min=1,max=128)
    @Schema(example = "null", required = true, description = "資格名")
    @get:JsonProperty("qualificationName", required = true) val qualificationName: kotlin.String,

    @Schema(example = "null", required = true, description = "ステータス")
    @get:JsonProperty("status", required = true) val status: Qualification.Status,

    @Schema(example = "null", required = true, description = "ランク")
    @get:JsonProperty("rank", required = true) val rank: Qualification.Rank,

    @get:Size(min=1,max=128)
    @Schema(example = "null", required = true, description = "発行組織")
    @get:JsonProperty("organization", required = true) val organization: kotlin.String,

    @field:Valid
    @Schema(example = "null", required = true, description = "公式URL")
    @get:JsonProperty("officialUrl", required = true) val officialUrl: java.net.URI,

    @Schema(example = "null", description = "資格ID")
    @get:JsonProperty("qualificationId") val qualificationId: java.util.UUID? = null,

    @Schema(example = "null", description = "ユーザID")
    @get:JsonProperty("userId") val userId: kotlin.String? = null,

    @get:Size(max=128)
    @Schema(example = "null", description = "略称")
    @get:JsonProperty("abbreviation") val abbreviation: kotlin.String? = null,

    @get:Size(max=128)
    @Schema(example = "null", description = "バージョン")
    @get:JsonProperty("version") val version: kotlin.String? = null,

    @field:Valid
    @Schema(example = "null", description = "取得年月日")
    @get:JsonProperty("acquiredDate") val acquiredDate: java.time.LocalDate? = null,

    @field:Valid
    @Schema(example = "null", description = "有効期限")
    @get:JsonProperty("expirationDate") val expirationDate: java.time.LocalDate? = null,

    @field:Valid
    @Schema(example = "null", description = "証明書URL")
    @get:JsonProperty("certificationUrl") val certificationUrl: java.net.URI? = null,

    @field:Valid
    @Schema(example = "null", description = "バッジURL")
    @get:JsonProperty("badgeUrl") val badgeUrl: java.net.URI? = null
    ) {

    /**
    * ステータス
    * Values: dream,planning,acquired
    */
    enum class Status(@get:JsonValue val value: kotlin.String) {

        dream("dream"),
        planning("planning"),
        acquired("acquired");

        companion object {
            @JvmStatic
            @JsonCreator
            fun forValue(value: kotlin.String): Status {
                return values().first{it -> it.value == value}
            }
        }
    }

    /**
    * ランク
    * Values: D,C,B,A
    */
    enum class Rank(@get:JsonValue val value: kotlin.String) {

        D("D"),
        C("C"),
        B("B"),
        A("A");

        companion object {
            @JvmStatic
            @JsonCreator
            fun forValue(value: kotlin.String): Rank {
                return values().first{it -> it.value == value}
            }
        }
    }

}
