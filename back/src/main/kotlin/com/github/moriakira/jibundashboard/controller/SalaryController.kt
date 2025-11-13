package com.github.moriakira.jibundashboard.controller

import com.github.moriakira.jibundashboard.component.CurrentAuth
import com.github.moriakira.jibundashboard.generated.api.SalaryApi
import com.github.moriakira.jibundashboard.generated.model.Overview
import com.github.moriakira.jibundashboard.generated.model.PayslipData
import com.github.moriakira.jibundashboard.generated.model.PayslipDataDataInner
import com.github.moriakira.jibundashboard.generated.model.Salary
import com.github.moriakira.jibundashboard.generated.model.SalaryId
import com.github.moriakira.jibundashboard.generated.model.Structure
import com.github.moriakira.jibundashboard.service.OverviewModel
import com.github.moriakira.jibundashboard.service.PayslipCategoryModel
import com.github.moriakira.jibundashboard.service.PayslipEntryModel
import com.github.moriakira.jibundashboard.service.SalaryModel
import com.github.moriakira.jibundashboard.service.SalaryService
import com.github.moriakira.jibundashboard.service.StructureModel
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.util.UUID

@RestController
class SalaryController(
    private val currentAuth: CurrentAuth,
    private val salaryService: SalaryService,
) : SalaryApi {

    override fun getSalary(
        targetDate: LocalDate?,
        targetDateFrom: LocalDate?,
        targetDateTo: LocalDate?,
    ): ResponseEntity<List<Salary>> {
        val list = when {
            targetDate != null -> salaryService.listByExactDate(currentAuth.userId, targetDate.toString())
            targetDateFrom != null || targetDateTo != null -> salaryService.listByDateRange(
                currentAuth.userId,
                targetDateFrom?.toString(),
                targetDateTo?.toString(),
            )

            else -> salaryService.listAll(currentAuth.userId)
        }
        return ResponseEntity.ok(list.map { it.toApi() })
    }

    @Suppress("ReturnCount")
    override fun putSalary(salary: Salary?): ResponseEntity<SalaryId> {
        requireNotNull(salary) { "Request body is required." }
        if (salary.salaryId != null) {
            val model =
                salaryService.getBySalaryId(salary.salaryId.toString()) ?: return ResponseEntity.notFound().build()
            if (model.userId != currentAuth.userId) return ResponseEntity.notFound().build()
        }
        val salaryId = salaryService.put(salary.toModel())
        val status = if (salary.salaryId == null) HttpStatus.CREATED else HttpStatus.OK
        return ResponseEntity.status(status).body(SalaryId(salaryId = UUID.fromString(salaryId)))
    }

    @Suppress("ReturnCount")
    override fun getSalaryById(salaryId: UUID): ResponseEntity<Salary> {
        val model = salaryService.getBySalaryId(salaryId.toString()) ?: return ResponseEntity.notFound().build()
        if (model.userId != currentAuth.userId) return ResponseEntity.notFound().build()
        return ResponseEntity.ok(model.toApi())
    }

    @Suppress("ReturnCount")
    override fun deleteSalary(salaryId: UUID): ResponseEntity<Unit> {
        val model = salaryService.getBySalaryId(salaryId.toString()) ?: return ResponseEntity.noContent().build()
        if (model.userId != currentAuth.userId) return ResponseEntity.noContent().build()
        salaryService.deleteBySalaryId(currentAuth.userId, model.targetDate)
        return ResponseEntity.noContent().build()
    }

    override fun getSalaryOcr(targetDate: LocalDate, fileId: UUID): ResponseEntity<SalaryId> =
        ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build()

    private fun SalaryModel.toApi(): Salary = Salary(
        salaryId = UUID.fromString(this.salaryId),
        userId = this.userId,
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
                data = category.data.map { data ->
                    PayslipDataDataInner(
                        key = data.key,
                        data = data.data,
                    )
                },
            )
        },
    )

    private fun Salary.toModel(): SalaryModel = SalaryModel(
        salaryId = this.salaryId?.toString() ?: UUID.randomUUID().toString(),
        userId = currentAuth.userId,
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
                data = category.data.map { data ->
                    PayslipEntryModel(
                        key = data.key,
                        data = data.data,
                    )
                },
            )
        },
    )
}
