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
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional.keyEqualTo
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest

@Repository
class CardbookRepository(
    private val enhanced: DynamoDbEnhancedClient,
    @param:Value("\${app.dynamodb.tables.cardbooks}") private val tableName: String,
) {
    private val schema: TableSchema<CardbookItem> = TableSchema.fromBean(CardbookItem::class.java)

    private fun table(): DynamoDbTable<CardbookItem> = enhanced.table(tableName, schema)

    fun getByUserAndCardbookId(userId: String, cardbookId: String): CardbookItem? =
        table().getItem(Key.builder().partitionValue(userId).sortValue(cardbookId).build())

    fun findByUser(userId: String): List<CardbookItem> {
        val req =
            QueryEnhancedRequest.builder()
                .queryConditional(keyEqualTo { it.partitionValue(userId) })
                .build()
        return table().query(req).flatMap { it.items().toList() }.toList()
    }

    fun put(item: CardbookItem) {
        table().putItem(item)
    }

    fun delete(userId: String, cardbookId: String) {
        table().deleteItem(Key.builder().partitionValue(userId).sortValue(cardbookId).build())
    }
}

@DynamoDbBean
class CardbookItem {
    @get:DynamoDbPartitionKey
    @get:DynamoDbAttribute("userId")
    var userId: String? = null

    @get:DynamoDbSortKey
    @get:DynamoDbAttribute("cardbookId")
    var cardbookId: String? = null

    @get:DynamoDbAttribute("name")
    var name: String? = null

    @get:DynamoDbAttribute("createdDateTime")
    var createdDateTime: String? = null

    @get:DynamoDbAttribute("updatedDateTime")
    var updatedDateTime: String? = null
}
