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
class CardbookQuizHistoryRepository(
    private val enhanced: DynamoDbEnhancedClient,
    @param:Value("\${app.dynamodb.tables.cardbook_quiz_histories}") private val tableName: String,
) {
    private val schema: TableSchema<CardbookQuizHistoryItem> =
        TableSchema.fromBean(CardbookQuizHistoryItem::class.java)

    private fun table(): DynamoDbTable<CardbookQuizHistoryItem> = enhanced.table(tableName, schema)

    fun getByUserAndQuizHistoryId(userId: String, quizHistoryId: String): CardbookQuizHistoryItem? =
        table().getItem(Key.builder().partitionValue(userId).sortValue(quizHistoryId).build())

    fun findByUser(userId: String): List<CardbookQuizHistoryItem> {
        val req =
            QueryEnhancedRequest.builder()
                .queryConditional(keyEqualTo { it.partitionValue(userId) })
                .build()
        return table().query(req).flatMap { it.items().toList() }.toList()
    }

    fun put(item: CardbookQuizHistoryItem) {
        table().putItem(item)
    }

    fun delete(userId: String, quizHistoryId: String) {
        table().deleteItem(Key.builder().partitionValue(userId).sortValue(quizHistoryId).build())
    }
}

@DynamoDbBean
class CardbookQuizHistoryItem {
    @get:DynamoDbPartitionKey
    @get:DynamoDbAttribute("userId")
    var userId: String? = null

    @get:DynamoDbSortKey
    @get:DynamoDbAttribute("quizHistoryId")
    var quizHistoryId: String? = null

    @get:DynamoDbAttribute("cardbookId")
    var cardbookId: String? = null

    @get:DynamoDbAttribute("answeredAt")
    var answeredAt: String? = null

    @get:DynamoDbAttribute("questionCount")
    var questionCount: Int? = null

    @get:DynamoDbAttribute("direction")
    var direction: String? = null

    @get:DynamoDbAttribute("correctCount")
    var correctCount: Int? = null

    @get:DynamoDbAttribute("incorrectCount")
    var incorrectCount: Int? = null

    @get:DynamoDbAttribute("answers")
    var answers: List<CardbookQuizHistoryAnswerItem>? = null
}

@DynamoDbBean
class CardbookQuizHistoryAnswerItem {
    @get:DynamoDbAttribute("cardId")
    var cardId: String? = null

    @get:DynamoDbAttribute("result")
    var result: String? = null
}
