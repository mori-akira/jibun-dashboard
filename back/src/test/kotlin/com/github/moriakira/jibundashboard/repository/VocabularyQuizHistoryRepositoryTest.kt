package com.github.moriakira.jibundashboard.repository

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable
import software.amazon.awssdk.enhanced.dynamodb.Key
import software.amazon.awssdk.enhanced.dynamodb.TableSchema
import software.amazon.awssdk.enhanced.dynamodb.model.Page
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest

class VocabularyQuizHistoryRepositoryTest :
    StringSpec({

        val enhanced = mockk<DynamoDbEnhancedClient>()
        val table = mockk<DynamoDbTable<VocabularyQuizHistoryItem>>(relaxed = true)
        val repo = VocabularyQuizHistoryRepository(enhanced, "vocabulary-quiz-histories")
        val schema: TableSchema<VocabularyQuizHistoryItem> = TableSchema.fromBean(VocabularyQuizHistoryItem::class.java)

        fun item(id: String = "qh1") =
            VocabularyQuizHistoryItem().apply {
                quizHistoryId = id
                userId = "u1"
                answeredAt = "2025-01-01T00:00:00Z"
                tagIds = listOf("tag1")
                questionCount = 2
                direction = "FRONT_TO_BACK"
                correctCount = 1
                incorrectCount = 1
                answers = listOf(
                    VocabularyQuizHistoryAnswerItem().apply {
                        vocabularyId = "v1"
                        result = "CORRECT"
                    },
                )
            }

        "getByUserAndQuizHistoryId: PK+SK で1件返す" {
            every { enhanced.table("vocabulary-quiz-histories", schema) } returns table
            val key = Key.builder().partitionValue("u1").sortValue("qh1").build()
            every { table.getItem(key) } returns item("qh1")

            repo.getByUserAndQuizHistoryId("u1", "qh1")!!.quizHistoryId shouldBe "qh1"
        }

        "getByUserAndQuizHistoryId: 存在しなければ null を返す" {
            every { enhanced.table("vocabulary-quiz-histories", schema) } returns table
            val key = Key.builder().partitionValue("u1").sortValue("missing").build()
            every { table.getItem(key) } returns null

            repo.getByUserAndQuizHistoryId("u1", "missing") shouldBe null
        }

        "findByUser: userId で一覧を返す" {
            val page = mockk<Page<VocabularyQuizHistoryItem>>()
            val iterable = mockk<PageIterable<VocabularyQuizHistoryItem>>()
            every { enhanced.table("vocabulary-quiz-histories", schema) } returns table
            every { page.items() } returns listOf(item("qh1"), item("qh2"))
            every { iterable.iterator() } returns mutableListOf(page).iterator()
            every { table.query(any<QueryEnhancedRequest>()) } returns iterable

            val res = repo.findByUser("u1")

            res.shouldHaveSize(2)
            res[0].quizHistoryId shouldBe "qh1"
            verify(exactly = 1) { table.query(any<QueryEnhancedRequest>()) }
        }

        "put/delete: 委譲" {
            every { enhanced.table("vocabulary-quiz-histories", schema) } returns table
            val it = item("qhX")

            repo.put(it)
            verify(exactly = 1) { table.putItem(it) }

            val delKey = Key.builder().partitionValue("u1").sortValue("qhX").build()
            every { table.deleteItem(delKey) } returns it
            repo.delete("u1", "qhX")
            verify(exactly = 1) { table.deleteItem(delKey) }
        }
    })
