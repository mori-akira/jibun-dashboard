package com.github.moriakira.jibundashboard.mapper

import com.github.moriakira.jibundashboard.generated.model.Overview
import com.github.moriakira.jibundashboard.generated.model.PayslipData
import com.github.moriakira.jibundashboard.generated.model.PayslipDataDataInner
import com.github.moriakira.jibundashboard.generated.model.Salary
import com.github.moriakira.jibundashboard.generated.model.SalaryBase
import com.github.moriakira.jibundashboard.generated.model.SalaryOcrTask
import com.github.moriakira.jibundashboard.generated.model.Structure
import com.github.moriakira.jibundashboard.service.OverviewModel
import com.github.moriakira.jibundashboard.service.PayslipCategoryModel
import com.github.moriakira.jibundashboard.service.PayslipEntryModel
import com.github.moriakira.jibundashboard.service.SalaryModel
import com.github.moriakira.jibundashboard.service.SalaryOcrTaskModel
import com.github.moriakira.jibundashboard.service.StructureModel
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

fun SalaryModel.toApi(): Salary = Salary(
    salaryId = UUID.fromString(this.salaryId),
    targetDate = LocalDate.parse(this.targetDate),
    overview = this.overview.let {
        Overview(
            grossIncome = it.grossIncome,
            netIncome = it.netIncome,
            operatingTime = it.operatingTime,
            overtime = it.overtime,
            bonus = it.bonus,
            bonusTakeHome = it.bonusTakeHome,
        )
    },
    structure = this.structure.let {
        Structure(
            basicSalary = it.basicSalary,
            overtimePay = it.overtimePay,
            housingAllowance = it.housingAllowance,
            positionAllowance = it.positionAllowance,
            other = it.other,
        )
    },
    payslipData = this.payslipData.map { category ->
        PayslipData(
            key = category.key,
            data = category.data.map { PayslipDataDataInner(key = it.key, data = it.data) },
        )
    },
)

fun SalaryBase.toModel(userId: String, salaryId: String = ""): SalaryModel = SalaryModel(
    salaryId = salaryId,
    userId = userId,
    targetDate = this.targetDate.toString(),
    overview = this.overview.let {
        OverviewModel(
            grossIncome = it.grossIncome,
            netIncome = it.netIncome,
            operatingTime = it.operatingTime,
            overtime = it.overtime,
            bonus = it.bonus,
            bonusTakeHome = it.bonusTakeHome,
        )
    },
    structure = this.structure.let {
        StructureModel(
            basicSalary = it.basicSalary,
            overtimePay = it.overtimePay,
            housingAllowance = it.housingAllowance,
            positionAllowance = it.positionAllowance,
            other = it.other,
        )
    },
    payslipData = this.payslipData.map { category ->
        PayslipCategoryModel(
            key = category.key,
            data = category.data.map { PayslipEntryModel(key = it.key, data = it.data) },
        )
    },
)

fun SalaryOcrTaskModel.toApi(): SalaryOcrTask = SalaryOcrTask(
    ocrTaskId = UUID.fromString(this.ocrTaskId),
    status = SalaryOcrTask.Status.valueOf(this.status.name),
    targetDate = LocalDate.parse(this.targetDate),
    createdAt = OffsetDateTime.parse(this.createdAt),
    updatedAt = OffsetDateTime.parse(this.updatedAt),
)
