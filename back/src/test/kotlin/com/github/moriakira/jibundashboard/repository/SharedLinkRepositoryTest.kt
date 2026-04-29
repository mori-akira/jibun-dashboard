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

class SharedLinkRepositoryTest :
    StringSpec({

        val enhanced = mockk<DynamoDbEnhancedClient>()
        val table = mockk<DynamoDbTable<SharedLinkItem>>(relaxed = true)
        val gsi = mockk<DynamoDbIndex<SharedLinkItem>>()
        val repo = SharedLinkRepository(enhanced, "shared-links")
        val schema: TableSchema<SharedLinkItem> = TableSchema.fromBean(SharedLinkItem::class.java)

        fun item(token: String = "tok1", userId: String = "u1") =
            SharedLinkItem().apply {
                this.token = token
                this.userId = userId
                this.dataTypes = listOf("salary")
                this.expiresAt = "2099-12-31"
            }

        "getByToken: トークンで1件返す" {
            every { enhanced.table("shared-links", schema) } returns table
            every { table.getItem(any<Key>()) } returns item("tok1")

            repo.getByToken("tok1")!!.token shouldBe "tok1"
            verify(exactly = 1) { table.getItem(any<Key>()) }
        }

        "findByUserId: GSI でユーザーのリンク一覧を返す" {
            val page = mockk<Page<SharedLinkItem>>()
            every { enhanced.table("shared-links", schema) } returns table
            every { table.index("gsi_user_id") } returns gsi
            every { page.items() } returns listOf(item("tok1"), item("tok2"))
            every { gsi.query(any<QueryEnhancedRequest>()) } returns SdkIterable { mutableListOf(page).iterator() }

            repo.findByUserId("u1").shouldHaveSize(2)
        }

        "put/delete: 委譲" {
            every { enhanced.table("shared-links", schema) } returns table
            val it = item("tokX")

            repo.put(it)
            verify(exactly = 1) { table.putItem(it) }

            repo.delete("tokX")
            verify(exactly = 1) { table.deleteItem(any<Key>()) }
        }
    })
