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
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional.keyEqualTo
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest

@Repository
class CardbookCheckResultRepository(
    private val enhanced: DynamoDbEnhancedClient,
    @param:Value("\${app.dynamodb.tables.cardbook_check_results}") private val tableName: String,
) {
    private val schema: TableSchema<CardbookCheckResultItem> =
        TableSchema.fromBean(CardbookCheckResultItem::class.java)

    private fun table(): DynamoDbTable<CardbookCheckResultItem> = enhanced.table(tableName, schema)

    fun findByUser(userId: String): List<CardbookCheckResultItem> {
        val req =
            QueryEnhancedRequest.builder()
                .queryConditional(keyEqualTo { it.partitionValue(userId) })
                .build()
        return table().query(req).flatMap { it.items().toList() }.toList()
    }

    fun getByCheckResultId(checkResultId: String): CardbookCheckResultItem? {
        val index = table().index("gsi_cardbook_check_result_id")
        val req =
            QueryEnhancedRequest.builder()
                .queryConditional(keyEqualTo { it.partitionValue(checkResultId) })
                .limit(1)
                .build()
        return index.query(req).flatMap { it.items().toList() }.firstOrNull()
    }

    fun put(item: CardbookCheckResultItem) {
        table().putItem(item)
    }

    fun delete(userId: String, cardId: String) {
        table().deleteItem(Key.builder().partitionValue(userId).sortValue(cardId).build())
    }
}

@DynamoDbBean
class CardbookCheckResultItem {
    @get:DynamoDbPartitionKey
    @get:DynamoDbAttribute("userId")
    var userId: String? = null

    @get:DynamoDbSortKey
    @get:DynamoDbAttribute("cardId")
    var cardId: String? = null

    @get:DynamoDbSecondaryPartitionKey(indexNames = ["gsi_cardbook_check_result_id"])
    @get:DynamoDbAttribute("cardbookCheckResultId")
    var cardbookCheckResultId: String? = null

    @get:DynamoDbAttribute("cardbookId")
    var cardbookId: String? = null

    @get:DynamoDbAttribute("front")
    var front: String? = null

    @get:DynamoDbAttribute("severity")
    var severity: String? = null

    @get:DynamoDbAttribute("status")
    var status: String? = null

    @get:DynamoDbAttribute("report")
    var report: String? = null

    @get:DynamoDbAttribute("cardUpdatedAt")
    var cardUpdatedAt: String? = null

    @get:DynamoDbAttribute("checkedAt")
    var checkedAt: String? = null
}
