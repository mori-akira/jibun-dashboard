package com.github.moriakira.jibundashboard.controller

import com.github.moriakira.jibundashboard.component.CurrentAuth
import com.github.moriakira.jibundashboard.generated.api.SettingApi
import com.github.moriakira.jibundashboard.generated.model.Setting
import com.github.moriakira.jibundashboard.mapper.toApi
import com.github.moriakira.jibundashboard.mapper.toModel
import com.github.moriakira.jibundashboard.service.SettingService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class SettingController(private val currentAuth: CurrentAuth, private val settingService: SettingService) :
    SettingApi {

    override fun getSettings(): ResponseEntity<Setting> =
        ResponseEntity.ok(settingService.getOrDefault(currentAuth.userId).toApi(currentAuth.userId))

    override fun putSettings(setting: Setting?): ResponseEntity<Unit> {
        requireNotNull(setting) { "Request body is required." }
        settingService.put(setting.toModel(currentAuth.userId))
        return ResponseEntity.ok().build()
    }
}
