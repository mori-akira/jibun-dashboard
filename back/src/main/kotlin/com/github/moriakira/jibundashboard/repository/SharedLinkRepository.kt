package com.github.moriakira.jibundashboard.repository

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Repository
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable
import software.amazon.awssdk.enhanced.dynamodb.Key
import software.amazon.awssdk.enhanced.dynamodb.TableSchema
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional.keyEqualTo
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest

@Repository
class SharedLinkRepository(
    private val enhanced: DynamoDbEnhancedClient,
    @param:Value("\${app.dynamodb.tables.shared-links}") private val tableName: String,
) {
    private val schema: TableSchema<SharedLinkItem> = TableSchema.fromBean(SharedLinkItem::class.java)
    private fun table(): DynamoDbTable<SharedLinkItem> = enhanced.table(tableName, schema)

    fun getByToken(token: String): SharedLinkItem? =
        table().getItem(Key.builder().partitionValue(token).build())

    fun findByUserId(userId: String): List<SharedLinkItem> {
        val req = QueryEnhancedRequest.builder()
            .queryConditional(keyEqualTo { it.partitionValue(userId) })
            .build()
        return table().index("gsi_user_id").query(req).flatMap { it.items() }.toList()
    }

    fun put(item: SharedLinkItem) {
        table().putItem(item)
    }

    fun delete(token: String) {
        table().deleteItem(Key.builder().partitionValue(token).build())
    }
}

@DynamoDbBean
class SharedLinkItem {
    @get:DynamoDbPartitionKey
    @get:DynamoDbAttribute("token")
    var token: String? = null

    @get:DynamoDbSecondaryPartitionKey(indexNames = ["gsi_user_id"])
    @get:DynamoDbAttribute("userId")
    var userId: String? = null

    @get:DynamoDbAttribute("dataTypes")
    var dataTypes: List<String>? = null

    @get:DynamoDbAttribute("expiresAt")
    var expiresAt: String? = null
}
