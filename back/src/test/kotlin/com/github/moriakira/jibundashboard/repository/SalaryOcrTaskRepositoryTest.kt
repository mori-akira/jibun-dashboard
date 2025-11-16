package com.github.moriakira.jibundashboard.repository

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import software.amazon.awssdk.core.pagination.sync.SdkIterable
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable
import software.amazon.awssdk.enhanced.dynamodb.Key
import software.amazon.awssdk.enhanced.dynamodb.TableSchema
import software.amazon.awssdk.enhanced.dynamodb.model.Page
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest

class SalaryOcrTaskRepositoryTest :
    StringSpec({

        val enhanced = mockk<DynamoDbEnhancedClient>()
        val table = mockk<DynamoDbTable<SalaryOcrTaskItem>>(relaxed = true)
        val gsi = mockk<DynamoDbIndex<SalaryOcrTaskItem>>()
        val repo = SalaryOcrTaskRepository(enhanced, "salary-ocr-tasks")
        val schema: TableSchema<SalaryOcrTaskItem> = TableSchema.fromBean(SalaryOcrTaskItem::class.java)

        fun item(taskId: String = "t1", user: String = "u1", date: String = "2025-01-01") = SalaryOcrTaskItem().apply {
            this.taskId = taskId
            this.userId = user
            this.targetDate = date
            this.status = "CREATED"
            this.createdAt = "2025-01-01T00:00:00Z"
            this.updatedAt = "2025-01-01T00:00:00Z"
        }

        "getByTaskId: 1件取得" {
            val it = item(taskId = "task-1")
            val key = Key.builder().partitionValue("task-1").build()
            every { enhanced.table("salary-ocr-tasks", schema) } returns table
            every { table.getItem(key) } returns it

            val res = repo.getByTaskId("task-1")

            res!!.taskId shouldBe "task-1"
            verify(exactly = 1) { table.getItem(key) }
        }

        "findByUserAndDate: GSIで一覧取得" {
            val p = mockk<Page<SalaryOcrTaskItem>>()
            every { enhanced.table("salary-ocr-tasks", schema) } returns table
            every { table.index("gsi_user_target_date") } returns gsi
            every { p.items() } returns listOf(
                item(taskId = "t1", user = "u1", date = "2025-01-01"),
                item(taskId = "t2", user = "u1", date = "2025-01-01"),
            )
            every { gsi.query(any<QueryEnhancedRequest>()) } returns SdkIterable { mutableListOf(p).iterator() }

            val res = repo.findByUserAndDate("u1", "2025-01-01")

            res.shouldHaveSize(2)
            res[0].taskId shouldBe "t1"
            verify(exactly = 1) { gsi.query(any<QueryEnhancedRequest>()) }
        }

        "put: 保存委譲" {
            every { enhanced.table("salary-ocr-tasks", schema) } returns table
            val it = item(taskId = "task-x", user = "uX", date = "2025-02-02")

            repo.put(it)

            verify(exactly = 1) { table.putItem(it) }
        }
    })
