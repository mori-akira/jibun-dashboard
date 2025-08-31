package com.github.moriakira.jibundashboard.repository

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Repository
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient
import software.amazon.awssdk.enhanced.dynamodb.Key
import software.amazon.awssdk.enhanced.dynamodb.TableSchema
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey

@Repository
class UserRepository(
    private val enhanced: DynamoDbEnhancedClient,
    @param:Value("\${app.dynamodb.tables.users}") private val usersTableName: String,
) {
    private val schema = TableSchema.fromBean(UserItem::class.java)

    fun get(userId: String): UserItem? {
        val table = enhanced.table(usersTableName, schema)
        return table.getItem(Key.builder().partitionValue(userId).build())
    }

    fun put(item: UserItem) {
        val table = enhanced.table(usersTableName, schema)
        table.putItem(item)
    }
}

@DynamoDbBean
class UserItem {
    @get:DynamoDbPartitionKey
    @get:DynamoDbAttribute("userId")
    var userId: String? = null

    @get:DynamoDbAttribute("userName")
    var userName: String? = null

    @get:DynamoDbAttribute("emailAddress")
    var emailAddress: String? = null
}
