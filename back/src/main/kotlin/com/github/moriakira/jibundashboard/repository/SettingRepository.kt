package com.github.moriakira.jibundashboard.repository

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Repository
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient
import software.amazon.awssdk.enhanced.dynamodb.Key
import software.amazon.awssdk.enhanced.dynamodb.TableSchema
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey
import software.amazon.awssdk.enhanced.dynamodb.model.GetItemEnhancedRequest

@Repository
class SettingRepository(
    private val enhanced: DynamoDbEnhancedClient,
    @param:Value("\${app.dynamodb.tables.settings}") private val settingsTableName: String,
) {
    private val schema = TableSchema.fromBean(SettingItem::class.java)
    private fun table() = enhanced.table(settingsTableName, schema)

    fun get(userId: String): SettingItem? {
        return table().getItem(
            GetItemEnhancedRequest.builder().key(Key.builder().partitionValue(userId).build()).build()
        )
    }

    fun put(item: SettingItem) {
        table().putItem(item)
    }
}

@DynamoDbBean
class SettingItem {
    @get:DynamoDbPartitionKey
    @get:DynamoDbAttribute("userId")
    var userId: String? = null

    @get:DynamoDbAttribute("salary")
    var salary: SalarySetting? = null

    @get:DynamoDbAttribute("qualification")
    var qualification: QualificationSetting? = null
}

@DynamoDbBean
class SalarySetting {
    @get:DynamoDbAttribute("financialYearStartMonth")
    var financialYearStartMonth: Int? = null

    @get:DynamoDbAttribute("transitionItemCount")
    var transitionItemCount: Int? = null

    @get:DynamoDbAttribute("compareDataColors")
    var compareDataColors: List<String>? = null
}

@DynamoDbBean
class QualificationSetting {
    @get:DynamoDbAttribute("rankAColor")
    var rankAColor: String? = null

    @get:DynamoDbAttribute("rankBColor")
    var rankBColor: String? = null

    @get:DynamoDbAttribute("rankCColor")
    var rankCColor: String? = null

    @get:DynamoDbAttribute("rankDColor")
    var rankDColor: String? = null
}
