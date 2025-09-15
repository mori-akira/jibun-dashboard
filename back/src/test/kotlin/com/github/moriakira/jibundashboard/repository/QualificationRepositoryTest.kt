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

class QualificationRepositoryTest :
    StringSpec({

        val enhanced = mockk<DynamoDbEnhancedClient>()
        val table = mockk<DynamoDbTable<QualificationItem>>(relaxed = true)
        val gsi = mockk<DynamoDbIndex<QualificationItem>>()
        val lsi = mockk<DynamoDbIndex<QualificationItem>>()
        val repo = QualificationRepository(enhanced, "qualifications")
        val schema: TableSchema<QualificationItem> = TableSchema.fromBean(QualificationItem::class.java)

        fun item(id: String = "q1", user: String = "u1", order: Int = 1) = QualificationItem().apply {
            qualificationId = id
            userId = user
            this.order = order
            qualificationName = "AWS"
            status = "acquired"
            rank = "A"
            organization = "AWS"
            officialUrl = "https://ex"
        }

        "getByQualificationId: GSI で1件返す" {
            val page = mockk<Page<QualificationItem>>()
            every { enhanced.table("qualifications", schema) } returns table
            every { table.index("gsi_qualification_id") } returns gsi
            every { page.items() } returns listOf(item("qid-1"))
            every {
                gsi.query(any<QueryEnhancedRequest>())
            } returns SdkIterable { mutableListOf(page).iterator() }

            val res = repo.getByQualificationId("qid-1")

            res!!.qualificationId shouldBe "qid-1"
            verify(exactly = 1) { gsi.query(any<QueryEnhancedRequest>()) }
        }

        "query: 条件なしでLSI order を使って一覧を返す" {
            val page = mockk<Page<QualificationItem>>()
            every { enhanced.table("qualifications", schema) } returns table
            every { table.index("lsi_order") } returns lsi
            every { page.items() } returns listOf(item("q1"), item("q2", order = 2))
            every { lsi.query(any<QueryEnhancedRequest>()) } returns SdkIterable {
                mutableListOf(
                    page,
                ).iterator()
            }

            val res = repo.query(
                userId = "u1",
            )

            res.shouldHaveSize(2)
            res[0].qualificationId shouldBe "q1"
            res[1].qualificationId shouldBe "q2"
            verify(exactly = 1) { lsi.query(any<QueryEnhancedRequest>()) }
        }

        "query: 各種フィルタ指定でも一覧を返す" {
            val page = mockk<Page<QualificationItem>>()
            every { enhanced.table("qualifications", schema) } returns table
            every { table.index("lsi_order") } returns lsi
            every { page.items() } returns listOf(item("q9"))
            every { lsi.query(any<QueryEnhancedRequest>()) } returns SdkIterable {
                mutableListOf(
                    page,
                ).iterator()
            }

            val res = repo.query(
                userId = "u9",
                qualificationName = "AWS",
                statuses = listOf("acquired"),
                ranks = listOf("A"),
                acquiredDateFrom = "2025-01-01",
                organization = "AWS",
                acquiredDateTo = "2025-12-31",
                expirationDateFrom = "2025-02-01",
                expirationDateTo = "2025-11-01",
            )

            res.shouldHaveSize(1)
            res[0].qualificationId shouldBe "q9"
            verify(exactly = 1) { lsi.query(any<QueryEnhancedRequest>()) }
        }

        "put/delete: 委譲" {
            every { enhanced.table("qualifications", schema) } returns table
            val it = item("qX", "uX", 10)

            repo.put(it)
            verify(exactly = 1) { table.putItem(it) }

            val delKey = Key.builder().partitionValue("uX").sortValue("qX").build()
            // deleteItem は Key を構築するため、任意の Key を受け取ることを許容
            every { table.deleteItem(delKey) } returns it
            repo.delete("uX", "qX")
            verify(exactly = 1) { table.deleteItem(delKey) }
        }
    })
