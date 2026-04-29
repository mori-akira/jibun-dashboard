package com.github.moriakira.jibundashboard.controller

import com.github.moriakira.jibundashboard.generated.api.ShareApi
import com.github.moriakira.jibundashboard.generated.model.Overview
import com.github.moriakira.jibundashboard.generated.model.PayslipData
import com.github.moriakira.jibundashboard.generated.model.PayslipDataDataInner
import com.github.moriakira.jibundashboard.generated.model.Qualification
import com.github.moriakira.jibundashboard.generated.model.Salary
import com.github.moriakira.jibundashboard.generated.model.Setting
import com.github.moriakira.jibundashboard.generated.model.SettingQualification
import com.github.moriakira.jibundashboard.generated.model.SettingSalary
import com.github.moriakira.jibundashboard.generated.model.Structure
import com.github.moriakira.jibundashboard.generated.model.Vocabulary
import com.github.moriakira.jibundashboard.generated.model.VocabularyTag
import com.github.moriakira.jibundashboard.service.QualificationService
import com.github.moriakira.jibundashboard.service.SalaryService
import com.github.moriakira.jibundashboard.service.SettingService
import com.github.moriakira.jibundashboard.service.SharedLinkService
import com.github.moriakira.jibundashboard.service.VocabularyService
import com.github.moriakira.jibundashboard.service.VocabularyTagService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.net.URI
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

@RestController
class ShareController(
    private val sharedLinkService: SharedLinkService,
    private val salaryService: SalaryService,
    private val qualificationService: QualificationService,
    private val vocabularyService: VocabularyService,
    private val vocabularyTagService: VocabularyTagService,
    private val settingService: SettingService,
) : ShareApi {

    override fun getShareSetting(token: UUID): ResponseEntity<Setting> {
        val link = sharedLinkService.validateTokenOnly(token.toString())
        val setting = settingService.getOrDefault(link.userId)
        return ResponseEntity.ok(
            Setting(
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

    override fun getShareSalaries(token: UUID): ResponseEntity<List<Salary>> {
        val link = sharedLinkService.validateAndGet(token.toString(), "salary")
        val list = salaryService.listAll(link.userId).map { m ->
            Salary(
                salaryId = UUID.fromString(m.salaryId),
                targetDate = LocalDate.parse(m.targetDate),
                overview = Overview(
                    grossIncome = m.overview.grossIncome,
                    netIncome = m.overview.netIncome,
                    operatingTime = m.overview.operatingTime,
                    overtime = m.overview.overtime,
                    bonus = m.overview.bonus,
                    bonusTakeHome = m.overview.bonusTakeHome,
                ),
                structure = Structure(
                    basicSalary = m.structure.basicSalary,
                    overtimePay = m.structure.overtimePay,
                    housingAllowance = m.structure.housingAllowance,
                    positionAllowance = m.structure.positionAllowance,
                    other = m.structure.other,
                ),
                payslipData = m.payslipData.map { cat ->
                    PayslipData(
                        key = cat.key,
                        data = cat.data.map { e -> PayslipDataDataInner(key = e.key, data = e.data) },
                    )
                },
            )
        }
        return ResponseEntity.ok(list)
    }

    override fun getShareQualifications(token: UUID): ResponseEntity<List<Qualification>> {
        val link = sharedLinkService.validateAndGet(token.toString(), "qualification")
        val list = qualificationService.listAll(link.userId).map { m ->
            Qualification(
                qualificationId = UUID.fromString(m.qualificationId),
                order = m.order,
                qualificationName = m.qualificationName,
                abbreviation = m.abbreviation,
                version = m.version,
                status = Qualification.Status.forValue(m.status),
                rank = Qualification.Rank.forValue(m.rank),
                organization = m.organization,
                acquiredDate = m.acquiredDate?.let { LocalDate.parse(it) },
                expirationDate = m.expirationDate?.let { LocalDate.parse(it) },
                officialUrl = URI.create(m.officialUrl),
                certificationUrl = m.certificationUrl?.let { URI.create(it) },
                badgeUrl = m.badgeUrl?.let { URI.create(it) },
                certificationAssetId = null,
            )
        }
        return ResponseEntity.ok(list)
    }

    override fun getShareVocabularies(token: UUID): ResponseEntity<List<Vocabulary>> {
        val link = sharedLinkService.validateAndGet(token.toString(), "vocabulary")
        val list = vocabularyService.listByConditions(link.userId).map { m ->
            Vocabulary(
                vocabularyId = UUID.fromString(m.vocabularyId),
                name = m.name,
                description = m.description,
                createdDateTime = OffsetDateTime.parse(m.createdDateTime),
                updatedDateTime = OffsetDateTime.parse(m.updatedDateTime),
                tags = m.tags.map { t ->
                    VocabularyTag(
                        vocabularyTagId = UUID.fromString(t.vocabularyTagId),
                        vocabularyTag = t.vocabularyTag,
                        order = t.order,
                    )
                }.toSet(),
            )
        }
        return ResponseEntity.ok(list)
    }

    override fun getShareVocabularyTags(token: UUID): ResponseEntity<List<VocabularyTag>> {
        val link = sharedLinkService.validateAndGet(token.toString(), "vocabulary")
        val list = vocabularyTagService.listByConditions(link.userId).map { m ->
            VocabularyTag(
                vocabularyTagId = UUID.fromString(m.vocabularyTagId),
                vocabularyTag = m.vocabularyTag,
                order = m.order,
            )
        }
        return ResponseEntity.ok(list)
    }
}
