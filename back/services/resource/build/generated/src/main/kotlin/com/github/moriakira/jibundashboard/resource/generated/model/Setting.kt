package com.github.moriakira.jibundashboard.resource.generated.model

import java.util.Objects
import com.fasterxml.jackson.annotation.JsonProperty
import com.github.moriakira.jibundashboard.resource.generated.model.SettingQualification
import com.github.moriakira.jibundashboard.resource.generated.model.SettingSalary
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
 * 設定情報
 * @param salary
 * @param qualification
 * @param settingId 設定ID
 * @param userId ユーザID
 */
data class Setting(

    @field:Valid
    @Schema(example = "null", required = true, description = "")
    @get:JsonProperty("salary", required = true) val salary: SettingSalary,

    @field:Valid
    @Schema(example = "null", required = true, description = "")
    @get:JsonProperty("qualification", required = true) val qualification: SettingQualification,

    @Schema(example = "null", description = "設定ID")
    @get:JsonProperty("settingId") val settingId: java.util.UUID? = null,

    @Schema(example = "null", description = "ユーザID")
    @get:JsonProperty("userId") val userId: kotlin.String? = null
    ) {

}
