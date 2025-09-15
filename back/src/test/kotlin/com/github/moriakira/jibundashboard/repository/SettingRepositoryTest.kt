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

class SettingRepositoryTest :
    StringSpec({

        val enhanced = mockk<DynamoDbEnhancedClient>()
        val table = mockk<DynamoDbTable<SettingItem>>(relaxed = true)
        val repo = SettingRepository(enhanced, "settings")
        val schema: TableSchema<SettingItem> = TableSchema.fromBean(SettingItem::class.java)

        "get: 1件取得" {
            val item = SettingItem().apply { userId = "u1" }
            val key = Key.builder().partitionValue("u1").build()
            every { enhanced.table("settings", schema) } returns table
            every { table.getItem(key) } returns item

            val res = repo.get("u1")

            res!!.userId shouldBe "u1"
            verify(exactly = 1) { table.getItem(key) }
        }

        "put: 保存委譲" {
            every { enhanced.table("settings", schema) } returns table
            val item = SettingItem().apply { userId = "u2" }

            repo.put(item)

            verify(exactly = 1) { table.putItem(item) }
        }
    })
