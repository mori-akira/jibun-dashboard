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

class CardbookCardRepositoryTest :
    StringSpec({

        val enhanced = mockk<DynamoDbEnhancedClient>()
        val table = mockk<DynamoDbTable<CardbookCardItem>>(relaxed = true)
        val gsi = mockk<DynamoDbIndex<CardbookCardItem>>()
        val repo = CardbookCardRepository(enhanced, "cardbook-cards")
        val schema: TableSchema<CardbookCardItem> = TableSchema.fromBean(CardbookCardItem::class.java)

        fun item(id: String = "card1", cardbookId: String = "cb1") =
            CardbookCardItem().apply {
                cardId = id
                this.cardbookId = cardbookId
                userId = "u1"
                front = "Front text"
                back = "Back text"
                createdDateTime = "2025-01-01T00:00:00Z"
            }

        "getByUserAndCardId: PK+SK で1件返す" {
            every { enhanced.table("cardbook-cards", schema) } returns table
            val key = Key.builder().partitionValue("u1").sortValue("card1").build()
            every { table.getItem(key) } returns item("card1")

            repo.getByUserAndCardId("u1", "card1")!!.cardId shouldBe "card1"
        }

        "getByUserAndCardId: 存在しなければ null を返す" {
            every { enhanced.table("cardbook-cards", schema) } returns table
            val key = Key.builder().partitionValue("u1").sortValue("missing").build()
            every { table.getItem(key) } returns null

            repo.getByUserAndCardId("u1", "missing") shouldBe null
        }

        "findByCardbookId: GSI でカード一覧を返す" {
            val page = mockk<Page<CardbookCardItem>>()
            every { enhanced.table("cardbook-cards", schema) } returns table
            every { table.index("gsi_cardbook_id") } returns gsi
            every { page.items() } returns listOf(item("card1"), item("card2"))
            every { gsi.query(any<QueryEnhancedRequest>()) } returns SdkIterable { mutableListOf(page).iterator() }

            val res = repo.findByCardbookId("cb1")

            res.shouldHaveSize(2)
            res[0].cardId shouldBe "card1"
            verify(exactly = 1) { gsi.query(any<QueryEnhancedRequest>()) }
        }

        "put/delete: 委譲" {
            every { enhanced.table("cardbook-cards", schema) } returns table
            val it = item("cardX")

            repo.put(it)
            verify(exactly = 1) { table.putItem(it) }

            val delKey = Key.builder().partitionValue("u1").sortValue("cardX").build()
            every { table.deleteItem(delKey) } returns it
            repo.delete("u1", "cardX")
            verify(exactly = 1) { table.deleteItem(delKey) }
        }
    })
