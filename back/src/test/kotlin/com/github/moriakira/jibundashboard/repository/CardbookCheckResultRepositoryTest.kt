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
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest

class CardbookCheckResultRepositoryTest :
    StringSpec({

        val enhanced = mockk<DynamoDbEnhancedClient>()
        val table = mockk<DynamoDbTable<CardbookCheckResultItem>>(relaxed = true)
        val gsi = mockk<DynamoDbIndex<CardbookCheckResultItem>>()
        val repo = CardbookCheckResultRepository(enhanced, "cardbook-check-results")
        val schema: TableSchema<CardbookCheckResultItem> =
            TableSchema.fromBean(CardbookCheckResultItem::class.java)

        fun item(
            checkResultId: String = "r1",
            cardId: String = "c1",
            userId: String = "u1",
        ) = CardbookCheckResultItem().apply {
            this.cardbookCheckResultId = checkResultId
            this.cardId = cardId
            this.cardbookId = "cb1"
            this.userId = userId
            this.front = "ephemeral"
            this.severity = "HIGH"
            this.status = "UNCHECKED"
            this.report = "## report"
            this.cardUpdatedAt = "2025-05-01T00:00:00Z"
            this.checkedAt = "2025-06-01T00:00:00Z"
        }

        "findByUser: userId でクエリして一覧を返す" {
            val page = mockk<Page<CardbookCheckResultItem>>()
            val iterable = mockk<PageIterable<CardbookCheckResultItem>>()
            every { enhanced.table("cardbook-check-results", schema) } returns table
            every { page.items() } returns listOf(item("r1", "c1"), item("r2", "c2"))
            every { iterable.iterator() } returns mutableListOf(page).iterator()
            every { table.query(any<QueryEnhancedRequest>()) } returns iterable

            val result = repo.findByUser("u1")

            result.shouldHaveSize(2)
            verify(exactly = 1) { table.query(any<QueryEnhancedRequest>()) }
        }

        "getByCheckResultId: GSI で1件返す" {
            val page = mockk<Page<CardbookCheckResultItem>>()
            every { enhanced.table("cardbook-check-results", schema) } returns table
            every { table.index("gsi_cardbook_check_result_id") } returns gsi
            every { page.items() } returns listOf(item(checkResultId = "r1"))
            every { gsi.query(any<QueryEnhancedRequest>()) } returns SdkIterable { mutableListOf(page).iterator() }

            repo.getByCheckResultId("r1")!!.cardbookCheckResultId shouldBe "r1"
            verify(exactly = 1) { gsi.query(any<QueryEnhancedRequest>()) }
        }

        "put/delete: 委譲" {
            every { enhanced.table("cardbook-check-results", schema) } returns table
            val it = item()

            repo.put(it)
            verify(exactly = 1) { table.putItem(it) }

            val delKey = Key.builder().partitionValue("u1").sortValue("c1").build()
            every { table.deleteItem(delKey) } returns it
            repo.delete("u1", "c1")
            verify(exactly = 1) { table.deleteItem(delKey) }
        }
    })
