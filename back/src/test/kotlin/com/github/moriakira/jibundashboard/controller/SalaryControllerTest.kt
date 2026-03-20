package com.github.moriakira.jibundashboard.controller

import com.github.moriakira.jibundashboard.component.CurrentAuth
import com.github.moriakira.jibundashboard.exception.ConflictException
import com.github.moriakira.jibundashboard.generated.model.Overview
import com.github.moriakira.jibundashboard.generated.model.PayslipData
import com.github.moriakira.jibundashboard.generated.model.PayslipDataDataInner
import com.github.moriakira.jibundashboard.generated.model.PostSalaryOcrTasksRequest
import com.github.moriakira.jibundashboard.generated.model.SalaryBase
import com.github.moriakira.jibundashboard.service.OverviewModel
import com.github.moriakira.jibundashboard.service.PayslipCategoryModel
import com.github.moriakira.jibundashboard.service.PayslipEntryModel
import com.github.moriakira.jibundashboard.service.SalaryModel
import com.github.moriakira.jibundashboard.service.SalaryOcrTaskModel
import com.github.moriakira.jibundashboard.service.SalaryOcrTaskService
import com.github.moriakira.jibundashboard.service.SalaryOcrTaskStatus
import com.github.moriakira.jibundashboard.service.SalaryService
import com.github.moriakira.jibundashboard.service.StructureModel
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.http.HttpStatus
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.*

class SalaryControllerTest :
    StringSpec({

        val currentAuth = mockk<CurrentAuth>(relaxed = true)
        val salaryService = mockk<SalaryService>(relaxed = true)
        val salaryOcrTaskService = mockk<SalaryOcrTaskService>(relaxed = true)
        val controller = SalaryController(currentAuth, salaryService, salaryOcrTaskService)

        beforeTest {
            every { currentAuth.userId } returns "u1"
            clearMocks(salaryService, salaryOcrTaskService, answers = false, recordedCalls = true, childMocks = true)
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

        @Suppress("LongParameterList")
        fun sampleOcrModel(
            id: String = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
            userId: String = "u1",
            status: SalaryOcrTaskStatus = SalaryOcrTaskStatus.PENDING,
            targetDate: String = "2025-11-01",
            createdAt: String = "2025-11-01T00:00:00Z",
            updatedAt: String = "2025-11-01T00:00:00Z",
        ): SalaryOcrTaskModel =
            SalaryOcrTaskModel(
                ocrTaskId = id,
                userId = userId,
                targetDate = targetDate,
                status = status,
                createdAt = createdAt,
                updatedAt = updatedAt,
            )

        "getSalary: パラメータなしなら list(userId, null, null, null) を呼ぶ" {
            every { salaryService.list("u1", null, null, null) } returns listOf(sampleModel())

            val res = controller.getSalaries(null, null, null)

            res.statusCode shouldBe HttpStatus.OK
            res.body!!.shouldHaveSize(1)
            res.body!![0].salaryId.toString() shouldBe "11111111-1111-1111-1111-111111111111"
            res.body!![0].targetDate shouldBe LocalDate.parse("2025-08-15")
            verify(exactly = 1) { salaryService.list("u1", null, null, null) }
        }

        "getSalary: データが空なら空配列を返す" {
            every { salaryService.list("u1", null, null, null) } returns emptyList()
            val res = controller.getSalaries(null, null, null)
            res.statusCode shouldBe HttpStatus.OK
            res.body!!.shouldHaveSize(0)
        }

        "getSalary: targetDate 指定なら list に targetDate を渡す" {
            every { salaryService.list("u1", "2025-01-01", null, null) } returns
                listOf(sampleModel(id = "22222222-2222-2222-2222-222222222222"))

            val res = controller.getSalaries(LocalDate.parse("2025-01-01"), null, null)

            res.statusCode shouldBe HttpStatus.OK
            res.body!![0].salaryId.toString() shouldBe "22222222-2222-2222-2222-222222222222"
            verify(exactly = 1) { salaryService.list("u1", "2025-01-01", null, null) }
        }

        "getSalary: 期間指定なら list に from/to を渡す" {
            every { salaryService.list("u1", null, "2025-01-01", "2025-12-31") } returns
                listOf(sampleModel(id = "33333333-3333-3333-3333-333333333333"))

            val res = controller.getSalaries(null, LocalDate.parse("2025-01-01"), LocalDate.parse("2025-12-31"))

            res.statusCode shouldBe HttpStatus.OK
            res.body!![0].salaryId.toString() shouldBe "33333333-3333-3333-3333-333333333333"
            verify(exactly = 1) { salaryService.list("u1", null, "2025-01-01", "2025-12-31") }
        }

        "postSalary: 新規作成なら 201 を返す (create 呼び出し)" {
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
            every { salaryService.create(any()) } returns returnedId

            val res = controller.postSalaries(req)

            res.statusCode shouldBe HttpStatus.CREATED
            res.body!!.salaryId.toString() shouldBe returnedId
            verify(exactly = 1) { salaryService.create(any()) }
            verify(exactly = 0) { salaryService.put(any()) }
        }

        "postSalary: null ボディなら IllegalArgumentException" {
            shouldThrow<IllegalArgumentException> { controller.postSalaries(null) }
        }

        "putSalary: 既存更新なら 200、所有者チェックOK" {
            val id = "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"
            every { salaryService.getByIdForUser(id, "u1") } returns sampleModel(id = id, userId = "u1")
            every { salaryService.put(any()) } returns id

            val req = SalaryBase(
                targetDate = LocalDate.parse("2025-08-15"),
                overview = Overview(0, 0, 0.0, 0.0, 0, 0),
                structure = com.github.moriakira.jibundashboard.generated.model.Structure(0, 0, 0, 0, 0),
                payslipData = emptyList(),
            )

            val res = controller.putSalariesById(UUID.fromString(id), req)

            res.statusCode shouldBe HttpStatus.OK
            res.body!!.salaryId.toString() shouldBe id
            verify(exactly = 1) { salaryService.getByIdForUser(id, "u1") }
            verify(exactly = 1) { salaryService.put(any()) }
        }

        "putSalary: 見つからなければ 404、put は呼ばれない" {
            val id = "cccccccc-cccc-cccc-cccc-cccccccccccc"
            every { salaryService.getByIdForUser(id, "u1") } returns null
            val req = SalaryBase(
                targetDate = LocalDate.parse("2025-08-15"),
                overview = Overview(0, 0, 0.0, 0.0, 0, 0),
                structure = com.github.moriakira.jibundashboard.generated.model.Structure(0, 0, 0, 0, 0),
                payslipData = emptyList(),
            )
            val res = controller.putSalariesById(UUID.fromString(id), req)
            res.statusCode shouldBe HttpStatus.NOT_FOUND
            verify(exactly = 0) { salaryService.put(any()) }
        }

        "getSalaryById: 所有者なら 200" {
            val id = "eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee"
            every { salaryService.getByIdForUser(id, "u1") } returns sampleModel(id = id, userId = "u1")

            val res = controller.getSalariesById(UUID.fromString(id))

            res.statusCode shouldBe HttpStatus.OK
            res.body!!.salaryId.toString() shouldBe id
        }

        "getSalaryById: 見つからなければ 404" {
            val id = "ffffffff-ffff-ffff-ffff-ffffffffffff"
            every { salaryService.getByIdForUser(id, "u1") } returns null

            val res = controller.getSalariesById(UUID.fromString(id))

            res.statusCode shouldBe HttpStatus.NOT_FOUND
        }

        "deleteSalary: 見つからなければ 404 (no-op)" {
            val id = "12121212-1212-1212-1212-121212121212"
            every { salaryService.getByIdForUser(id, "u1") } returns null

            val res = controller.deleteSalariesById(UUID.fromString(id))

            res.statusCode shouldBe HttpStatus.NOT_FOUND
            verify(exactly = 0) { salaryService.delete(any(), any()) }
        }

        "deleteSalary: 所有者なら削除して 204" {
            val id = "14141414-1414-1414-1414-141414141414"
            every { salaryService.getByIdForUser(id, "u1") } returns sampleModel(id = id, userId = "u1")

            val res = controller.deleteSalariesById(UUID.fromString(id))

            res.statusCode shouldBe HttpStatus.NO_CONTENT
            verify(exactly = 1) { salaryService.delete("u1", "2025-08-15") }
        }

        // ===== OCR タスク関連 =====
        "getSalaryOcrTask: 指定日の自分のタスク一覧を返す" {
            every { salaryOcrTaskService.listByUserAndDate("u1", "2025-11-01") } returns listOf(
                sampleOcrModel(
                    id = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
                    status = SalaryOcrTaskStatus.PENDING,
                    targetDate = "2025-11-01",
                    createdAt = "2025-11-01T00:00:00Z",
                    updatedAt = "2025-11-01T00:00:01Z",
                ),
                sampleOcrModel(
                    id = "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb",
                    status = SalaryOcrTaskStatus.COMPLETED,
                    targetDate = "2025-11-01",
                    createdAt = "2025-11-02T00:00:00Z",
                    updatedAt = "2025-11-02T00:00:01Z",
                ),
            )

            val res = controller.getSalaryOcrTasks(LocalDate.parse("2025-11-01"))

            res.statusCode shouldBe HttpStatus.OK
            res.body!!.shouldHaveSize(2)
            res.body!![0].ocrTaskId.toString() shouldBe "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"
            res.body!![0].status.name shouldBe "PENDING"
            res.body!![0].targetDate shouldBe LocalDate.parse("2025-11-01")
            res.body!![0].createdAt shouldBe OffsetDateTime.parse("2025-11-01T00:00:00Z")
            res.body!![0].updatedAt shouldBe OffsetDateTime.parse("2025-11-01T00:00:01Z")
            res.body!![1].ocrTaskId.toString() shouldBe "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"
            res.body!![1].status.name shouldBe "COMPLETED"
            verify(exactly = 1) { salaryOcrTaskService.listByUserAndDate("u1", "2025-11-01") }
        }

        "getSalaryOcrTaskById: 見つからなければ 404" {
            val id = "cccccccc-cccc-cccc-cccc-cccccccccccc"
            every { salaryOcrTaskService.getByTaskId(id) } returns null

            val res = controller.getSalaryOcrTasksById(UUID.fromString(id))

            res.statusCode shouldBe HttpStatus.NOT_FOUND
        }

        "getSalaryOcrTaskById: 他ユーザなら 404" {
            val id = "dddddddd-dddd-dddd-dddd-dddddddddddd"
            every { salaryOcrTaskService.getByTaskId(id) } returns sampleOcrModel(id = id, userId = "other")

            val res = controller.getSalaryOcrTasksById(UUID.fromString(id))

            res.statusCode shouldBe HttpStatus.NOT_FOUND
        }

        "getSalaryOcrTaskById: 所有者なら 200 で返す" {
            val id = "eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee"
            every { salaryOcrTaskService.getByTaskId(id) } returns sampleOcrModel(
                id = id,
                userId = "u1",
                status = SalaryOcrTaskStatus.RUNNING,
                targetDate = "2025-11-01",
                createdAt = "2025-11-03T00:00:00Z",
                updatedAt = "2025-11-03T00:00:01Z",
            )

            val res = controller.getSalaryOcrTasksById(UUID.fromString(id))

            res.statusCode shouldBe HttpStatus.OK
            res.body!!.ocrTaskId.toString() shouldBe id
            res.body!!.status.name shouldBe "RUNNING"
            res.body!!.targetDate shouldBe LocalDate.parse("2025-11-01")
        }

        "postSalaryOcrTask: null ボディなら IllegalArgumentException" {
            shouldThrow<IllegalArgumentException> { controller.postSalaryOcrTasks(null) }
        }

        "postSalaryOcrTask: 進行中タスクがあれば Service が ConflictException をスロー → Controller は伝播させる" {
            every {
                salaryOcrTaskService.startTask(
                    userId = "u1",
                    targetDate = "2025-11-01",
                    fileId = "f1f1f1f1-f1f1-f1f1-f1f1-f1f1f1f1f1f1",
                )
            } throws ConflictException("already in progress")

            val req = PostSalaryOcrTasksRequest(
                targetDate = LocalDate.parse("2025-11-01"),
                fileId = UUID.fromString("f1f1f1f1-f1f1-f1f1-f1f1-f1f1f1f1f1f1"),
            )

            shouldThrow<ConflictException> { controller.postSalaryOcrTasks(req) }
        }

        "postSalaryOcrTask: 実行可能ならキュー投入して 202 + ID 返す" {
            every {
                salaryOcrTaskService.startTask(
                    userId = "u1",
                    targetDate = "2025-11-01",
                    fileId = "11111111-1111-1111-1111-111111111111",
                )
            } returns "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"

            val req = PostSalaryOcrTasksRequest(
                targetDate = LocalDate.parse("2025-11-01"),
                fileId = UUID.fromString("11111111-1111-1111-1111-111111111111"),
            )

            val res = controller.postSalaryOcrTasks(req)

            res.statusCode shouldBe HttpStatus.ACCEPTED
            res.body!!.ocrTaskId.toString() shouldBe "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"
            verify(exactly = 1) {
                salaryOcrTaskService.startTask(
                    userId = "u1",
                    targetDate = "2025-11-01",
                    fileId = "11111111-1111-1111-1111-111111111111",
                )
            }
        }
    })
