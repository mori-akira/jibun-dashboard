package com.github.moriakira.jibundashboard.service

import com.github.moriakira.jibundashboard.repository.SalaryItem
import com.github.moriakira.jibundashboard.repository.SalaryRepository
import org.springframework.stereotype.Service

@Service
class SalaryService(
    private val salaryRepository: SalaryRepository,
) {

    fun listAll(userId: String): List<SalaryModel> = salaryRepository.queryByUser(userId).map { it.toDomain() }

    fun listByExactDate(userId: String, date: String): List<SalaryModel> =
        salaryRepository.queryByUserAndDate(userId, date).map { it.toDomain() }

    fun listByDateRange(userId: String, from: String?, to: String?): List<SalaryModel> =
        salaryRepository.queryByUserAndDateRange(userId, from, to).map { it.toDomain() }

    fun getBySalaryId(salaryId: String): SalaryModel? = salaryRepository.getBySalaryId(salaryId)?.toDomain()

    fun deleteBySalaryId(userId: String, targetDate: String) {
        salaryRepository.delete(userId, targetDate)
    }

    fun put(model: SalaryModel): String {
        salaryRepository.put(model.toItem())
        return model.salaryId
    }

    @Suppress("CyclomaticComplexMethod")
    private fun SalaryItem.toDomain(): SalaryModel = SalaryModel(
        salaryId = this.salaryId!!,
        userId = this.userId!!,
        targetDate = this.targetDate!!,
        overview = this.overview!!.let {
            OverviewModel(
                grossIncome = it.grossIncome ?: 0,
                netIncome = it.netIncome ?: 0,
                operatingTime = it.operatingTime ?: 0.0,
                overtime = it.overtime ?: 0.0,
                bonus = it.bonus ?: 0,
                bonusTakeHome = it.bonusTakeHome ?: 0,
            )
        },
        structure = this.structure!!.let {
            StructureModel(
                basicSalary = it.basicSalary ?: 0,
                overtimePay = it.overtimePay ?: 0,
                housingAllowance = it.housingAllowance ?: 0,
                positionAllowance = it.positionAllowance ?: 0,
                other = it.other ?: 0,
            )
        },
        payslipData = this.payslipData?.map { category ->
            PayslipCategoryModel(
                key = category.key!!,
                data = category.data?.map { data ->
                    PayslipEntryModel(
                        key = data.key!!,
                        data = data.data ?: 0.0,
                    )
                } ?: emptyList(),
            )
        } ?: emptyList(),
    )

    private fun SalaryModel.toItem(): SalaryItem = SalaryItem().also { item ->
        item.salaryId = this.salaryId
        item.userId = this.userId
        item.targetDate = this.targetDate
        item.overview = this.overview.let {
            SalaryItem.Overview().apply {
                grossIncome = it.grossIncome
                netIncome = it.netIncome
                operatingTime = it.operatingTime
                overtime = it.overtime
                bonus = it.bonus
                bonusTakeHome = it.bonusTakeHome
            }
        }
        item.structure = this.structure.let {
            SalaryItem.Structure().apply {
                basicSalary = it.basicSalary
                overtimePay = it.overtimePay
                housingAllowance = it.housingAllowance
                positionAllowance = it.positionAllowance
                other = it.other
            }
        }
        item.payslipData = this.payslipData.map { category ->
            SalaryItem.PayslipCategory().apply {
                key = category.key
                data = category.data.map { d ->
                    SalaryItem.PayslipEntry().apply {
                        key = d.key
                        data = d.data
                    }
                }
            }
        }
    }
}

data class SalaryModel(
    val salaryId: String,
    val userId: String,
    val targetDate: String,
    val overview: OverviewModel,
    val structure: StructureModel,
    val payslipData: List<PayslipCategoryModel>,
)

data class OverviewModel(
    val grossIncome: Int,
    val netIncome: Int,
    val operatingTime: Double,
    val overtime: Double,
    val bonus: Int,
    val bonusTakeHome: Int,
)

data class StructureModel(
    val basicSalary: Int,
    val overtimePay: Int,
    val housingAllowance: Int,
    val positionAllowance: Int,
    val other: Int,
)

data class PayslipCategoryModel(
    val key: String,
    val data: List<PayslipEntryModel>,
)

data class PayslipEntryModel(
    val key: String,
    val data: Double,
)
