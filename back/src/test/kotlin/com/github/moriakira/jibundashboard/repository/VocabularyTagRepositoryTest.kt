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

class VocabularyTagRepositoryTest :
    StringSpec({

        val enhanced = mockk<DynamoDbEnhancedClient>()
        val table = mockk<DynamoDbTable<VocabularyTagItem>>(relaxed = true)
        val gsi = mockk<DynamoDbIndex<VocabularyTagItem>>()
        val repo = VocabularyTagRepository(enhanced, "vocabulary-tags")
        val schema: TableSchema<VocabularyTagItem> = TableSchema.fromBean(VocabularyTagItem::class.java)

        fun item(id: String = "tag1", tag: String = "kotlin") =
            VocabularyTagItem().apply {
                vocabularyTagId = id
                userId = "u1"
                vocabularyTag = tag
                order = 1
            }

        "getByVocabularyTagId: GSI で1件返す" {
            val page = mockk<Page<VocabularyTagItem>>()
            every { enhanced.table("vocabulary-tags", schema) } returns table
            every { table.index("gsi_vocabulary_tag_id") } returns gsi
            every { page.items() } returns listOf(item("tag-1"))
            every { gsi.query(any<QueryEnhancedRequest>()) } returns SdkIterable { mutableListOf(page).iterator() }

            repo.getByVocabularyTagId("tag-1")!!.vocabularyTagId shouldBe "tag-1"
            verify(exactly = 1) { gsi.query(any<QueryEnhancedRequest>()) }
        }

        "findByUser: 条件なし/vocabularyTag フィルタで一覧を返す" {
            val page1 = mockk<Page<VocabularyTagItem>>()
            val page2 = mockk<Page<VocabularyTagItem>>()
            val iterable1 = mockk<SdkIterable<Page<VocabularyTagItem>>>()
            val iterable2 = mockk<SdkIterable<Page<VocabularyTagItem>>>()
            every { enhanced.table("vocabulary-tags", schema) } returns table
            every { table.index("gsi_user_order") } returns gsi
            every { page1.items() } returns listOf(item("tag1"), item("tag2", tag = "java"))
            every { page2.items() } returns listOf(item("tag9"))
            every { iterable1.iterator() } returns mutableListOf(page1).iterator()
            every { iterable2.iterator() } returns mutableListOf(page2).iterator()
            every { gsi.query(any<QueryEnhancedRequest>()) } returnsMany listOf(iterable1, iterable2)

            val all = repo.findByUser("u1")
            val filtered = repo.findByUser("u9", vocabularyTag = "kotlin")

            all.shouldHaveSize(2)
            filtered.shouldHaveSize(1)
            filtered[0].vocabularyTagId shouldBe "tag9"
        }

        "put/delete: 委譲" {
            every { enhanced.table("vocabulary-tags", schema) } returns table
            val it = item("tagX")

            repo.put(it)
            verify(exactly = 1) { table.putItem(it) }

            val delKey = Key.builder().partitionValue("u1").sortValue("tagX").build()
            every { table.deleteItem(delKey) } returns it
            repo.delete("u1", "tagX")
            verify(exactly = 1) { table.deleteItem(delKey) }
        }
    })
