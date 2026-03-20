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

class VocabularyRepositoryTest :
    StringSpec({

        val enhanced = mockk<DynamoDbEnhancedClient>()
        val table = mockk<DynamoDbTable<VocabularyItem>>(relaxed = true)
        val gsi = mockk<DynamoDbIndex<VocabularyItem>>()
        val repo = VocabularyRepository(enhanced, "vocabularies")
        val schema: TableSchema<VocabularyItem> = TableSchema.fromBean(VocabularyItem::class.java)

        fun item(id: String = "v1", name: String = "Kotlin") =
            VocabularyItem().apply {
                vocabularyId = id
                userId = "u1"
                this.name = name
                tags = emptyList()
                createdDateTime = "2025-01-01T00:00:00Z"
                updatedDateTime = "2025-01-01T00:00:00Z"
            }

        "getByVocabularyId: GSI で1件返す" {
            val page = mockk<Page<VocabularyItem>>()
            every { enhanced.table("vocabularies", schema) } returns table
            every { table.index("gsi_vocabulary_id") } returns gsi
            every { page.items() } returns listOf(item("vid-1"))
            every { gsi.query(any<QueryEnhancedRequest>()) } returns SdkIterable { mutableListOf(page).iterator() }

            repo.getByVocabularyId("vid-1")!!.vocabularyId shouldBe "vid-1"
            verify(exactly = 1) { gsi.query(any<QueryEnhancedRequest>()) }
        }

        "findByUser: 条件なし/vocabularyName フィルタで一覧を返す" {
            val page1 = mockk<Page<VocabularyItem>>()
            val page2 = mockk<Page<VocabularyItem>>()
            val iterable1 = mockk<PageIterable<VocabularyItem>>()
            val iterable2 = mockk<PageIterable<VocabularyItem>>()
            every { enhanced.table("vocabularies", schema) } returns table
            every { page1.items() } returns listOf(item("v1"), item("v2", name = "Java"))
            every { page2.items() } returns listOf(item("v9"))
            every { iterable1.iterator() } returns mutableListOf(page1).iterator()
            every { iterable2.iterator() } returns mutableListOf(page2).iterator()
            every { table.query(any<QueryEnhancedRequest>()) } returnsMany listOf(iterable1, iterable2)

            val all = repo.findByUser("u1")
            val filtered = repo.findByUser("u9", vocabularyName = "Kotlin")

            all.shouldHaveSize(2)
            filtered.shouldHaveSize(1)
            filtered[0].vocabularyId shouldBe "v9"
        }

        "put/delete: 委譲" {
            every { enhanced.table("vocabularies", schema) } returns table
            val it = item("vX")

            repo.put(it)
            verify(exactly = 1) { table.putItem(it) }

            val delKey = Key.builder().partitionValue("u1").sortValue("vX").build()
            every { table.deleteItem(delKey) } returns it
            repo.delete("u1", "vX")
            verify(exactly = 1) { table.deleteItem(delKey) }
        }
    })
