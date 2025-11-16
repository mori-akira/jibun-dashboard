package com.github.moriakira.jibundashboard.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.moriakira.jibundashboard.repository.SalaryOcrTaskItem
import com.github.moriakira.jibundashboard.repository.SalaryOcrTaskRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import io.mockk.verify
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.*
import java.util.function.Consumer

class SalaryOcrTaskServiceTest :
    StringSpec({
        val repository = mockk<SalaryOcrTaskRepository>(relaxed = true)
        val sqsClient = mockk<SqsClient>(relaxed = true)
        val queueUrl = "https://sqs.local/queue"
        val service = SalaryOcrTaskService(repository, sqsClient, queueUrl)

        beforeTest {
            clearMocks(repository, sqsClient, answers = false, recordedCalls = true, childMocks = true)
        }

        "listByUserAndDate: Repository からの Item を Domain に変換して返す" {
            val i1 = SalaryOcrTaskItem().apply {
                taskId = "t1"
                userId = "u1"
                targetDate = "2025-10"
                status = "COMPLETED"
                createdAt = "2025-10-10T00:00:00Z"
                updatedAt = "2025-10-10T01:00:00Z"
            }
            val i2 = SalaryOcrTaskItem().apply {
                taskId = "t2"
                userId = "u1"
                targetDate = "2025-10"
                status = null // ステータス未設定は PENDING 扱い
                createdAt = "2025-10-11T00:00:00Z"
                updatedAt = "2025-10-11T01:00:00Z"
            }
            every { repository.findByUserAndDate("u1", "2025-10") } returns listOf(i1, i2)

            val result = service.listByUserAndDate("u1", "2025-10")

            result.shouldHaveSize(2)
            result[0] shouldBe SalaryOcrTaskModel(
                ocrTaskId = "t1",
                userId = "u1",
                targetDate = "2025-10",
                status = SalaryOcrTaskStatus.COMPLETED,
                createdAt = "2025-10-10T00:00:00Z",
                updatedAt = "2025-10-10T01:00:00Z",
            )
            result[1] shouldBe SalaryOcrTaskModel(
                ocrTaskId = "t2",
                userId = "u1",
                targetDate = "2025-10",
                status = SalaryOcrTaskStatus.PENDING,
                createdAt = "2025-10-11T00:00:00Z",
                updatedAt = "2025-10-11T01:00:00Z",
            )
            verify(exactly = 1) { repository.findByUserAndDate("u1", "2025-10") }
        }

        "getByTaskId: 見つからなければ null" {
            every { repository.getByTaskId("nope") } returns null

            val result = service.getByTaskId("nope")

            result shouldBe null
            verify(exactly = 1) { repository.getByTaskId("nope") }
        }

        "startTask: DynamoDB に保存し SQS に送信して taskId を返す" {
            // 固定 UUID と 固定時刻
            mockkStatic(UUID::class)
            mockkStatic(OffsetDateTime::class)
            val fixedId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
            val fixedNow = OffsetDateTime.parse("2023-02-03T04:05:06Z")
            every { UUID.randomUUID() } returns fixedId
            every { OffsetDateTime.now(ZoneOffset.UTC) } returns fixedNow

            // Repository への put の引数をキャプチャ
            val putSlot = slot<SalaryOcrTaskItem>()
            every { repository.put(capture(putSlot)) } returns Unit

            // SQS sendMessage の Consumer をキャプチャ
            val consumerSlot = slot<Consumer<SendMessageRequest.Builder>>()
            every { sqsClient.sendMessage(capture(consumerSlot)) } answers {
                // 適当なレスポンスを返す（不要だが型的に要求される）
                mockk(relaxed = true)
            }

            val returnedTaskId = service.startTask(
                userId = "u1",
                targetDate = "2025-11",
                fileId = "file-123",
            )

            // 返り値
            returnedTaskId shouldBe fixedId.toString()

            // Repository.put の中身検証
            verify(exactly = 1) { repository.put(any()) }
            putSlot.captured.taskId shouldBe fixedId.toString()
            putSlot.captured.userId shouldBe "u1"
            putSlot.captured.targetDate shouldBe "2025-11"
            putSlot.captured.status shouldBe SalaryOcrTaskStatus.PENDING.name
            putSlot.captured.createdAt shouldBe fixedNow.toString()
            putSlot.captured.updatedAt shouldBe fixedNow.toString()

            // SQS 送信内容検証
            verify(exactly = 1) { sqsClient.sendMessage(any<Consumer<SendMessageRequest.Builder>>()) }
            val builder = SendMessageRequest.builder()
            consumerSlot.captured.accept(builder)
            val built = builder.build()
            built.queueUrl() shouldBe queueUrl

            // JSON 本文の検証（順序非依存で Map 比較）
            val obj = jacksonObjectMapper().readValue(built.messageBody(), Map::class.java) as Map<*, *>
            obj["ocrTaskId"] shouldBe fixedId.toString()
            obj["userId"] shouldBe "u1"
            obj["targetDate"] shouldBe "2025-11"
            obj["fileId"] shouldBe "file-123"

            unmockkStatic(UUID::class)
            unmockkStatic(OffsetDateTime::class)
        }
    })
