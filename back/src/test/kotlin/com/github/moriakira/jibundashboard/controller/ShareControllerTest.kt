package com.github.moriakira.jibundashboard.controller

import com.github.moriakira.jibundashboard.service.OverviewModel
import com.github.moriakira.jibundashboard.service.QualificationModel
import com.github.moriakira.jibundashboard.service.QualificationService
import com.github.moriakira.jibundashboard.service.QualificationSettingModel
import com.github.moriakira.jibundashboard.service.SalaryModel
import com.github.moriakira.jibundashboard.service.SalaryService
import com.github.moriakira.jibundashboard.service.SalarySettingModel
import com.github.moriakira.jibundashboard.service.SettingModel
import com.github.moriakira.jibundashboard.service.SettingService
import com.github.moriakira.jibundashboard.service.SharedLinkModel
import com.github.moriakira.jibundashboard.service.SharedLinkService
import com.github.moriakira.jibundashboard.service.StructureModel
import com.github.moriakira.jibundashboard.service.VocabularyModel
import com.github.moriakira.jibundashboard.service.VocabularyService
import com.github.moriakira.jibundashboard.service.VocabularyTagModel
import com.github.moriakira.jibundashboard.service.VocabularyTagService
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.springframework.http.HttpStatus
import java.util.UUID

class ShareControllerTest :
    StringSpec({

        val sharedLinkService = mockk<SharedLinkService>(relaxed = true)
        val salaryService = mockk<SalaryService>(relaxed = true)
        val qualificationService = mockk<QualificationService>(relaxed = true)
        val vocabularyService = mockk<VocabularyService>(relaxed = true)
        val vocabularyTagService = mockk<VocabularyTagService>(relaxed = true)
        val settingService = mockk<SettingService>(relaxed = true)
        val controller = ShareController(
            sharedLinkService = sharedLinkService,
            salaryService = salaryService,
            qualificationService = qualificationService,
            vocabularyService = vocabularyService,
            vocabularyTagService = vocabularyTagService,
            settingService = settingService,
        )

        val token = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"

        fun link(dataTypes: List<String> = listOf("salary")) = SharedLinkModel(
            token = token,
            userId = "u1",
            dataTypes = dataTypes,
            expiresAt = "2099-12-31",
            shareUrl = "http://localhost:3333/share/$token",
        )

        "getShareSetting: 設定を返す" {
            every { sharedLinkService.validateTokenOnly(token) } returns link()
            every { settingService.getOrDefault("u1") } returns SettingModel(
                userId = "u1",
                salary = SalarySettingModel(
                    financialYearStartMonth = 4,
                    transitionItemCount = 12,
                    compareDataColors = listOf("#aaa"),
                ),
                qualification = QualificationSettingModel(
                    rankAColor = "#f00",
                    rankBColor = "#0f0",
                    rankCColor = "#00f",
                    rankDColor = "#aaa",
                ),
            )

            val res = controller.getShareSetting(UUID.fromString(token))
            res.statusCode shouldBe HttpStatus.OK
            res.body!!.salary.financialYearStartMonth shouldBe 4
        }

        "getShareSalaries: 給与一覧を返す" {
            every { sharedLinkService.validateAndGet(token, "salary") } returns link()
            every { salaryService.listAll("u1") } returns listOf(
                SalaryModel(
                    salaryId = "sal1",
                    userId = "u1",
                    targetDate = "2025-01-01",
                    overview = OverviewModel(
                        grossIncome = 500000,
                        netIncome = 380000,
                        operatingTime = 160.0,
                        overtime = 10.0,
                        bonus = 0,
                        bonusTakeHome = 0,
                    ),
                    structure = StructureModel(
                        basicSalary = 400000,
                        overtimePay = 50000,
                        housingAllowance = 30000,
                        positionAllowance = 20000,
                        other = 0,
                    ),
                    payslipData = emptyList(),
                ),
            )

            val res = controller.getShareSalaries(UUID.fromString(token))
            res.statusCode shouldBe HttpStatus.OK
            res.body!!.size shouldBe 1
            res.body!![0].overview.grossIncome shouldBe 500000
        }

        "getShareQualifications: 資格一覧を返す" {
            every { sharedLinkService.validateAndGet(token, "qualification") } returns link(listOf("qualification"))
            every { qualificationService.listAll("u1") } returns listOf(
                QualificationModel(
                    qualificationId = "q1",
                    userId = "u1",
                    order = 1,
                    qualificationName = "AWS SAA",
                    abbreviation = null,
                    version = null,
                    status = "acquired",
                    rank = "A",
                    organization = "AWS",
                    acquiredDate = "2024-06-01",
                    expirationDate = null,
                    officialUrl = "https://example.com",
                    certificationUrl = null,
                    badgeUrl = null,
                    certificationAssetId = null,
                ),
            )

            val res = controller.getShareQualifications(UUID.fromString(token))
            res.statusCode shouldBe HttpStatus.OK
            res.body!!.size shouldBe 1
            res.body!![0].qualificationName shouldBe "AWS SAA"
        }

        "getShareVocabularies: タグを含む語彙一覧を返す" {
            every { sharedLinkService.validateAndGet(token, "vocabulary") } returns link(listOf("vocabulary"))
            every { vocabularyService.listByConditions("u1") } returns listOf(
                VocabularyModel(
                    vocabularyId = "v1",
                    userId = "u1",
                    name = "Coroutine",
                    description = "Kotlin concurrency",
                    createdDateTime = "2025-01-01T00:00:00Z",
                    updatedDateTime = "2025-01-01T00:00:00Z",
                    tags = listOf(
                        VocabularyTagModel(
                            vocabularyTagId = "tag1",
                            userId = "u1",
                            vocabularyTag = "kotlin",
                            order = 1,
                        ),
                    ),
                ),
            )

            val res = controller.getShareVocabularies(UUID.fromString(token))
            res.statusCode shouldBe HttpStatus.OK
            res.body!!.size shouldBe 1
            res.body!![0].name shouldBe "Coroutine"
            res.body!![0].tags!!.size shouldBe 1
            res.body!![0].tags!!.first().vocabularyTag shouldBe "kotlin"
        }

        "getShareVocabularyTags: タグ一覧を返す" {
            every { sharedLinkService.validateAndGet(token, "vocabulary") } returns link(listOf("vocabulary"))
            every { vocabularyTagService.listByConditions("u1") } returns listOf(
                VocabularyTagModel(vocabularyTagId = "tag1", userId = "u1", vocabularyTag = "kotlin", order = 1),
            )

            val res = controller.getShareVocabularyTags(UUID.fromString(token))
            res.statusCode shouldBe HttpStatus.OK
            res.body!!.size shouldBe 1
            res.body!![0].vocabularyTag shouldBe "kotlin"
        }
    })
