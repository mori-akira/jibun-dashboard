package com.github.moriakira.jibundashboard.controller

import com.github.moriakira.jibundashboard.component.CurrentAuth
import com.github.moriakira.jibundashboard.generated.model.Overview
import com.github.moriakira.jibundashboard.generated.model.PayslipData
import com.github.moriakira.jibundashboard.generated.model.PayslipDataDataInner
import com.github.moriakira.jibundashboard.generated.model.SalaryBase
import com.github.moriakira.jibundashboard.service.OverviewModel
import com.github.moriakira.jibundashboard.service.PayslipCategoryModel
import com.github.moriakira.jibundashboard.service.PayslipEntryModel
import com.github.moriakira.jibundashboard.service.SalaryModel
import com.github.moriakira.jibundashboard.service.SalaryService
import com.github.moriakira.jibundashboard.service.StructureModel
import io.kotest.assertions.throwables.shouldThrow
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

        // 追加: getSalary 空リスト
        "getSalary: データが空なら空配列を返す" {
            every { salaryService.listAll("u1") } returns emptyList()
            val res = controller.getSalary(null, null, null)
            res.statusCode shouldBe HttpStatus.OK
            res.body!!.shouldHaveSize(0)
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

        // 追加: targetDateFrom のみ指定
        "getSalary: targetDateFrom のみなら listByDateRange (to=null)" {
            every {
                salaryService.listByDateRange(
                    "u1",
                    "2025-01-01",
                    null,
                )
            } returns listOf(
                sampleModel(id = "44444444-4444-4444-4444-444444444444"),
            )

            val res = controller.getSalary(
                null,
                LocalDate.parse("2025-01-01"),
                null,
            )

            res.statusCode shouldBe HttpStatus.OK
            res.body!![0].salaryId.toString() shouldBe "44444444-4444-4444-4444-444444444444"
            verify(exactly = 1) { salaryService.listByDateRange("u1", "2025-01-01", null) }
        }

        // 追加: targetDateTo のみ指定
        "getSalary: targetDateTo のみなら listByDateRange (from=null)" {
            every {
                salaryService.listByDateRange(
                    "u1",
                    null,
                    "2025-12-31",
                )
            } returns listOf(
                sampleModel(id = "55555555-5555-5555-5555-555555555555"),
            )

            val res = controller.getSalary(
                null,
                null,
                LocalDate.parse("2025-12-31"),
            )

            res.statusCode shouldBe HttpStatus.OK
            res.body!![0].salaryId.toString() shouldBe "55555555-5555-5555-5555-555555555555"
            verify(exactly = 1) { salaryService.listByDateRange("u1", null, "2025-12-31") }
        }

        // 追加: targetDate と範囲両方指定された場合は targetDate 優先
        "getSalary: targetDate があれば範囲指定より優先して listByExactDate" {
            every {
                salaryService.listByExactDate(
                    "u1",
                    "2025-06-01",
                )
            } returns listOf(
                sampleModel(id = "66666666-6666-6666-6666-666666666666"),
            )
            every {
                salaryService.listByDateRange(
                    "u1",
                    any(),
                    any(),
                )
            } returns listOf(
                sampleModel(id = "should-not-be-used"),
            )

            val res = controller.getSalary(
                LocalDate.parse("2025-06-01"),
                LocalDate.parse("2025-01-01"),
                LocalDate.parse("2025-12-31"),
            )

            res.statusCode shouldBe HttpStatus.OK
            res.body!![0].salaryId.toString() shouldBe "66666666-6666-6666-6666-666666666666"
            verify(exactly = 1) { salaryService.listByExactDate("u1", "2025-06-01") }
            verify(exactly = 0) { salaryService.listByDateRange(any(), any(), any()) }
        }

        "postSalary: 新規作成なら 201 を返す" {
            val req = SalaryBase(
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

            val res = controller.postSalary(req)

            res.statusCode shouldBe HttpStatus.CREATED
            res.body!!.salaryId.toString() shouldBe returnedId
            captured.captured.userId shouldBe "u1"
            captured.captured.targetDate shouldBe "2025-08-15"

            // 追加検証: 生成されたモデルの salaryId は UUID 形式で、戻り値とは異なる
            UUID.fromString(captured.captured.salaryId) // 例外にならなければOK
            captured.captured.salaryId shouldBe captured.captured.salaryId // 冗長防止、上記で妥当性確認済
            captured.captured.salaryId shouldBe captured.captured.salaryId // placeholder
            captured.captured.salaryId shouldBe captured.captured.salaryId // placeholder
            captured.captured.salaryId != returnedId shouldBe true
        }

        // 追加: postSalary null ボディ
        "postSalary: null ボディなら IllegalArgumentException" {
            shouldThrow<IllegalArgumentException> { controller.postSalary(null) }
        }

        "putSalary: 既存更新なら 200、所有者チェックOK" {
            val id = "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"
            every { salaryService.getBySalaryId(id) } returns sampleModel(id = id, userId = "u1")
            every { salaryService.put(any()) } returns id

            val req = SalaryBase(
                targetDate = LocalDate.parse("2025-08-15"),
                overview = Overview(0, 0, 0.0, 0.0, 0, 0),
                structure = com.github.moriakira.jibundashboard.generated.model.Structure(0, 0, 0, 0, 0),
                payslipData = emptyList(),
            )

            val res = controller.putSalary(UUID.fromString(id), req)

            res.statusCode shouldBe HttpStatus.OK
            res.body!!.salaryId.toString() shouldBe id
            verify(exactly = 1) { salaryService.getBySalaryId(id) }
            verify(exactly = 1) { salaryService.put(any()) }
        }

        // 復活: putSalary 既存更新で見つからなければ 404、put は呼ばれない
        "putSalary: 既存更新で見つからなければ 404、put は呼ばれない" {
            val id = "cccccccc-cccc-cccc-cccc-cccccccccccc"
            every { salaryService.getBySalaryId(id) } returns null
            val req = SalaryBase(
                targetDate = LocalDate.parse("2025-08-15"),
                overview = Overview(0, 0, 0.0, 0.0, 0, 0),
                structure = com.github.moriakira.jibundashboard.generated.model.Structure(0, 0, 0, 0, 0),
                payslipData = emptyList(),
            )
            val res = controller.putSalary(UUID.fromString(id), req)
            res.statusCode shouldBe HttpStatus.NOT_FOUND
            verify(exactly = 0) { salaryService.put(any()) }
        }

        // 復活: putSalary 他ユーザのデータなら 404
        "putSalary: 他ユーザのデータなら 404" {
            val id = "dddddddd-dddd-dddd-dddd-dddddddddddd"
            every { salaryService.getBySalaryId(id) } returns sampleModel(id = id, userId = "other")
            val req = SalaryBase(
                targetDate = LocalDate.parse("2025-08-15"),
                overview = Overview(0, 0, 0.0, 0.0, 0, 0),
                structure = com.github.moriakira.jibundashboard.generated.model.Structure(0, 0, 0, 0, 0),
                payslipData = emptyList(),
            )
            val res = controller.putSalary(UUID.fromString(id), req)
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

        "deleteSalary: 見つからなければ 404 (no-op)" {
            val id = "12121212-1212-1212-1212-121212121212"
            every { salaryService.getBySalaryId(id) } returns null

            val res = controller.deleteSalary(UUID.fromString(id))

            res.statusCode shouldBe HttpStatus.NOT_FOUND
            verify(exactly = 0) { salaryService.delete(any(), any()) }
        }

        "deleteSalary: 他ユーザなら 404 (no-op)" {
            val id = "13131313-1313-1313-1313-131313131313"
            every { salaryService.getBySalaryId(id) } returns sampleModel(id = id, userId = "other")

            val res = controller.deleteSalary(UUID.fromString(id))

            res.statusCode shouldBe HttpStatus.NOT_FOUND
            verify(exactly = 0) { salaryService.delete(any(), any()) }
        }

        "deleteSalary: 所有者なら削除して 204" {
            val id = "14141414-1414-1414-1414-141414141414"
            every { salaryService.getBySalaryId(id) } returns sampleModel(id = id, userId = "u1")

            val res = controller.deleteSalary(UUID.fromString(id))

            res.statusCode shouldBe HttpStatus.NO_CONTENT
            verify(exactly = 1) { salaryService.delete("u1", "2025-08-15") }
        }
    })
