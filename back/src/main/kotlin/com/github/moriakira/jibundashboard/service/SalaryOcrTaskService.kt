package com.github.moriakira.jibundashboard.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.moriakira.jibundashboard.repository.SalaryOcrTaskItem
import com.github.moriakira.jibundashboard.repository.SalaryOcrTaskRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.sqs.SqsClient
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

@Service
class SalaryOcrTaskService(
    private val salaryOcrTaskRepository: SalaryOcrTaskRepository,
    private val sqsClient: SqsClient,
    @param:Value("\${app.sqs.salary-ocr-queue-url}") private val salaryOcrQueueUrl: String,
) {

    private val objectMapper = jacksonObjectMapper()

    fun listByUserAndDate(userId: String, targetDate: String): List<SalaryOcrTaskModel> =
        salaryOcrTaskRepository.findByUserAndDate(userId, targetDate).map { it.toDomain() }

    fun getByTaskId(taskId: String): SalaryOcrTaskModel? =
        salaryOcrTaskRepository.getByTaskId(taskId)?.toDomain()

    fun startTask(
        userId: String,
        targetDate: String,
        fileId: String,
    ): String {
        val now = OffsetDateTime.now(ZoneOffset.UTC).toString()
        val taskId = UUID.randomUUID().toString()

        val model = SalaryOcrTaskModel(
            ocrTaskId = taskId,
            userId = userId,
            targetDate = targetDate,
            status = SalaryOcrTaskStatus.PENDING,
            createdAt = now,
            updatedAt = now,
        )

        // DynamoDBに保存
        salaryOcrTaskRepository.put(model.toItem())

        // SQSにメッセージ送信
        val body = objectMapper.writeValueAsString(
            mapOf(
                "ocrTaskId" to taskId,
                "userId" to userId,
                "targetDate" to targetDate,
                "fileId" to fileId,
            ),
        )
        sqsClient.sendMessage { builder ->
            builder.queueUrl(salaryOcrQueueUrl)
                .messageBody(body)
        }

        return taskId
    }
}

data class SalaryOcrTaskModel(
    val ocrTaskId: String,
    val userId: String,
    val targetDate: String,
    val status: SalaryOcrTaskStatus,
    val createdAt: String,
    val updatedAt: String,
)

enum class SalaryOcrTaskStatus {
    PENDING,
    RUNNING,
    COMPLETED,
    FAILED,
}

private fun SalaryOcrTaskItem.toDomain(): SalaryOcrTaskModel =
    SalaryOcrTaskModel(
        ocrTaskId = this.taskId!!,
        userId = this.userId!!,
        targetDate = this.targetDate!!,
        status = SalaryOcrTaskStatus.valueOf(this.status ?: SalaryOcrTaskStatus.PENDING.name),
        createdAt = this.createdAt!!,
        updatedAt = this.updatedAt!!,
    )

private fun SalaryOcrTaskModel.toItem(): SalaryOcrTaskItem =
    SalaryOcrTaskItem().also { item ->
        item.taskId = this.ocrTaskId
        item.userId = this.userId
        item.targetDate = this.targetDate
        item.status = this.status.name
        item.createdAt = this.createdAt
        item.updatedAt = this.updatedAt
    }
