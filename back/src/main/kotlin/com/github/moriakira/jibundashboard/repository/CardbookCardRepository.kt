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
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondarySortKey
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional.keyEqualTo
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest

@Repository
class CardbookCardRepository(
    private val enhanced: DynamoDbEnhancedClient,
    @param:Value("\${app.dynamodb.tables.cardbook_cards}") private val tableName: String,
) {
    private val schema: TableSchema<CardbookCardItem> = TableSchema.fromBean(CardbookCardItem::class.java)

    private fun table(): DynamoDbTable<CardbookCardItem> = enhanced.table(tableName, schema)

    fun getByUserAndCardId(userId: String, cardId: String): CardbookCardItem? =
        table().getItem(Key.builder().partitionValue(userId).sortValue(cardId).build())

    fun findByCardbookId(cardbookId: String): List<CardbookCardItem> {
        val index = table().index("gsi_cardbook_id")
        val req =
            QueryEnhancedRequest.builder()
                .queryConditional(keyEqualTo { it.partitionValue(cardbookId) })
                .build()
        return index.query(req).flatMap { it.items().toList() }.toList()
    }

    fun put(item: CardbookCardItem) {
        table().putItem(item)
    }

    fun delete(userId: String, cardId: String) {
        table().deleteItem(Key.builder().partitionValue(userId).sortValue(cardId).build())
    }
}

@DynamoDbBean
class CardbookCardItem {
    @get:DynamoDbPartitionKey
    @get:DynamoDbAttribute("userId")
    var userId: String? = null

    @get:DynamoDbSortKey
    @get:DynamoDbAttribute("cardId")
    var cardId: String? = null

    @get:DynamoDbSecondaryPartitionKey(indexNames = ["gsi_cardbook_id"])
    @get:DynamoDbAttribute("cardbookId")
    var cardbookId: String? = null

    @get:DynamoDbSecondarySortKey(indexNames = ["gsi_cardbook_id"])
    @get:DynamoDbAttribute("createdDateTime")
    var createdDateTime: String? = null

    @get:DynamoDbAttribute("front")
    var front: String? = null

    @get:DynamoDbAttribute("back")
    var back: String? = null
}
