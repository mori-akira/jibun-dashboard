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
 * パスワード情報
 * @param oldPassword 旧パスワード
 * @param newPassword 新パスワード
 */
data class Password(

    @get:Pattern(regexp="^[a-zA-Z0-9!\\\"#\\$%&'\\(\\)=~\\|@{}\\[\\]\\+\\*,\\./\\\\<>?_]+$")
    @get:Size(min=8,max=32)
    @Schema(example = "null", required = true, description = "旧パスワード")
    @get:JsonProperty("oldPassword", required = true) val oldPassword: kotlin.String,

    @get:Pattern(regexp="^[a-zA-Z0-9!\\\"#\\$%&'\\(\\)=~\\|@{}\\[\\]\\+\\*,\\./\\\\<>?_]+$")
    @get:Size(min=8,max=32)
    @Schema(example = "null", required = true, description = "新パスワード")
    @get:JsonProperty("newPassword", required = true) val newPassword: kotlin.String
    ) {

}
