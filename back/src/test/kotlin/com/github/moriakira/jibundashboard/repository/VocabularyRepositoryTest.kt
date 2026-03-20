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

        fun tag(id: String = "tag1", tag: String = "kotlin") =
            VocabularyTagEmbed().apply {
                vocabularyTagId = id
                vocabularyTag = tag
            }

        fun item(
            id: String = "v1",
            user: String = "u1",
            name: String = "Kotlin",
            tags: List<VocabularyTagEmbed>? = listOf(tag()),
        ) = VocabularyItem().apply {
            vocabularyId = id
            userId = user
            this.name = name
            description = "A programming language"
            this.tags = tags
            createdDateTime = "2025-01-01T00:00:00Z"
            updatedDateTime = "2025-01-01T00:00:00Z"
        }

        "getByVocabularyId: GSI で1件返す" {
            val page = mockk<Page<VocabularyItem>>()
            every { enhanced.table("vocabularies", schema) } returns table
            every { table.index("gsi_vocabulary_id") } returns gsi
            every { page.items() } returns listOf(item("vid-1"))
            every {
                gsi.query(any<QueryEnhancedRequest>())
            } returns SdkIterable { mutableListOf(page).iterator() }

            val res = repo.getByVocabularyId("vid-1")

            res!!.vocabularyId shouldBe "vid-1"
            verify(exactly = 1) { gsi.query(any<QueryEnhancedRequest>()) }
        }

        "getByVocabularyId: 存在しない場合は null" {
            val page = mockk<Page<VocabularyItem>>()
            every { enhanced.table("vocabularies", schema) } returns table
            every { table.index("gsi_vocabulary_id") } returns gsi
            every { page.items() } returns emptyList()
            every {
                gsi.query(any<QueryEnhancedRequest>())
            } returns SdkIterable { mutableListOf(page).iterator() }

            val res = repo.getByVocabularyId("nope")

            res shouldBe null
        }

        "findByUser: 条件なしで一覧を返す" {
            val page = mockk<Page<VocabularyItem>>()
            val iterable = mockk<PageIterable<VocabularyItem>>()
            every { enhanced.table("vocabularies", schema) } returns table
            every { page.items() } returns listOf(item("v1"), item("v2", name = "Java"))
            every { iterable.iterator() } returns mutableListOf(page).iterator()
            every { table.query(any<QueryEnhancedRequest>()) } returns iterable

            val res = repo.findByUser("u1")

            res.shouldHaveSize(2)
            res[0].vocabularyId shouldBe "v1"
            res[1].vocabularyId shouldBe "v2"
            verify(exactly = 1) { table.query(any<QueryEnhancedRequest>()) }
        }

        "findByUser: vocabularyName 指定でフィルタあり一覧を返す" {
            val page = mockk<Page<VocabularyItem>>()
            val iterable = mockk<PageIterable<VocabularyItem>>()
            every { enhanced.table("vocabularies", schema) } returns table
            every { page.items() } returns listOf(item("v9", name = "Kotlin"))
            every { iterable.iterator() } returns mutableListOf(page).iterator()
            every { table.query(any<QueryEnhancedRequest>()) } returns iterable

            val res = repo.findByUser("u9", vocabularyName = "Kotlin")

            res.shouldHaveSize(1)
            res[0].vocabularyId shouldBe "v9"
            verify(exactly = 1) { table.query(any<QueryEnhancedRequest>()) }
        }

        "put/delete: 委譲" {
            every { enhanced.table("vocabularies", schema) } returns table
            val it = item("vX", "uX")

            repo.put(it)
            verify(exactly = 1) { table.putItem(it) }

            val delKey = Key.builder().partitionValue("uX").sortValue("vX").build()
            every { table.deleteItem(delKey) } returns it
            repo.delete("uX", "vX")
            verify(exactly = 1) { table.deleteItem(delKey) }
        }
    })
