package com.github.moriakira.jibundashboard.repository

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import software.amazon.awssdk.core.pagination.sync.SdkIterable
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable
import software.amazon.awssdk.enhanced.dynamodb.TableSchema
import software.amazon.awssdk.enhanced.dynamodb.model.Page
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest
import java.util.function.Consumer

class ResourceRepositoryTest :
    StringSpec({

        val enhanced = mockk<DynamoDbEnhancedClient>()
        val table = mockk<DynamoDbTable<I18nItem>>()
        val repo = ResourceRepository(enhanced, "resources-i18n")

        "findI18nByLocaleCode: クエリ結果を返す" {
            val page = mockk<Page<I18nItem>>()
            val pages = mockk<PageIterable<I18nItem>>()
            val schema: TableSchema<I18nItem> = TableSchema.fromBean(I18nItem::class.java)
            val item1 = I18nItem().apply {
                localeCode = "ja"
                messageKey = "hello"
                message = "こんにちは"
            }
            val item2 = I18nItem().apply {
                localeCode = "ja"
                messageKey = "bye"
                message = "さようなら"
            }

            every { enhanced.table("resources-i18n", schema) } returns table
            every { page.items() } returns listOf(item1, item2)
            // pages.items() が items の Iterable を返すように設定
            every { pages.items() } returns SdkIterable { mutableListOf(item1, item2).iterator() }
            every { table.query(any<Consumer<QueryEnhancedRequest.Builder>>()) } returns pages

            val result = repo.findI18nByLocaleCode("ja")

            result.shouldContainExactly(item1, item2)
            verify(exactly = 1) { table.query(any<Consumer<QueryEnhancedRequest.Builder>>()) }
        }
    })
