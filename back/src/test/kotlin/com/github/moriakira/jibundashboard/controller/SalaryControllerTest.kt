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

        "getSalary: パラメータなし/targetDate/期間でそれぞれ正しい list を呼ぶ" {
            every { salaryService.list("u1", null, null, null) } returns listOf(sampleModel())
            every { salaryService.list("u1", "2025-01-01", null, null) } returns
                listOf(sampleModel(id = "22222222-2222-2222-2222-222222222222"))
            every { salaryService.list("u1", null, "2025-01-01", "2025-12-31") } returns
                listOf(sampleModel(id = "33333333-3333-3333-3333-333333333333"))

            controller.getSalaries(null, null, null).also {
                it.statusCode shouldBe HttpStatus.OK
                it.body!!.shouldHaveSize(1)
                it.body!![0].salaryId.toString() shouldBe "11111111-1111-1111-1111-111111111111"
            }
            controller.getSalaries(LocalDate.parse("2025-01-01"), null, null).also {
                it.body!![0].salaryId.toString() shouldBe "22222222-2222-2222-2222-222222222222"
            }
            controller.getSalaries(null, LocalDate.parse("2025-01-01"), LocalDate.parse("2025-12-31")).also {
                it.body!![0].salaryId.toString() shouldBe "33333333-3333-3333-3333-333333333333"
            }
        }

        "postSalary: 201 を返す" {
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
            every { salaryService.create(any()) } returns "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"

            val res = controller.postSalaries(req)

            res.statusCode shouldBe HttpStatus.CREATED
            res.body!!.salaryId.toString() shouldBe "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"
            verify(exactly = 0) { salaryService.put(any()) }
        }

        "putSalary: 200 または 404" {
            val id = "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"
            val missingId = "cccccccc-cccc-cccc-cccc-cccccccccccc"
            every { salaryService.getByIdForUser(id, "u1") } returns sampleModel(id = id)
            every { salaryService.getByIdForUser(missingId, "u1") } returns null
            every { salaryService.put(any()) } returns id

            val req = SalaryBase(
                targetDate = LocalDate.parse("2025-08-15"),
                overview = Overview(0, 0, 0.0, 0.0, 0, 0),
                structure = com.github.moriakira.jibundashboard.generated.model.Structure(0, 0, 0, 0, 0),
                payslipData = emptyList(),
            )

            controller.putSalariesById(UUID.fromString(id), req).statusCode shouldBe HttpStatus.OK
            controller.putSalariesById(UUID.fromString(missingId), req).also {
                it.statusCode shouldBe HttpStatus.NOT_FOUND
                verify(exactly = 0) { salaryService.put(any()) }
            }
        }

        "getSalaryById: 200 または 404" {
            val id = "eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee"
            val missingId = "ffffffff-ffff-ffff-ffff-ffffffffffff"
            every { salaryService.getByIdForUser(id, "u1") } returns sampleModel(id = id)
            every { salaryService.getByIdForUser(missingId, "u1") } returns null

            controller.getSalariesById(UUID.fromString(id)).statusCode shouldBe HttpStatus.OK
            controller.getSalariesById(UUID.fromString(missingId)).statusCode shouldBe HttpStatus.NOT_FOUND
        }

        "deleteSalary: 204 または 404" {
            val id = "14141414-1414-1414-1414-141414141414"
            val missingId = "12121212-1212-1212-1212-121212121212"
            every { salaryService.getByIdForUser(id, "u1") } returns sampleModel(id = id)
            every { salaryService.getByIdForUser(missingId, "u1") } returns null

            controller.deleteSalariesById(UUID.fromString(id)).statusCode shouldBe HttpStatus.NO_CONTENT
            controller.deleteSalariesById(UUID.fromString(missingId)).also {
                it.statusCode shouldBe HttpStatus.NOT_FOUND
                verify(exactly = 0) { salaryService.delete(any(), any()) }
            }
        }

        // ===== OCR タスク関連 =====
        "getSalaryOcrTask: 指定日の自分のタスク一覧を返す" {
            every { salaryOcrTaskService.listByUserAndDate("u1", "2025-11-01") } returns listOf(
                sampleOcrModel(
                    id = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
                    status = SalaryOcrTaskStatus.PENDING,
                    createdAt = "2025-11-01T00:00:00Z",
                    updatedAt = "2025-11-01T00:00:01Z",
                ),
                sampleOcrModel(
                    id = "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb",
                    status = SalaryOcrTaskStatus.COMPLETED,
                    createdAt = "2025-11-02T00:00:00Z",
                    updatedAt = "2025-11-02T00:00:01Z",
                ),
            )

            val res = controller.getSalaryOcrTasks(LocalDate.parse("2025-11-01"))

            res.statusCode shouldBe HttpStatus.OK
            res.body!!.shouldHaveSize(2)
            res.body!![0].ocrTaskId.toString() shouldBe "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"
            res.body!![0].status.name shouldBe "PENDING"
            res.body!![0].createdAt shouldBe OffsetDateTime.parse("2025-11-01T00:00:00Z")
            res.body!![1].status.name shouldBe "COMPLETED"
        }

        "getSalaryOcrTaskById: 所有者なら 200、それ以外は 404" {
            val id = "eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee"
            val missingId = "cccccccc-cccc-cccc-cccc-cccccccccccc"
            val otherId = "dddddddd-dddd-dddd-dddd-dddddddddddd"
            every { salaryOcrTaskService.getByTaskId(id) } returns sampleOcrModel(
                id = id,
                status = SalaryOcrTaskStatus.RUNNING,
            )
            every { salaryOcrTaskService.getByTaskId(missingId) } returns null
            every { salaryOcrTaskService.getByTaskId(otherId) } returns sampleOcrModel(id = otherId, userId = "other")

            controller.getSalaryOcrTasksById(UUID.fromString(id)).statusCode shouldBe HttpStatus.OK
            controller.getSalaryOcrTasksById(UUID.fromString(missingId)).statusCode shouldBe HttpStatus.NOT_FOUND
            controller.getSalaryOcrTasksById(UUID.fromString(otherId)).statusCode shouldBe HttpStatus.NOT_FOUND
        }

        "postSalaryOcrTask: 進行中タスクがあれば ConflictException" {
            every {
                salaryOcrTaskService.startTask(
                    userId = "u1",
                    targetDate = "2025-11-01",
                    fileId = "f1f1f1f1-f1f1-f1f1-f1f1-f1f1f1f1f1f1",
                )
            } throws ConflictException("already in progress")

            shouldThrow<ConflictException> {
                controller.postSalaryOcrTasks(
                    PostSalaryOcrTasksRequest(
                        targetDate = LocalDate.parse("2025-11-01"),
                        fileId = UUID.fromString("f1f1f1f1-f1f1-f1f1-f1f1-f1f1f1f1f1f1"),
                    ),
                )
            }
        }

        "postSalaryOcrTask: 実行可能ならキュー投入して 202 + ID 返す" {
            every {
                salaryOcrTaskService.startTask(
                    userId = "u1",
                    targetDate = "2025-11-01",
                    fileId = "11111111-1111-1111-1111-111111111111",
                )
            } returns "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"

            val res = controller.postSalaryOcrTasks(
                PostSalaryOcrTasksRequest(
                    targetDate = LocalDate.parse("2025-11-01"),
                    fileId = UUID.fromString("11111111-1111-1111-1111-111111111111"),
                ),
            )

            res.statusCode shouldBe HttpStatus.ACCEPTED
            res.body!!.ocrTaskId.toString() shouldBe "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"
        }
    })
