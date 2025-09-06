package com.github.moriakira.jibundashboard.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.github.moriakira.jibundashboard.repository.QualificationSetting
import com.github.moriakira.jibundashboard.repository.SalarySetting
import com.github.moriakira.jibundashboard.repository.SettingItem
import com.github.moriakira.jibundashboard.repository.SettingRepository
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Service

@Service
class SettingService(
    private val settingRepository: SettingRepository,
    private val resourceLoader: ResourceLoader,
) {
    private val yamlMapper: ObjectMapper = ObjectMapper(YAMLFactory()).registerModule(KotlinModule.Builder().build())

    private val defaultSetting: SettingModel by lazy {
        val res = resourceLoader.getResource("classpath:defaults/setting.yaml")
        require(res.exists()) { "Default setting YAML not found: classpath:defaults/setting.yaml" }
        res.inputStream.use { yamlMapper.readValue(it, SettingModel::class.java) }
    }

    fun getSetting(userId: String): SettingModel? = settingRepository.get(userId)?.toDomain()

    fun putSetting(model: SettingModel): SettingModel {
        val item = model.toItem()
        settingRepository.put(item)
        return item.toDomain()
    }

    fun putDefault(userId: String): SettingModel {
        val existing = settingRepository.get(userId)
        if (existing != null) {
            return existing.toDomain()
        }

        val item = SettingItem().apply {
            this.userId = userId
            this.salary = SalarySetting().apply {
                this.financialYearStartMonth = defaultSetting.salary.financialYearStartMonth
                this.transitionItemCount = defaultSetting.salary.transitionItemCount
                this.compareDataColors = defaultSetting.salary.compareDataColors
            }
            this.qualification = QualificationSetting().apply {
                this.rankAColor = defaultSetting.qualification.rankAColor
                this.rankBColor = defaultSetting.qualification.rankBColor
                this.rankCColor = defaultSetting.qualification.rankCColor
                this.rankDColor = defaultSetting.qualification.rankDColor
            }
        }
        settingRepository.put(item)
        return item.toDomain()
    }

    private fun SettingItem.toDomain(): SettingModel = SettingModel(
        userId = this.userId!!,
        salary = this.salary?.let {
            SalarySettingModel(
                financialYearStartMonth = it.financialYearStartMonth ?: defaultSetting.salary.financialYearStartMonth,
                transitionItemCount = it.transitionItemCount ?: defaultSetting.salary.transitionItemCount,
                compareDataColors = it.compareDataColors ?: defaultSetting.salary.compareDataColors,
            )
        } ?: defaultSetting.salary,
        qualification = this.qualification?.let {
            QualificationSettingModel(
                rankAColor = it.rankAColor ?: defaultSetting.qualification.rankAColor,
                rankBColor = it.rankBColor ?: defaultSetting.qualification.rankBColor,
                rankCColor = it.rankCColor ?: defaultSetting.qualification.rankCColor,
                rankDColor = it.rankDColor ?: defaultSetting.qualification.rankDColor,
            )
        } ?: defaultSetting.qualification,
    )

    private fun SettingModel.toItem(): SettingItem = SettingItem().also { dst ->
        dst.userId = this.userId
        dst.salary = this.salary.let {
            SalarySetting().apply {
                financialYearStartMonth = it.financialYearStartMonth
                transitionItemCount = it.transitionItemCount
                compareDataColors = it.compareDataColors
            }
        }
        dst.qualification = this.qualification.let {
            QualificationSetting().apply {
                rankAColor = it.rankAColor
                rankBColor = it.rankBColor
                rankCColor = it.rankCColor
                rankDColor = it.rankDColor
            }
        }
    }
}

data class SettingModel(
    val userId: String?,
    val salary: SalarySettingModel,
    val qualification: QualificationSettingModel,
)

data class SalarySettingModel(
    val financialYearStartMonth: Int,
    val transitionItemCount: Int,
    val compareDataColors: List<String>,
)

data class QualificationSettingModel(
    val rankAColor: String,
    val rankBColor: String,
    val rankCColor: String,
    val rankDColor: String,
)
