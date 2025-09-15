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

class SalaryRepositoryTest :
    StringSpec({

        val enhanced = mockk<DynamoDbEnhancedClient>()
        val table = mockk<DynamoDbTable<SalaryItem>>(relaxed = true)
        val gsi = mockk<DynamoDbIndex<SalaryItem>>()
        val repo = SalaryRepository(enhanced, "salaries")
        val schema: TableSchema<SalaryItem> = TableSchema.fromBean(SalaryItem::class.java)

        fun item(id: String = "s1", user: String = "u1", date: String = "2025-08-15") = SalaryItem().apply {
            salaryId = id
            userId = user
            targetDate = date
        }

        "get: パーティション+ソートキーで取得" {
            val it = item("sid-1", "u1", "2025-08-15")
            val key = Key.builder().partitionValue("u1").sortValue("2025-08-15").build()
            every { enhanced.table("salaries", schema) } returns table
            every { table.getItem(key) } returns it

            val res = repo.get("u1", "2025-08-15")

            res!!.salaryId shouldBe "sid-1"
            verify(exactly = 1) { table.getItem(key) }
        }

        "getBySalaryId: GSI で1件取得" {
            val it = item("sid-2")
            val page = mockk<Page<SalaryItem>>()
            every { enhanced.table("salaries", schema) } returns table
            every { table.index("gsi_salary_id") } returns gsi
            every { page.items() } returns listOf(it)
            every { gsi.query(any<QueryEnhancedRequest>()) } returns SdkIterable { mutableListOf(page).iterator() }

            val res = repo.getBySalaryId("sid-2")

            res!!.salaryId shouldBe "sid-2"
            verify(exactly = 1) { gsi.query(any<QueryEnhancedRequest>()) }
        }

        "findByUser: ユーザで一覧取得" {
            val p = mockk<Page<SalaryItem>>()
            val pages = mockk<PageIterable<SalaryItem>>()
            every { enhanced.table("salaries", schema) } returns table
            every { p.items() } returns listOf(item("s1"), item("s2"))
            every { pages.iterator() } returns mutableListOf(p).iterator()
            every { table.query(any<QueryEnhancedRequest>()) } returns pages

            val res = repo.findByUser("u1")

            res.shouldHaveSize(2)
            res[0].salaryId shouldBe "s1"
            res[1].salaryId shouldBe "s2"
        }

        "findByUserAndDate: 日付一致で取得" {
            val p = mockk<Page<SalaryItem>>()
            val pages = mockk<PageIterable<SalaryItem>>()
            every { enhanced.table("salaries", schema) } returns table
            every { p.items() } returns listOf(item("s3", date = "2025-01-01"))
            every { pages.iterator() } returns mutableListOf(p).iterator()
            every { table.query(any<QueryEnhancedRequest>()) } returns pages

            val res = repo.findByUserAndDate("u1", "2025-01-01")

            res.shouldHaveSize(1)
            res[0].salaryId shouldBe "s3"
        }

        "findByUserAndDateRange: 期間で取得" {
            val p = mockk<Page<SalaryItem>>()
            val pages = mockk<PageIterable<SalaryItem>>()
            every { enhanced.table("salaries", schema) } returns table
            every { p.items() } returns listOf(item("s4"))
            every { pages.iterator() } returns mutableListOf(p).iterator()
            every { table.query(any<QueryEnhancedRequest>()) } returns pages

            val res = repo.findByUserAndDateRange("u1", "2025-01-01", "2025-12-31")

            res.shouldHaveSize(1)
            res[0].salaryId shouldBe "s4"
        }

        "put/delete: 委譲" {
            every { enhanced.table("salaries", schema) } returns table
            val it = item("sid-x", "uX", "2025-02-02")

            repo.put(it)
            verify(exactly = 1) { table.putItem(it) }

            val delKey = Key.builder().partitionValue("uX").sortValue("2025-02-02").build()
            every { table.deleteItem(delKey) } returns it
            repo.delete("uX", "2025-02-02")
            verify(exactly = 1) { table.deleteItem(delKey) }
        }
    })
