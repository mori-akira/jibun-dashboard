package com.github.moriakira.jibundashboard.repository

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable
import software.amazon.awssdk.enhanced.dynamodb.Key
import software.amazon.awssdk.enhanced.dynamodb.TableSchema

class UserRepositoryTest :
    StringSpec({

        val enhanced = mockk<DynamoDbEnhancedClient>()
        val table = mockk<DynamoDbTable<UserItem>>(relaxed = true)
        val repo = UserRepository(enhanced, "users")
        val schema: TableSchema<UserItem> = TableSchema.fromBean(UserItem::class.java)

        "get: Keyで1件取得する" {
            val item = UserItem().apply {
                userId = "u1"
                userName = "Alice"
                emailAddress = "alice@example.com"
            }
            val key = Key.builder().partitionValue("u1").build()
            every { enhanced.table("users", schema) } returns table
            every { table.getItem(key) } returns item

            val result = repo.get("u1")

            result!!.userId shouldBe "u1"
            result.userName shouldBe "Alice"
            result.emailAddress shouldBe "alice@example.com"
            verify(exactly = 1) { table.getItem(key) }
        }

        "put: item を保存する" {
            every { enhanced.table("users", schema) } returns table

            val item = UserItem().apply { userId = "u2" }
            repo.put(item)

            verify(exactly = 1) { table.putItem(item) }
        }
    })
