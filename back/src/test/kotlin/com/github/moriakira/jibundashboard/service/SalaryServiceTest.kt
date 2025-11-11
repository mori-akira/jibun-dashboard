package com.github.moriakira.jibundashboard.service

import com.github.moriakira.jibundashboard.prompts.SalaryPayslipOcrPromptsProvider
import com.github.moriakira.jibundashboard.repository.SalaryItem
import com.github.moriakira.jibundashboard.repository.SalaryRepository
import com.openai.client.OpenAIClient
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import software.amazon.awssdk.services.s3.S3Client

class SalaryServiceTest :
    StringSpec({

        val repository = mockk<SalaryRepository>(relaxed = true)
        val promptsProvider = mockk<SalaryPayslipOcrPromptsProvider>(relaxed = true)
        val s3Client = mockk<S3Client>(relaxed = true)
        val openAIClient = mockk<OpenAIClient>(relaxed = true)
        val fileUploadService = mockk<FileUploadService>(relaxed = true)
        val uploadsBucketName = "uploads-bucket"
        val openAIModel = "test-model"

        val service = SalaryService(
            repository,
            promptsProvider,
            s3Client,
            openAIClient,
            fileUploadService,
            uploadsBucketName,
            openAIModel,
        )

        beforeTest {
            clearMocks(
                repository,
                promptsProvider,
                s3Client,
                openAIClient,
                fileUploadService,
                answers = false,
                recordedCalls = true,
                childMocks = true,
            )
        }

        fun item(
            salaryId: String = "s1",
            userId: String = "u1",
            date: String = "2025-08-15",
            withData: Boolean = true,
        ): SalaryItem = SalaryItem().apply {
            this.salaryId = salaryId
            this.userId = userId
            this.targetDate = date
            this.overview = SalaryItem.Overview().apply {
                if (withData) {
                    grossIncome = 100
                    netIncome = 80
                    operatingTime = 160.0
                    overtime = 10.0
                    bonus = 5
                    bonusTakeHome = 4
                } else {
                    // 全て null のまま（default 埋めを検証）
                }
            }
            this.structure = SalaryItem.Structure().apply {
                if (withData) {
                    basicSalary = 50
                    overtimePay = 10
                    housingAllowance = 5
                    positionAllowance = 3
                    other = 12
                } else {
                    // null のまま
                }
            }
            this.payslipData = if (withData) {
                listOf(
                    SalaryItem.PayslipCategory().apply {
                        key = "earnings"
                        data = listOf(
                            SalaryItem.PayslipEntry().apply {
                                key = "base"
                                data = 100.0
                            },
                        )
                    },
                )
            } else {
                null
            }
        }

        "listAll: リストをドメインに変換" {
            every { repository.findByUser("u1") } returns listOf(item())

            val res = service.listAll("u1")

            res.shouldHaveSize(1)
            res[0].salaryId shouldBe "s1"
            res[0].userId shouldBe "u1"
            res[0].targetDate shouldBe "2025-08-15"
            res[0].overview.grossIncome shouldBe 100
            res[0].structure.basicSalary shouldBe 50
            res[0].payslipData[0].key shouldBe "earnings"
            res[0].payslipData[0].data[0].key shouldBe "base"
        }

        "get: userId と targetDate で取得" {
            every { repository.get("u1", "2025-08-15") } returns item()

            val res = service.get("u1", "2025-08-15")

            res!!.salaryId shouldBe "s1"
            res.userId shouldBe "u1"
            res.targetDate shouldBe "2025-08-15"
            verify(exactly = 1) { repository.get("u1", "2025-08-15") }
        }

        "get: 存在しなければ null" {
            every { repository.get("u1", "2025-01-01") } returns null

            val res = service.get("u1", "2025-01-01")

            res shouldBe null
        }

        "listByExactDate: 指定日のみを返す" {
            every {
                repository.findByUserAndDate(
                    "u1",
                    "2025-01-01",
                )
            } returns listOf(item(salaryId = "s2", date = "2025-01-01"))

            val res = service.listByExactDate("u1", "2025-01-01")

            res.shouldHaveSize(1)
            res[0].salaryId shouldBe "s2"
            res[0].targetDate shouldBe "2025-01-01"
            verify(exactly = 1) { repository.findByUserAndDate("u1", "2025-01-01") }
        }

        "listByDateRange: 期間で絞り込む" {
            every {
                repository.findByUserAndDateRange(
                    "u1",
                    "2025-01-01",
                    "2025-12-31",
                )
            } returns listOf(item(salaryId = "s3"))

            val res = service.listByDateRange("u1", "2025-01-01", "2025-12-31")

            res.shouldHaveSize(1)
            res[0].salaryId shouldBe "s3"
            verify(exactly = 1) { repository.findByUserAndDateRange("u1", "2025-01-01", "2025-12-31") }
        }

        "getBySalaryId: 存在すれば返す、無ければ null" {
            every { repository.getBySalaryId("sid-1") } returns item(salaryId = "sid-1")
            every { repository.getBySalaryId("nope") } returns null

            service.getBySalaryId("sid-1")!!.salaryId shouldBe "sid-1"
            service.getBySalaryId("nope") shouldBe null
        }

        "put: 渡したモデルが保存され salaryId が返る" {
            val capt = slot<SalaryItem>()
            every { repository.put(capture(capt)) } returns Unit

            val model = SalaryModel(
                salaryId = "sid-9",
                userId = "u9",
                targetDate = "2025-02-02",
                overview = OverviewModel(1, 2, 3.0, 4.0, 5, 6),
                structure = StructureModel(7, 8, 9, 10, 11),
                payslipData = listOf(
                    PayslipCategoryModel("k", listOf(PayslipEntryModel("e", 12.3))),
                ),
            )

            val returned = service.put(model)

            returned shouldBe "sid-9"
            capt.captured.salaryId shouldBe "sid-9"
            capt.captured.userId shouldBe "u9"
            capt.captured.targetDate shouldBe "2025-02-02"
            capt.captured.overview!!.grossIncome shouldBe 1
            capt.captured.structure!!.basicSalary shouldBe 7
            capt.captured.payslipData!![0].key shouldBe "k"
            capt.captured.payslipData!![0].data!![0].key shouldBe "e"
        }

        "deleteBySalaryId: 指定キーで削除を委譲" {
            service.deleteBySalaryId("u1", "2025-08-15")
            verify(exactly = 1) { repository.delete("u1", "2025-08-15") }
        }

        "toDomain: null はデフォルトで埋め、payslipData は空に" {
            every { repository.findByUser("u1") } returns listOf(item(withData = false))

            val res = service.listAll("u1")[0]
            res.overview.grossIncome shouldBe 0
            res.overview.netIncome shouldBe 0
            res.overview.operatingTime shouldBe 0.0
            res.overview.overtime shouldBe 0.0
            res.overview.bonus shouldBe 0
            res.overview.bonusTakeHome shouldBe 0
            res.structure.basicSalary shouldBe 0
            res.structure.overtimePay shouldBe 0
            res.structure.housingAllowance shouldBe 0
            res.structure.positionAllowance shouldBe 0
            res.structure.other shouldBe 0
            res.payslipData.isEmpty() shouldBe true
        }

        "runOcr: S3 からダウンロードして OCR を実行し結果を返す" {
            val salaryId = "s-ocr-1"
            val userId = "u-ocr-1"
            val date = "2025-09-01"
            val fileId = java.util.UUID.randomUUID()
            val s3Key = "uploads/u-ocr-1/$fileId.pdf"

            every { fileUploadService.uploadKey(userId, fileId) } returns s3Key
            every {
                s3Client.getObject(
                    any<software.amazon.awssdk.services.s3.model.GetObjectRequest>(),
                    @Suppress("MaxLineLength")
                    any<software.amazon.awssdk.core.sync.ResponseTransformer<software.amazon.awssdk.services.s3.model.GetObjectResponse, *>>(),
                )
            } returns mockk()

            val ocrResult = OcrResult(
                overview = OverviewModel(200, 180, 168.0, 8.0, 0, 0),
                structure = StructureModel(120, 30, 10, 5, 35),
                payslipData = listOf(
                    PayslipCategoryModel("earnings", listOf(PayslipEntryModel("base", 120.0))),
                ),
            )

            val spy = io.mockk.spyk(service)
            every { spy["queryOpenAI"](any<java.nio.file.Path>()) } returns ocrResult

            val res = spy.runOcr(salaryId, userId, fileId, date)

            res.salaryId shouldBe salaryId
            res.userId shouldBe userId
            res.targetDate shouldBe date
            res.overview.grossIncome shouldBe 200
            res.structure.basicSalary shouldBe 120
            res.payslipData.shouldHaveSize(1)
            res.payslipData[0].key shouldBe "earnings"
            res.payslipData[0].data[0].key shouldBe "base"

            verify(exactly = 1) { fileUploadService.uploadKey(userId, fileId) }
            verify(atLeast = 1) {
                s3Client.getObject(
                    any<software.amazon.awssdk.services.s3.model.GetObjectRequest>(),
                    @Suppress("MaxLineLength")
                    any<software.amazon.awssdk.core.sync.ResponseTransformer<software.amazon.awssdk.services.s3.model.GetObjectResponse, *>>(),
                )
            }
        }
    })
