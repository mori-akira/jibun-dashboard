package com.github.moriakira.jibundashboard.repository

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Repository
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient
import software.amazon.awssdk.enhanced.dynamodb.Key
import software.amazon.awssdk.enhanced.dynamodb.TableSchema
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional

@Repository
class ResourceRepository(
    private val enhanced: DynamoDbEnhancedClient,
    @param:Value("\${app.dynamodb.tables.resources-i18n}") private val resourcesI18nTableName: String,
) {
    private val schema = TableSchema.fromBean(I18nItem::class.java)

    fun findI18nByLocaleCode(localeCode: String): List<I18nItem> {
        val table = enhanced.table(resourcesI18nTableName, schema)
        val pages = table.query { req ->
            req.queryConditional(
                QueryConditional.keyEqualTo(Key.builder().partitionValue(localeCode).build()),
            )
        }
        return pages.items().toList()
    }
}

@DynamoDbBean
class I18nItem {
    @get:DynamoDbPartitionKey
    var localeCode: String? = null

    @get:DynamoDbSortKey
    var messageKey: String? = null

    var message: String? = null
}
