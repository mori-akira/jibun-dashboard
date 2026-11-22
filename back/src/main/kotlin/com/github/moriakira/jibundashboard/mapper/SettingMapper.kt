package com.github.moriakira.jibundashboard.mapper

import com.github.moriakira.jibundashboard.generated.model.Setting
import com.github.moriakira.jibundashboard.generated.model.SettingQualification
import com.github.moriakira.jibundashboard.generated.model.SettingSalary
import com.github.moriakira.jibundashboard.service.QualificationSettingModel
import com.github.moriakira.jibundashboard.service.SalarySettingModel
import com.github.moriakira.jibundashboard.service.SettingModel

fun SettingModel.toApi(userId: String? = null): Setting = Setting(
    userId = userId,
    salary = SettingSalary(
        financialYearStartMonth = this.salary.financialYearStartMonth,
        transitionItemCount = this.salary.transitionItemCount,
        compareDataColors = this.salary.compareDataColors,
    ),
    qualification = SettingQualification(
        rankAColor = this.qualification.rankAColor,
        rankBColor = this.qualification.rankBColor,
        rankCColor = this.qualification.rankCColor,
        rankDColor = this.qualification.rankDColor,
    ),
)

fun Setting.toModel(userId: String): SettingModel = SettingModel(
    userId = userId,
    salary = SalarySettingModel(
        financialYearStartMonth = this.salary.financialYearStartMonth,
        transitionItemCount = this.salary.transitionItemCount,
        compareDataColors = this.salary.compareDataColors,
    ),
    qualification = QualificationSettingModel(
        rankAColor = this.qualification.rankAColor,
        rankBColor = this.qualification.rankBColor,
        rankCColor = this.qualification.rankCColor,
        rankDColor = this.qualification.rankDColor,
    ),
)
