package com.github.moriakira.jibundashboard.repository

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Repository
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable
import software.amazon.awssdk.enhanced.dynamodb.Expression
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
import software.amazon.awssdk.services.dynamodb.model.AttributeValue

@Repository
class VocabularyTagRepository(
    private val enhanced: DynamoDbEnhancedClient,
    @param:Value("\${app.dynamodb.tables.vocabulary_tags}") private val tableName: String,
) {
    private val schema: TableSchema<VocabularyTagItem> = TableSchema.fromBean(VocabularyTagItem::class.java)
    private fun table(): DynamoDbTable<VocabularyTagItem> = enhanced.table(tableName, schema)

    fun getByVocabularyTagId(vocabularyTagId: String): VocabularyTagItem? {
        val index = table().index("gsi_vocabulary_tag_id")
        val req =
            QueryEnhancedRequest.builder().queryConditional(keyEqualTo { it.partitionValue(vocabularyTagId) }).limit(1)
                .build()
        return index.query(req).flatMap { it.items().toList() }.firstOrNull()
    }

    fun findByUser(
        userId: String,
        vocabularyTag: String? = null,
    ): List<VocabularyTagItem> {
        val names = mutableMapOf<String, String>()
        val values = mutableMapOf<String, AttributeValue>()
        val conditions = mutableListOf<String>()

        vocabularyTag?.let {
            names["#vocabularyTag"] = "vocabularyTag"
            values[":vocabularyTag"] = AttributeValue.builder().s(it).build()
            conditions += "contains(#vocabularyTag, :vocabularyTag)"
        }

        val filterExpr =
            if (conditions.isEmpty()) {
                null
            } else {
                Expression.builder().expression(conditions.joinToString(" AND "))
                    .expressionNames(names.ifEmpty { null })
                    .expressionValues(values.ifEmpty { null }).build()
            }

        val req =
            QueryEnhancedRequest.builder()
                .queryConditional(keyEqualTo { it.partitionValue(userId) })
                .also { b -> filterExpr?.let { b.filterExpression(it) } }
                .build()

        return table().index("gsi_user_order").query(req).flatMap { it.items().toList() }.toList()
    }

    fun put(item: VocabularyTagItem) {
        table().putItem(item)
    }

    fun delete(userId: String, vocabularyTagId: String) {
        table().deleteItem(Key.builder().partitionValue(userId).sortValue(vocabularyTagId).build())
    }
}

@DynamoDbBean
class VocabularyTagItem {
    @get:DynamoDbSortKey
    @get:DynamoDbSecondaryPartitionKey(indexNames = ["gsi_vocabulary_tag_id"])
    @get:DynamoDbAttribute("vocabularyTagId")
    var vocabularyTagId: String? = null

    @get:DynamoDbPartitionKey
    @get:DynamoDbSecondaryPartitionKey(indexNames = ["gsi_user_order"])
    @get:DynamoDbAttribute("userId")
    var userId: String? = null

    @get:DynamoDbAttribute("vocabularyTag")
    var vocabularyTag: String? = null

    @get:DynamoDbSecondarySortKey(indexNames = ["gsi_user_order"])
    @get:DynamoDbAttribute("order")
    var order: Int? = null
}
