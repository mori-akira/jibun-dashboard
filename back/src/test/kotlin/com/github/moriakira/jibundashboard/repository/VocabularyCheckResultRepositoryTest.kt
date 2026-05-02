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

class VocabularyCheckResultRepositoryTest :
    StringSpec({

        val enhanced = mockk<DynamoDbEnhancedClient>()
        val table = mockk<DynamoDbTable<VocabularyCheckResultItem>>(relaxed = true)
        val gsi = mockk<DynamoDbIndex<VocabularyCheckResultItem>>()
        val repo = VocabularyCheckResultRepository(enhanced, "vocabulary-check-results")
        val schema: TableSchema<VocabularyCheckResultItem> =
            TableSchema.fromBean(VocabularyCheckResultItem::class.java)

        fun item(
            checkResultId: String = "r1",
            vocabularyId: String = "v1",
            userId: String = "u1",
        ) = VocabularyCheckResultItem().apply {
            this.vocabularyCheckResultId = checkResultId
            this.vocabularyId = vocabularyId
            this.userId = userId
            this.vocabularyName = "Kotlin"
            this.severity = "HIGH"
            this.status = "UNCHECKED"
            this.report = "## report"
            this.vocabularyUpdatedAt = "2025-05-01T00:00:00Z"
            this.checkedAt = "2025-06-01T00:00:00Z"
        }

        "findByUser: userId でクエリして一覧を返す" {
            val page = mockk<Page<VocabularyCheckResultItem>>()
            val iterable = mockk<PageIterable<VocabularyCheckResultItem>>()
            every { enhanced.table("vocabulary-check-results", schema) } returns table
            every { page.items() } returns listOf(item("r1", "v1"), item("r2", "v2"))
            every { iterable.iterator() } returns mutableListOf(page).iterator()
            every { table.query(any<QueryEnhancedRequest>()) } returns iterable

            val result = repo.findByUser("u1")

            result.shouldHaveSize(2)
            verify(exactly = 1) { table.query(any<QueryEnhancedRequest>()) }
        }

        "getByCheckResultId: GSI で1件返す" {
            val page = mockk<Page<VocabularyCheckResultItem>>()
            every { enhanced.table("vocabulary-check-results", schema) } returns table
            every { table.index("gsi_vocabulary_check_result_id") } returns gsi
            every { page.items() } returns listOf(item(checkResultId = "r1"))
            every { gsi.query(any<QueryEnhancedRequest>()) } returns SdkIterable { mutableListOf(page).iterator() }

            repo.getByCheckResultId("r1")!!.vocabularyCheckResultId shouldBe "r1"
            verify(exactly = 1) { gsi.query(any<QueryEnhancedRequest>()) }
        }

        "put/delete: 委譲" {
            every { enhanced.table("vocabulary-check-results", schema) } returns table
            val it = item()

            repo.put(it)
            verify(exactly = 1) { table.putItem(it) }

            val delKey = Key.builder().partitionValue("u1").sortValue("v1").build()
            every { table.deleteItem(delKey) } returns it
            repo.delete("u1", "v1")
            verify(exactly = 1) { table.deleteItem(delKey) }
        }
    })
