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

        "getSetting: 登録済み設定を返す" {
            val existing = SettingModel(
                userId = "u1",
                salary = SalarySettingModel(
                    financialYearStartMonth = 4,
                    transitionItemCount = 12,
                    compareDataColors = listOf("#111111", "#222222"),
                ),
                qualification = QualificationSettingModel(
                    rankAColor = "#AA0000",
                    rankBColor = "#00BB00",
                    rankCColor = "#0000CC",
                    rankDColor = "#DDDDDD",
                ),
            )
            every { settingService.getSetting("u1") } returns existing

            val res = controller.getSetting()

            res.statusCode shouldBe HttpStatus.OK
            res.body!!.userId shouldBe "u1"
            res.body!!.salary.financialYearStartMonth shouldBe 4
            res.body!!.salary.transitionItemCount shouldBe 12
            res.body!!.salary.compareDataColors.shouldContainExactly("#111111", "#222222")
            res.body!!.qualification.rankAColor shouldBe "#AA0000"
            res.body!!.qualification.rankBColor shouldBe "#00BB00"
            res.body!!.qualification.rankCColor shouldBe "#0000CC"
            res.body!!.qualification.rankDColor shouldBe "#DDDDDD"
            verify(exactly = 0) { settingService.putDefault(any()) }
        }

        "getSetting: 未登録なら初期登録して返す" {
            every { settingService.getSetting("u1") } returns null
            val created = SettingModel(
                userId = "u1",
                salary = SalarySettingModel(
                    financialYearStartMonth = 1,
                    transitionItemCount = 6,
                    compareDataColors = listOf("#123456", "#654321"),
                ),
                qualification = QualificationSettingModel(
                    rankAColor = "#A1A1A1",
                    rankBColor = "#B2B2B2",
                    rankCColor = "#C3C3C3",
                    rankDColor = "#D4D4D4",
                ),
            )
            every { settingService.putDefault("u1") } returns created

            val res = controller.getSetting()

            res.statusCode shouldBe HttpStatus.OK
            res.body!!.userId shouldBe "u1"
            res.body!!.salary.financialYearStartMonth shouldBe 1
            res.body!!.salary.transitionItemCount shouldBe 6
            res.body!!.salary.compareDataColors.shouldContainExactly("#123456", "#654321")
            res.body!!.qualification.rankAColor shouldBe "#A1A1A1"
            res.body!!.qualification.rankBColor shouldBe "#B2B2B2"
            res.body!!.qualification.rankCColor shouldBe "#C3C3C3"
            res.body!!.qualification.rankDColor shouldBe "#D4D4D4"
            verify(exactly = 1) { settingService.putDefault("u1") }
        }

        "putSetting: 設定を保存して200を返す" {
            val req = Setting(
                userId = null, // Controller 側で currentAuth.userId を使用
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
                settingService.putSetting(
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

            val res = controller.putSetting(req)

            res.statusCode shouldBe HttpStatus.OK
            verify(exactly = 1) {
                settingService.putSetting(
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
                controller.putSetting(null)
            }
        }
    })
