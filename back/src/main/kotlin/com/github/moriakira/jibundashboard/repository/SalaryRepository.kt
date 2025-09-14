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
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest

@Repository
class SalaryRepository(
    private val enhanced: DynamoDbEnhancedClient,
    @param:Value("\${app.dynamodb.tables.salaries}") private val tableName: String,
) {
    private val schema: TableSchema<SalaryItem> = TableSchema.fromBean(SalaryItem::class.java)
    private fun table(): DynamoDbTable<SalaryItem> = enhanced.table(tableName, schema)

    fun get(userId: String, targetDate: String): SalaryItem? =
        table().getItem(Key.builder().partitionValue(userId).sortValue(targetDate).build())

    fun getBySalaryId(salaryId: String): SalaryItem? {
        val index = table().index("gsi_salary_id")
        val req =
            QueryEnhancedRequest.builder().queryConditional(QueryConditional.keyEqualTo { it.partitionValue(salaryId) })
                .limit(1).build()
        return index.query(req).flatMap { it.items().toList() }.firstOrNull()
    }

    fun findByUser(userId: String): List<SalaryItem> {
        val req =
            QueryEnhancedRequest.builder().queryConditional(QueryConditional.keyEqualTo { it.partitionValue(userId) })
                .build()
        return table().query(req).flatMap { it.items().toList() }
    }

    fun findByUserAndDate(userId: String, targetDate: String): List<SalaryItem> {
        val req = QueryEnhancedRequest.builder().queryConditional(
            QueryConditional.keyEqualTo(
                Key.builder().partitionValue(userId).sortValue(targetDate).build(),
            ),
        ).build()
        return table().query(req).flatMap { it.items().toList() }
    }

    fun findByUserAndDateRange(userId: String, from: String?, to: String?): List<SalaryItem> {
        val cond = when {
            from != null && to != null -> QueryConditional.sortBetween(
                Key.builder().partitionValue(userId).sortValue(from).build(),
                Key.builder().partitionValue(userId).sortValue(to).build(),
            )

            from != null -> QueryConditional.sortGreaterThanOrEqualTo(
                Key.builder().partitionValue(userId).sortValue(from).build(),
            )

            to != null -> QueryConditional.sortLessThanOrEqualTo(
                Key.builder().partitionValue(userId).sortValue(to).build(),
            )

            else -> QueryConditional.keyEqualTo { it.partitionValue(userId) }
        }
        val req = QueryEnhancedRequest.builder()
            .queryConditional(cond)
            .scanIndexForward(true)
            .build()
        return table().query(req).flatMap { it.items().toList() }
    }

    fun put(item: SalaryItem) {
        table().putItem(item)
    }

    fun delete(userId: String, targetDate: String) {
        table().deleteItem(Key.builder().partitionValue(userId).sortValue(targetDate).build())
    }
}

@DynamoDbBean
class SalaryItem {
    @get:DynamoDbSecondaryPartitionKey(indexNames = ["gsi_salary_id"])
    @get:DynamoDbAttribute("salaryId")
    var salaryId: String? = null

    @get:DynamoDbPartitionKey
    @get:DynamoDbAttribute("userId")
    var userId: String? = null

    @get:DynamoDbSortKey
    @get:DynamoDbAttribute("targetDate")
    var targetDate: String? = null // ISO yyyy-MM-dd

    @get:DynamoDbAttribute("overview")
    var overview: Overview? = null

    @get:DynamoDbAttribute("structure")
    var structure: Structure? = null

    @get:DynamoDbAttribute("payslipData")
    var payslipData: List<PayslipCategory>? = null

    @DynamoDbBean
    class Overview {
        @get:DynamoDbAttribute("grossIncome")
        var grossIncome: Int? = null

        @get:DynamoDbAttribute("netIncome")
        var netIncome: Int? = null

        @get:DynamoDbAttribute("operatingTime")
        var operatingTime: Double? = null

        @get:DynamoDbAttribute("overtime")
        var overtime: Double? = null

        @get:DynamoDbAttribute("bonus")
        var bonus: Int? = null

        @get:DynamoDbAttribute("bonusTakeHome")
        var bonusTakeHome: Int? = null
    }

    @DynamoDbBean
    class Structure {
        @get:DynamoDbAttribute("basicSalary")
        var basicSalary: Int? = null

        @get:DynamoDbAttribute("overtimePay")
        var overtimePay: Int? = null

        @get:DynamoDbAttribute("housingAllowance")
        var housingAllowance: Int? = null

        @get:DynamoDbAttribute("positionAllowance")
        var positionAllowance: Int? = null

        @get:DynamoDbAttribute("other")
        var other: Int? = null
    }

    @DynamoDbBean
    class PayslipCategory {
        @get:DynamoDbAttribute("key")
        var key: String? = null

        @get:DynamoDbAttribute("data")
        var data: List<PayslipEntry>? = null
    }

    @DynamoDbBean
    class PayslipEntry {
        @get:DynamoDbAttribute("key")
        var key: String? = null

        @get:DynamoDbAttribute("data")
        var data: Double? = null
    }
}
