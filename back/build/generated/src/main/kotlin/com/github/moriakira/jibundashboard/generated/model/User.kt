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
 * ユーザ情報
 * @param userName ユーザ名
 * @param emailAddress eメールアドレス
 * @param userId ユーザID
 */
data class User(

    @get:Size(min=1,max=64)
    @Schema(example = "null", required = true, description = "ユーザ名")
    @get:JsonProperty("userName", required = true) val userName: kotlin.String,

    @get:Email
    @get:Size(min=1,max=256)
    @Schema(example = "null", required = true, description = "eメールアドレス")
    @get:JsonProperty("emailAddress", required = true) val emailAddress: kotlin.String,

    @Schema(example = "null", description = "ユーザID")
    @get:JsonProperty("userId") val userId: kotlin.String? = null
    ) {

}
