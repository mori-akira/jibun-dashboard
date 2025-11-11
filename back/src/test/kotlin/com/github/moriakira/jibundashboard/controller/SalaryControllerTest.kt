package com.github.moriakira.jibundashboard.controller

import com.github.moriakira.jibundashboard.component.CurrentAuth
import com.github.moriakira.jibundashboard.generated.model.Overview
import com.github.moriakira.jibundashboard.generated.model.PayslipData
import com.github.moriakira.jibundashboard.generated.model.PayslipDataDataInner
import com.github.moriakira.jibundashboard.generated.model.Salary
import com.github.moriakira.jibundashboard.service.OverviewModel
import com.github.moriakira.jibundashboard.service.PayslipCategoryModel
import com.github.moriakira.jibundashboard.service.PayslipEntryModel
import com.github.moriakira.jibundashboard.service.SalaryModel
import com.github.moriakira.jibundashboard.service.SalaryService
import com.github.moriakira.jibundashboard.service.StructureModel
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.springframework.http.HttpStatus
import java.time.LocalDate
import java.util.*

class SalaryControllerTest :
    StringSpec({

        val currentAuth = mockk<CurrentAuth>(relaxed = true)
        val salaryService = mockk<SalaryService>(relaxed = true)
        val controller = SalaryController(currentAuth, salaryService)

        beforeTest {
            every { currentAuth.userId } returns "u1"
            // 直前のテストでの呼び出し履歴をクリア
            clearMocks(salaryService, answers = false, recordedCalls = true, childMocks = true)
        }

        fun sampleModel(id: String = "11111111-1111-1111-1111-111111111111", userId: String = "u1"): SalaryModel =
            SalaryModel(
                salaryId = id,
                userId = userId,
                targetDate = "2025-08-15",
                overview = OverviewModel(100, 80, 160.0, 10.0, 0, 0),
                structure = StructureModel(50, 10, 5, 3, 12),
                payslipData = listOf(
                    PayslipCategoryModel(
                        key = "earnings",
                        data = listOf(PayslipEntryModel("base", 100.0)),
                    ),
                ),
            )

        "getSalary: パラメータなしなら listAll を返す" {
            every { salaryService.listAll("u1") } returns listOf(sampleModel())

            val res = controller.getSalary(null, null, null)

            res.statusCode shouldBe HttpStatus.OK
            res.body!!.shouldHaveSize(1)
            res.body!![0].salaryId.toString() shouldBe "11111111-1111-1111-1111-111111111111"
            res.body!![0].targetDate shouldBe LocalDate.parse("2025-08-15")
            verify(exactly = 1) { salaryService.listAll("u1") }
            verify(exactly = 0) { salaryService.listByExactDate(any(), any()) }
            verify(exactly = 0) { salaryService.listByDateRange(any(), any(), any()) }
        }

        "getSalary: targetDate 指定なら listByExactDate" {
            every {
                salaryService.listByExactDate(
                    "u1",
                    "2025-01-01",
                )
            } returns listOf(sampleModel(id = "22222222-2222-2222-2222-222222222222"))

            val res = controller.getSalary(LocalDate.parse("2025-01-01"), null, null)

            res.statusCode shouldBe HttpStatus.OK
            res.body!![0].salaryId.toString() shouldBe "22222222-2222-2222-2222-222222222222"
            verify(exactly = 1) { salaryService.listByExactDate("u1", "2025-01-01") }
        }

        "getSalary: 期間指定なら listByDateRange" {
            every {
                salaryService.listByDateRange(
                    "u1",
                    "2025-01-01",
                    "2025-12-31",
                )
            } returns listOf(sampleModel(id = "33333333-3333-3333-3333-333333333333"))

            val res = controller.getSalary(null, LocalDate.parse("2025-01-01"), LocalDate.parse("2025-12-31"))

            res.statusCode shouldBe HttpStatus.OK
            res.body!![0].salaryId.toString() shouldBe "33333333-3333-3333-3333-333333333333"
            verify(exactly = 1) { salaryService.listByDateRange("u1", "2025-01-01", "2025-12-31") }
        }

        "putSalary: 新規作成なら 201 を返す" {
            val req = Salary(
                salaryId = null,
                userId = null,
                targetDate = LocalDate.parse("2025-08-15"),
                overview = Overview(100, 80, 160.0, 10.0, 0, 0),
                structure = com.github.moriakira.jibundashboard.generated.model.Structure(50, 10, 5, 3, 12),
                payslipData = listOf(
                    PayslipData(
                        key = "earnings",
                        data = listOf(PayslipDataDataInner("base", 100.0)),
                    ),
                ),
            )
            val returnedId = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"
            val captured = slot<SalaryModel>()
            every { salaryService.put(capture(captured)) } returns returnedId

            val res = controller.putSalary(req)

            res.statusCode shouldBe HttpStatus.CREATED
            res.body!!.salaryId.toString() shouldBe returnedId
            captured.captured.userId shouldBe "u1"
            captured.captured.targetDate shouldBe "2025-08-15"
        }

        "putSalary: 既存更新なら 200、所有者チェックOK" {
            val id = "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"
            every { salaryService.getBySalaryId(id) } returns sampleModel(id = id, userId = "u1")
            every { salaryService.put(any()) } returns id

            val req = Salary(
                salaryId = UUID.fromString(id),
                userId = null,
                targetDate = LocalDate.parse("2025-08-15"),
                overview = Overview(0, 0, 0.0, 0.0, 0, 0),
                structure = com.github.moriakira.jibundashboard.generated.model.Structure(0, 0, 0, 0, 0),
                payslipData = emptyList(),
            )

            val res = controller.putSalary(req)

            res.statusCode shouldBe HttpStatus.OK
            res.body!!.salaryId.toString() shouldBe id
            verify(exactly = 1) { salaryService.getBySalaryId(id) }
            verify(exactly = 1) { salaryService.put(any()) }
        }

        "putSalary: 既存更新で見つからなければ 404、put は呼ばれない" {
            val id = "cccccccc-cccc-cccc-cccc-cccccccccccc"
            every { salaryService.getBySalaryId(id) } returns null

            val req = Salary(
                salaryId = UUID.fromString(id),
                userId = null,
                targetDate = LocalDate.parse("2025-08-15"),
                overview = Overview(0, 0, 0.0, 0.0, 0, 0),
                structure = com.github.moriakira.jibundashboard.generated.model.Structure(0, 0, 0, 0, 0),
                payslipData = emptyList(),
            )

            val res = controller.putSalary(req)

            res.statusCode shouldBe HttpStatus.NOT_FOUND
            verify(exactly = 0) { salaryService.put(any()) }
        }

        "putSalary: 他ユーザのデータなら 404" {
            val id = "dddddddd-dddd-dddd-dddd-dddddddddddd"
            every { salaryService.getBySalaryId(id) } returns sampleModel(id = id, userId = "other")

            val req = Salary(
                salaryId = UUID.fromString(id),
                userId = null,
                targetDate = LocalDate.parse("2025-08-15"),
                overview = Overview(0, 0, 0.0, 0.0, 0, 0),
                structure = com.github.moriakira.jibundashboard.generated.model.Structure(0, 0, 0, 0, 0),
                payslipData = emptyList(),
            )

            val res = controller.putSalary(req)

            res.statusCode shouldBe HttpStatus.NOT_FOUND
            verify(exactly = 0) { salaryService.put(any()) }
        }

        "getSalaryById: 所有者なら 200" {
            val id = "eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee"
            every { salaryService.getBySalaryId(id) } returns sampleModel(id = id, userId = "u1")

            val res = controller.getSalaryById(UUID.fromString(id))

            res.statusCode shouldBe HttpStatus.OK
            res.body!!.salaryId.toString() shouldBe id
        }

        "getSalaryById: 見つからなければ 404" {
            val id = "ffffffff-ffff-ffff-ffff-ffffffffffff"
            every { salaryService.getBySalaryId(id) } returns null

            val res = controller.getSalaryById(UUID.fromString(id))

            res.statusCode shouldBe HttpStatus.NOT_FOUND
        }

        "getSalaryById: 他ユーザなら 404" {
            val id = "99999999-9999-9999-9999-999999999999"
            every { salaryService.getBySalaryId(id) } returns sampleModel(id = id, userId = "other")

            val res = controller.getSalaryById(UUID.fromString(id))

            res.statusCode shouldBe HttpStatus.NOT_FOUND
        }

        "deleteSalary: 見つからなければ 204 (no-op)" {
            val id = "12121212-1212-1212-1212-121212121212"
            every { salaryService.getBySalaryId(id) } returns null

            val res = controller.deleteSalary(UUID.fromString(id))

            res.statusCode shouldBe HttpStatus.NO_CONTENT
            verify(exactly = 0) { salaryService.deleteBySalaryId(any(), any()) }
        }

        "deleteSalary: 他ユーザなら 204 (no-op)" {
            val id = "13131313-1313-1313-1313-131313131313"
            every { salaryService.getBySalaryId(id) } returns sampleModel(id = id, userId = "other")

            val res = controller.deleteSalary(UUID.fromString(id))

            res.statusCode shouldBe HttpStatus.NO_CONTENT
            verify(exactly = 0) { salaryService.deleteBySalaryId(any(), any()) }
        }

        "deleteSalary: 所有者なら削除して 204" {
            val id = "14141414-1414-1414-1414-141414141414"
            every { salaryService.getBySalaryId(id) } returns sampleModel(id = id, userId = "u1")

            val res = controller.deleteSalary(UUID.fromString(id))

            res.statusCode shouldBe HttpStatus.NO_CONTENT
            verify(exactly = 1) { salaryService.deleteBySalaryId("u1", "2025-08-15") }
        }

        "getSalaryOcr: 既存のSalaryが存在しない場合は新規作成して201を返す" {
            val targetDate = LocalDate.parse("2025-09-01")
            val fileId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
            val newSalaryId = "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"
            val ocrResult = sampleModel(id = newSalaryId)

            every { salaryService.get("u1", "2025-09-01") } returns null
            every { salaryService.runOcr(any(), "u1", fileId, "2025-09-01") } returns ocrResult
            every { salaryService.put(ocrResult) } returns newSalaryId

            val res = controller.getSalaryOcr(targetDate, fileId)

            res.statusCode shouldBe HttpStatus.CREATED
            res.body!!.salaryId.toString() shouldBe newSalaryId
            verify(exactly = 1) { salaryService.get("u1", "2025-09-01") }
            verify(exactly = 1) { salaryService.runOcr(match { it.length == 36 }, "u1", fileId, "2025-09-01") }
            verify(exactly = 1) { salaryService.put(ocrResult) }
        }

        "getSalaryOcr: 既存のSalaryが存在する場合は更新して200を返す" {
            val targetDate = LocalDate.parse("2025-09-01")
            val fileId = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc")
            val existingSalaryId = "dddddddd-dddd-dddd-dddd-dddddddddddd"
            val existingModel = sampleModel(id = existingSalaryId)
            val ocrResult = sampleModel(id = existingSalaryId)

            every { salaryService.get("u1", "2025-09-01") } returns existingModel
            every { salaryService.runOcr(existingSalaryId, "u1", fileId, "2025-09-01") } returns ocrResult
            every { salaryService.put(ocrResult) } returns existingSalaryId

            val res = controller.getSalaryOcr(targetDate, fileId)

            res.statusCode shouldBe HttpStatus.OK
            res.body!!.salaryId.toString() shouldBe existingSalaryId
            verify(exactly = 1) { salaryService.get("u1", "2025-09-01") }
            verify(exactly = 1) { salaryService.runOcr(existingSalaryId, "u1", fileId, "2025-09-01") }
            verify(exactly = 1) { salaryService.put(ocrResult) }
        }
    })
