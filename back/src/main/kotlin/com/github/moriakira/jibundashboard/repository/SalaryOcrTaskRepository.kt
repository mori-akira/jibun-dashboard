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
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest

@Repository
class SalaryOcrTaskRepository(
    private val enhanced: DynamoDbEnhancedClient,
    @param:Value("\${app.dynamodb.tables.salary-ocr-tasks}") private val tableName: String,
) {

    private val schema: TableSchema<SalaryOcrTaskItem> = TableSchema.fromBean(SalaryOcrTaskItem::class.java)
    private fun table(): DynamoDbTable<SalaryOcrTaskItem> = enhanced.table(tableName, schema)

    fun getByTaskId(taskId: String): SalaryOcrTaskItem? =
        table().getItem(Key.builder().partitionValue(taskId).build())

    fun findByUserAndDate(userId: String, targetDate: String): List<SalaryOcrTaskItem> {
        val index = table().index("gsi_user_target_date")
        val req = QueryEnhancedRequest.builder()
            .queryConditional(
                QueryConditional.keyEqualTo(
                    Key.builder()
                        .partitionValue(userId)
                        .sortValue(targetDate)
                        .build(),
                ),
            )
            .scanIndexForward(false)
            .build()
        return index.query(req).flatMap { it.items().toList() }
    }

    fun put(item: SalaryOcrTaskItem) {
        table().putItem(item)
    }
}

@DynamoDbBean
class SalaryOcrTaskItem {
    @get:DynamoDbPartitionKey
    @get:DynamoDbAttribute("taskId")
    var taskId: String? = null

    @get:DynamoDbSecondaryPartitionKey(indexNames = ["gsi_user_target_date"])
    @get:DynamoDbAttribute("userId")
    var userId: String? = null

    @get:DynamoDbSecondarySortKey(indexNames = ["gsi_user_target_date"])
    @get:DynamoDbAttribute("targetDate")
    var targetDate: String? = null

    @get:DynamoDbAttribute("status")
    var status: String? = null

    @get:DynamoDbAttribute("createdAt")
    var createdAt: String? = null

    @get:DynamoDbAttribute("updatedAt")
    var updatedAt: String? = null
}
