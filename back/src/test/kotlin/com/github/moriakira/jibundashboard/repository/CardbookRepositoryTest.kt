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

class CardbookRepositoryTest :
    StringSpec({

        val enhanced = mockk<DynamoDbEnhancedClient>()
        val table = mockk<DynamoDbTable<CardbookItem>>(relaxed = true)
        val repo = CardbookRepository(enhanced, "cardbooks")
        val schema: TableSchema<CardbookItem> = TableSchema.fromBean(CardbookItem::class.java)

        fun item(id: String = "cb1") =
            CardbookItem().apply {
                cardbookId = id
                userId = "u1"
                name = "My Cardbook"
                createdDateTime = "2025-01-01T00:00:00Z"
                updatedDateTime = "2025-01-01T00:00:00Z"
            }

        "getByUserAndCardbookId: PK+SK で1件返す" {
            every { enhanced.table("cardbooks", schema) } returns table
            val key = Key.builder().partitionValue("u1").sortValue("cb1").build()
            every { table.getItem(key) } returns item("cb1")

            repo.getByUserAndCardbookId("u1", "cb1")!!.cardbookId shouldBe "cb1"
        }

        "getByUserAndCardbookId: 存在しなければ null を返す" {
            every { enhanced.table("cardbooks", schema) } returns table
            val key = Key.builder().partitionValue("u1").sortValue("missing").build()
            every { table.getItem(key) } returns null

            repo.getByUserAndCardbookId("u1", "missing") shouldBe null
        }

        "findByUser: userId で一覧を返す" {
            val page = mockk<Page<CardbookItem>>()
            val iterable = mockk<PageIterable<CardbookItem>>()
            every { enhanced.table("cardbooks", schema) } returns table
            every { page.items() } returns listOf(item("cb1"), item("cb2"))
            every { iterable.iterator() } returns mutableListOf(page).iterator()
            every { table.query(any<QueryEnhancedRequest>()) } returns iterable

            val res = repo.findByUser("u1")

            res.shouldHaveSize(2)
            res[0].cardbookId shouldBe "cb1"
            verify(exactly = 1) { table.query(any<QueryEnhancedRequest>()) }
        }

        "put/delete: 委譲" {
            every { enhanced.table("cardbooks", schema) } returns table
            val it = item("cbX")

            repo.put(it)
            verify(exactly = 1) { table.putItem(it) }

            val delKey = Key.builder().partitionValue("u1").sortValue("cbX").build()
            every { table.deleteItem(delKey) } returns it
            repo.delete("u1", "cbX")
            verify(exactly = 1) { table.deleteItem(delKey) }
        }
    })
