package com.github.moriakira.jibundashboard.api

import com.github.moriakira.jibundashboard.component.CurrentAuth
import com.github.moriakira.jibundashboard.generated.api.SettingApi
import com.github.moriakira.jibundashboard.generated.model.Setting
import com.github.moriakira.jibundashboard.generated.model.SettingQualification
import com.github.moriakira.jibundashboard.generated.model.SettingSalary
import com.github.moriakira.jibundashboard.service.QualificationSettingModel
import com.github.moriakira.jibundashboard.service.SalarySettingModel
import com.github.moriakira.jibundashboard.service.SettingModel
import com.github.moriakira.jibundashboard.service.SettingService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class SettingController(
    private val currentAuth: CurrentAuth,
    private val settingService: SettingService,
) : SettingApi {

    override fun getSetting(): ResponseEntity<Setting> {
        var setting = settingService.getSetting(currentAuth.userId)
        return setting?.let {
            // 設定が登録済みなら返却
            ResponseEntity.ok(
                Setting(
                    userId = currentAuth.userId,
                    salary = SettingSalary(
                        financialYearStartMonth = setting.salary.financialYearStartMonth,
                        transitionItemCount = setting.salary.transitionItemCount,
                        compareDataColors = setting.salary.compareDataColors,
                    ),
                    qualification = SettingQualification(
                        rankAColor = setting.qualification.rankAColor,
                        rankBColor = setting.qualification.rankBColor,
                        rankCColor = setting.qualification.rankCColor,
                        rankDColor = setting.qualification.rankDColor,
                    ),
                ),
            )
        } ?: run {
            // 未登録の場合、初期登録
            setting = settingService.putDefault(currentAuth.userId)

            // 登録した設定情報を返却
            ResponseEntity.ok(
                Setting(
                    userId = currentAuth.userId,
                    salary = SettingSalary(
                        financialYearStartMonth = setting.salary.financialYearStartMonth,
                        transitionItemCount = setting.salary.transitionItemCount,
                        compareDataColors = setting.salary.compareDataColors,
                    ),
                    qualification = SettingQualification(
                        rankAColor = setting.qualification.rankAColor,
                        rankBColor = setting.qualification.rankBColor,
                        rankCColor = setting.qualification.rankCColor,
                        rankDColor = setting.qualification.rankDColor,
                    ),
                ),
            )
        }
    }

    override fun putSetting(setting: Setting?): ResponseEntity<Unit> {
        setting?.let {
            settingService.putSetting(
                SettingModel(
                    userId = currentAuth.userId,
                    salary = SalarySettingModel(
                        financialYearStartMonth = it.salary.financialYearStartMonth,
                        transitionItemCount = it.salary.transitionItemCount,
                        compareDataColors = it.salary.compareDataColors,
                    ),
                    qualification = QualificationSettingModel(
                        rankAColor = it.qualification.rankAColor,
                        rankBColor = it.qualification.rankBColor,
                        rankCColor = it.qualification.rankCColor,
                        rankDColor = it.qualification.rankDColor,
                    ),
                ),
            )
        }
        return ResponseEntity.ok().build()
    }
}
