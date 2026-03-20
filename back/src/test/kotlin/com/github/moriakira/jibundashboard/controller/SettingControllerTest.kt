package com.github.moriakira.jibundashboard.controller

import com.github.moriakira.jibundashboard.component.CurrentAuth
import com.github.moriakira.jibundashboard.generated.model.Setting
import com.github.moriakira.jibundashboard.generated.model.SettingQualification
import com.github.moriakira.jibundashboard.generated.model.SettingSalary
import com.github.moriakira.jibundashboard.service.QualificationSettingModel
import com.github.moriakira.jibundashboard.service.SalarySettingModel
import com.github.moriakira.jibundashboard.service.SettingModel
import com.github.moriakira.jibundashboard.service.SettingService
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.http.HttpStatus

class SettingControllerTest :
    StringSpec({

        val currentAuth = mockk<CurrentAuth>(relaxed = true)
        val settingService = mockk<SettingService>(relaxed = true)
        val controller = SettingController(currentAuth, settingService)

        beforeTest {
            every { currentAuth.userId } returns "u1"
        }

        @Suppress("LongParameterList")
        fun model(
            userId: String = "u1",
            financialYearStartMonth: Int = 4,
            transitionItemCount: Int = 12,
            compareDataColors: List<String> = listOf("#111111", "#222222"),
            rankAColor: String = "#AA0000",
            rankBColor: String = "#00BB00",
            rankCColor: String = "#0000CC",
            rankDColor: String = "#DDDDDD",
        ) = SettingModel(
            userId = userId,
            salary = SalarySettingModel(financialYearStartMonth, transitionItemCount, compareDataColors),
            qualification = QualificationSettingModel(rankAColor, rankBColor, rankCColor, rankDColor),
        )

        "getSetting: getOrDefault の結果を返す" {
            every { settingService.getOrDefault("u1") } returns model()

            val res = controller.getSettings()

            res.statusCode shouldBe HttpStatus.OK
            res.body!!.userId shouldBe "u1"
            res.body!!.salary.financialYearStartMonth shouldBe 4
            res.body!!.salary.transitionItemCount shouldBe 12
            res.body!!.salary.compareDataColors.shouldContainExactly("#111111", "#222222")
            res.body!!.qualification.rankAColor shouldBe "#AA0000"
            res.body!!.qualification.rankBColor shouldBe "#00BB00"
            res.body!!.qualification.rankCColor shouldBe "#0000CC"
            res.body!!.qualification.rankDColor shouldBe "#DDDDDD"
            verify(exactly = 1) { settingService.getOrDefault("u1") }
        }

        "getSetting: 未登録でも getOrDefault が初期化を担う (put/putDefault は Controller 側では呼ばない)" {
            every { settingService.getOrDefault("u1") } returns
                model(financialYearStartMonth = 1, transitionItemCount = 6)

            val res = controller.getSettings()

            res.statusCode shouldBe HttpStatus.OK
            res.body!!.salary.financialYearStartMonth shouldBe 1
            verify(exactly = 1) { settingService.getOrDefault("u1") }
            verify(exactly = 0) { settingService.putDefault(any()) }
            verify(exactly = 0) { settingService.get(any()) }
        }

        "putSetting: 設定を保存して200を返す" {
            val req = Setting(
                userId = null,
                salary = SettingSalary(
                    financialYearStartMonth = 3,
                    transitionItemCount = 24,
                    compareDataColors = listOf("#000000", "#FFFFFF"),
                ),
                qualification = SettingQualification(
                    rankAColor = "#FF0000",
                    rankBColor = "#00FF00",
                    rankCColor = "#0000FF",
                    rankDColor = "#888888",
                ),
            )

            every {
                settingService.put(
                    SettingModel(
                        userId = "u1",
                        salary = SalarySettingModel(3, 24, listOf("#000000", "#FFFFFF")),
                        qualification = QualificationSettingModel("#FF0000", "#00FF00", "#0000FF", "#888888"),
                    ),
                )
            } returns SettingModel(
                userId = "u1",
                salary = SalarySettingModel(3, 24, listOf("#000000", "#FFFFFF")),
                qualification = QualificationSettingModel("#FF0000", "#00FF00", "#0000FF", "#888888"),
            )

            val res = controller.putSettings(req)

            res.statusCode shouldBe HttpStatus.OK
            verify(exactly = 1) {
                settingService.put(
                    SettingModel(
                        userId = "u1",
                        salary = SalarySettingModel(3, 24, listOf("#000000", "#FFFFFF")),
                        qualification = QualificationSettingModel("#FF0000", "#00FF00", "#0000FF", "#888888"),
                    ),
                )
            }
        }

        "putSetting: null は例外" {
            shouldThrow<IllegalArgumentException> {
                controller.putSettings(null)
            }
        }
    })
