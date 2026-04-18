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
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional.keyEqualTo
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest
import software.amazon.awssdk.services.dynamodb.model.AttributeValue

@Repository
class VocabularyRepository(
    private val enhanced: DynamoDbEnhancedClient,
    @param:Value("\${app.dynamodb.tables.vocabularies}") private val tableName: String,
) {
    private val schema: TableSchema<VocabularyItem> = TableSchema.fromBean(VocabularyItem::class.java)
    private fun table(): DynamoDbTable<VocabularyItem> = enhanced.table(tableName, schema)

    fun getByVocabularyId(vocabularyId: String): VocabularyItem? {
        val index = table().index("gsi_vocabulary_id")
        val req =
            QueryEnhancedRequest.builder().queryConditional(keyEqualTo { it.partitionValue(vocabularyId) }).limit(1)
                .build()
        return index.query(req).flatMap { it.items().toList() }.firstOrNull()
    }

    fun findByUser(
        userId: String,
        vocabularyName: String? = null,
        description: String? = null,
    ): List<VocabularyItem> {
        val names = mutableMapOf<String, String>()
        val values = mutableMapOf<String, AttributeValue>()
        val conditions = mutableListOf<String>()

        vocabularyName?.let {
            names["#name"] = "name"
            values[":name"] = AttributeValue.builder().s(it).build()
            conditions += "contains(#name, :name)"
        }

        description?.let {
            names["#description"] = "description"
            values[":description"] = AttributeValue.builder().s(it).build()
            conditions += "contains(#description, :description)"
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

        return table().query(req).flatMap { it.items().toList() }.toList()
    }

    fun put(item: VocabularyItem) {
        table().putItem(item)
    }

    fun delete(userId: String, vocabularyId: String) {
        table().deleteItem(Key.builder().partitionValue(userId).sortValue(vocabularyId).build())
    }
}

@DynamoDbBean
class VocabularyItem {
    @get:DynamoDbPartitionKey
    @get:DynamoDbAttribute("userId")
    var userId: String? = null

    @get:DynamoDbSortKey
    @get:DynamoDbSecondaryPartitionKey(indexNames = ["gsi_vocabulary_id"])
    @get:DynamoDbAttribute("vocabularyId")
    var vocabularyId: String? = null

    @get:DynamoDbAttribute("name")
    var name: String? = null

    @get:DynamoDbAttribute("description")
    var description: String? = null

    @get:DynamoDbAttribute("tagIds")
    var tagIds: List<String>? = null

    @get:DynamoDbAttribute("createdDateTime")
    var createdDateTime: String? = null

    @get:DynamoDbAttribute("updatedDateTime")
    var updatedDateTime: String? = null
}
