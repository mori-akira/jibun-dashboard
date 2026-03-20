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

class VocabularyTagRepositoryTest :
    StringSpec({

        val enhanced = mockk<DynamoDbEnhancedClient>()
        val table = mockk<DynamoDbTable<VocabularyTagItem>>(relaxed = true)
        val gsi = mockk<DynamoDbIndex<VocabularyTagItem>>()
        val repo = VocabularyTagRepository(enhanced, "vocabulary-tags")
        val schema: TableSchema<VocabularyTagItem> = TableSchema.fromBean(VocabularyTagItem::class.java)

        fun item(id: String = "tag1", user: String = "u1", tag: String = "kotlin") =
            VocabularyTagItem().apply {
                vocabularyTagId = id
                userId = user
                vocabularyTag = tag
            }

        "getByVocabularyTagId: GSI で1件返す" {
            val page = mockk<Page<VocabularyTagItem>>()
            every { enhanced.table("vocabulary-tags", schema) } returns table
            every { table.index("gsi_vocabulary_tag_id") } returns gsi
            every { page.items() } returns listOf(item("tag-1"))
            every {
                gsi.query(any<QueryEnhancedRequest>())
            } returns SdkIterable { mutableListOf(page).iterator() }

            val res = repo.getByVocabularyTagId("tag-1")

            res!!.vocabularyTagId shouldBe "tag-1"
            verify(exactly = 1) { gsi.query(any<QueryEnhancedRequest>()) }
        }

        "getByVocabularyTagId: 存在しない場合は null" {
            val page = mockk<Page<VocabularyTagItem>>()
            every { enhanced.table("vocabulary-tags", schema) } returns table
            every { table.index("gsi_vocabulary_tag_id") } returns gsi
            every { page.items() } returns emptyList()
            every {
                gsi.query(any<QueryEnhancedRequest>())
            } returns SdkIterable { mutableListOf(page).iterator() }

            val res = repo.getByVocabularyTagId("nope")

            res shouldBe null
        }

        "findByUser: 条件なしで一覧を返す" {
            val page = mockk<Page<VocabularyTagItem>>()
            val iterable = mockk<PageIterable<VocabularyTagItem>>()
            every { enhanced.table("vocabulary-tags", schema) } returns table
            every { page.items() } returns listOf(item("tag1"), item("tag2", tag = "java"))
            every { iterable.iterator() } returns mutableListOf(page).iterator()
            every { table.query(any<QueryEnhancedRequest>()) } returns iterable

            val res = repo.findByUser("u1")

            res.shouldHaveSize(2)
            res[0].vocabularyTagId shouldBe "tag1"
            res[1].vocabularyTagId shouldBe "tag2"
            verify(exactly = 1) { table.query(any<QueryEnhancedRequest>()) }
        }

        "findByUser: vocabularyTag 指定でフィルタあり一覧を返す" {
            val page = mockk<Page<VocabularyTagItem>>()
            val iterable = mockk<PageIterable<VocabularyTagItem>>()
            every { enhanced.table("vocabulary-tags", schema) } returns table
            every { page.items() } returns listOf(item("tag9"))
            every { iterable.iterator() } returns mutableListOf(page).iterator()
            every { table.query(any<QueryEnhancedRequest>()) } returns iterable

            val res = repo.findByUser("u9", vocabularyTag = "kotlin")

            res.shouldHaveSize(1)
            res[0].vocabularyTagId shouldBe "tag9"
            verify(exactly = 1) { table.query(any<QueryEnhancedRequest>()) }
        }

        "put/delete: 委譲" {
            every { enhanced.table("vocabulary-tags", schema) } returns table
            val it = item("tagX", "uX")

            repo.put(it)
            verify(exactly = 1) { table.putItem(it) }

            val delKey = Key.builder().partitionValue("uX").sortValue("tagX").build()
            every { table.deleteItem(delKey) } returns it
            repo.delete("uX", "tagX")
            verify(exactly = 1) { table.deleteItem(delKey) }
        }
    })
