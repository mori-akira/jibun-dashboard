package com.github.moriakira.jibundashboard.service

import com.github.moriakira.jibundashboard.repository.QualificationSetting
import com.github.moriakira.jibundashboard.repository.SalarySetting
import com.github.moriakira.jibundashboard.repository.SettingItem
import com.github.moriakira.jibundashboard.repository.SettingRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import java.io.ByteArrayInputStream

class SettingServiceTest :
    StringSpec({

        val repository = mockk<SettingRepository>(relaxed = true)
        val resourceLoader = mockk<ResourceLoader>()

        fun yaml(): String = """
        salary:
          financialYearStartMonth: 4
          transitionItemCount: 12
          compareDataColors:
            - "#111111"
            - "#222222"
        qualification:
          rankAColor: "#AA0000"
          rankBColor: "#00BB00"
          rankCColor: "#0000CC"
          rankDColor: "#DDDDDD"
        """.trimIndent()

        fun mockDefaults() {
            val res = mockk<Resource>()
            every { res.exists() } returns true
            every { res.inputStream } answers { ByteArrayInputStream(yaml().toByteArray()) }
            every { resourceLoader.getResource("classpath:defaults/setting.yaml") } returns res
        }

        beforeTest {
            clearMocks(repository, answers = false, recordedCalls = true, childMocks = true)
        }

        "getSetting: 既存設定をドメインに変換（null はデフォルトで補完）" {
            mockDefaults()
            val service = SettingService(repository, resourceLoader)
            val item = SettingItem().apply {
                userId = "u1"
                salary = SalarySetting().apply {
                    financialYearStartMonth = null // デフォルト 4
                    transitionItemCount = 8 // そのまま
                    compareDataColors = null // デフォルト [#111111, #222222]
                }
                qualification = QualificationSetting().apply {
                    rankAColor = null // デフォルト
                    rankBColor = "#BBBBBB"
                    rankCColor = null // デフォルト
                    rankDColor = "#DDDDDD"
                }
            }
            every { repository.get("u1") } returns item

            val result = service.get("u1")!!

            result.userId shouldBe "u1"
            result.salary.financialYearStartMonth shouldBe 4
            result.salary.transitionItemCount shouldBe 8
            result.salary.compareDataColors.shouldContainExactly("#111111", "#222222")
            result.qualification.rankAColor shouldBe "#AA0000"
            result.qualification.rankBColor shouldBe "#BBBBBB"
            result.qualification.rankCColor shouldBe "#0000CC"
            result.qualification.rankDColor shouldBe "#DDDDDD"
        }

        "getSetting: 未登録なら null" {
            mockDefaults()
            val service = SettingService(repository, resourceLoader)
            every { repository.get("uX") } returns null

            service.get("uX") shouldBe null
        }

        "putSetting: モデルを保存して往復変換" {
            mockDefaults()
            val service = SettingService(repository, resourceLoader)
            val capt = slot<SettingItem>()
            every { repository.put(capture(capt)) } returns Unit

            val model = SettingModel(
                userId = "u9",
                salary = SalarySettingModel(3, 24, listOf("#000000", "#FFFFFF")),
                qualification = QualificationSettingModel("#FF0000", "#00FF00", "#0000FF", "#888888"),
            )

            val returned = service.put(model)

            returned.userId shouldBe "u9"
            returned.salary.financialYearStartMonth shouldBe 3
            returned.salary.transitionItemCount shouldBe 24
            returned.salary.compareDataColors.shouldContainExactly("#000000", "#FFFFFF")
            returned.qualification.rankAColor shouldBe "#FF0000"
            returned.qualification.rankBColor shouldBe "#00FF00"
            returned.qualification.rankCColor shouldBe "#0000FF"
            returned.qualification.rankDColor shouldBe "#888888"

            capt.captured.userId shouldBe "u9"
            capt.captured.salary!!.financialYearStartMonth shouldBe 3
            capt.captured.salary!!.transitionItemCount shouldBe 24
            capt.captured.salary!!.compareDataColors shouldBe listOf("#000000", "#FFFFFF")
        }

        "putDefault: 既存があればそれを返し、保存しない" {
            mockDefaults()
            val service = SettingService(repository, resourceLoader)
            val existing = SettingItem().apply {
                userId = "u1"
                salary = SalarySetting().apply {
                    financialYearStartMonth = 2
                    transitionItemCount = 10
                    compareDataColors = listOf("#1", "#2")
                }
                qualification = QualificationSetting().apply {
                    rankAColor = "#A"
                    rankBColor = "#B"
                    rankCColor = "#C"
                    rankDColor = "#D"
                }
            }
            every { repository.get("u1") } returns existing

            val res = service.putDefault("u1")

            res.userId shouldBe "u1"
            res.salary.financialYearStartMonth shouldBe 2
            verify(exactly = 0) { repository.put(any()) }
        }

        "putDefault: 未登録ならデフォルトを書き込み" {
            mockDefaults()
            val service = SettingService(repository, resourceLoader)
            every { repository.get("u2") } returns null
            val capt = slot<SettingItem>()
            every { repository.put(capture(capt)) } returns Unit

            val res = service.putDefault("u2")

            res.userId shouldBe "u2"
            res.salary.financialYearStartMonth shouldBe 4
            res.salary.transitionItemCount shouldBe 12
            res.salary.compareDataColors.shouldContainExactly("#111111", "#222222")
            res.qualification.rankAColor shouldBe "#AA0000"
            res.qualification.rankBColor shouldBe "#00BB00"
            res.qualification.rankCColor shouldBe "#0000CC"
            res.qualification.rankDColor shouldBe "#DDDDDD"

            capt.captured.userId shouldBe "u2"
            capt.captured.salary!!.financialYearStartMonth shouldBe 4
            capt.captured.salary!!.transitionItemCount shouldBe 12
            capt.captured.salary!!.compareDataColors shouldBe listOf("#111111", "#222222")
        }
    })
