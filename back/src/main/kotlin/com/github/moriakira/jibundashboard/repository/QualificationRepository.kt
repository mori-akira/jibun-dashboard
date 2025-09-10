package com.github.moriakira.jibundashboard.repository

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Repository
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable
import software.amazon.awssdk.enhanced.dynamodb.Expression
import software.amazon.awssdk.enhanced.dynamodb.Key
import software.amazon.awssdk.enhanced.dynamodb.TableSchema
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional.keyEqualTo
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest
import software.amazon.awssdk.services.dynamodb.model.AttributeValue

@Repository
class QualificationRepository(
    private val enhanced: DynamoDbEnhancedClient,
    @param:Value("\${app.dynamodb.tables.qualifications}") private val tableName: String,
) {
    private val schema: TableSchema<QualificationItem> = TableSchema.fromBean(QualificationItem::class.java)
    private fun table(): DynamoDbTable<QualificationItem> = enhanced.table(tableName, schema)

    fun get(userId: String, qualificationId: String): QualificationItem? =
        table().getItem(Key.builder().partitionValue(userId).sortValue(qualificationId).build())

    fun getByQualificationId(qualificationId: String): QualificationItem? {
        val index = table().index("gsi_qualification_id")
        val req =
            QueryEnhancedRequest.builder().queryConditional(keyEqualTo { it.partitionValue(qualificationId) }).limit(1)
                .build()
        return index.query(req).flatMap { it.items().toList() }.firstOrNull()
    }

    @Suppress("ComplexMethod", "LongParameterList", "ReturnCount")
    fun query(
        userId: String,
        qualificationName: String? = null,
        statuses: List<String>? = null,
        ranks: List<String>? = null,
        acquiredDateFrom: String? = null,
        organization: String? = null,
        acquiredDateTo: String? = null,
        expirationDateFrom: String? = null,
        expirationDateTo: String? = null,
        limit: Int? = null, // 必要なら上限を指定（nullなら全件）
    ): List<QualificationItem> {
        val names = mutableMapOf<String, String>()
        val values = mutableMapOf<String, AttributeValue>()
        val conds = mutableListOf<String>()

        qualificationName?.let {
            names["#qualificationName"] = "qualificationName"
            values[":qualificationName"] = AttributeValue.builder().s(it).build()
            conds += "contains(#qualificationName, :qualificationName)"
        }

        organization?.let {
            names["#organization"] = "organization"
            values[":organization"] = AttributeValue.builder().s(it).build()
            conds += "contains(#organization, :organization)"
        }

        if (!statuses.isNullOrEmpty()) {
            names["#status"] = "status"
            val placeholders = statuses.mapIndexed { i, s ->
                val k = ":status$i"
                values[k] = AttributeValue.builder().s(s).build()
                k
            }
            conds += "#status IN (${placeholders.joinToString(",")})"
        }

        if (!ranks.isNullOrEmpty()) {
            names["#rank"] = "rank"
            val placeholders = ranks.mapIndexed { i, r ->
                val k = ":rank$i"
                values[k] = AttributeValue.builder().s(r).build()
                k
            }
            conds += "#rank IN (${placeholders.joinToString(",")})"
        }

        if (acquiredDateFrom != null || acquiredDateTo != null) {
            names["#acquiredDate"] = "acquiredDate"
            when {
                acquiredDateFrom != null && acquiredDateTo != null -> {
                    values[":acqFrom"] = AttributeValue.builder().s(acquiredDateFrom).build()
                    values[":acqTo"] = AttributeValue.builder().s(acquiredDateTo).build()
                    conds += "#acquiredDate BETWEEN :acqFrom AND :acqTo"
                }

                acquiredDateFrom != null -> {
                    values[":acqFrom"] = AttributeValue.builder().s(acquiredDateFrom).build()
                    conds += "#acquiredDate >= :acqFrom"
                }

                else -> {
                    values[":acqTo"] = AttributeValue.builder().s(acquiredDateTo).build()
                    conds += "#acquiredDate <= :acqTo"
                }
            }
        }

        if (expirationDateFrom != null || expirationDateTo != null) {
            names["#expirationDate"] = "expirationDate"
            when {
                expirationDateFrom != null && expirationDateTo != null -> {
                    values[":expFrom"] = AttributeValue.builder().s(expirationDateFrom).build()
                    values[":expTo"] = AttributeValue.builder().s(expirationDateTo).build()
                    conds += "#expirationDate BETWEEN :expFrom AND :expTo"
                }

                expirationDateFrom != null -> {
                    values[":expFrom"] = AttributeValue.builder().s(expirationDateFrom).build()
                    conds += "#expirationDate >= :expFrom"
                }

                else -> {
                    values[":expTo"] = AttributeValue.builder().s(expirationDateTo).build()
                    conds += "#expirationDate <= :expTo"
                }
            }
        }

        val filterExpr = if (conds.isEmpty()) {
            null
        } else {
            Expression.builder().expression(conds.joinToString(" AND ")).expressionNames(names.ifEmpty { null })
                .expressionValues(values.ifEmpty { null }).build()
        }

        val base = QueryEnhancedRequest.builder().queryConditional(keyEqualTo { it.partitionValue(userId) })
            .also { b -> filterExpr?.let { b.filterExpression(it) } }.also { b -> limit?.let { b.limit(it) } }.build()

        val pages = table().query(base)
        return pages.flatMap { it.items().toList() }.toList()
    }

    fun put(item: QualificationItem) {
        table().putItem(item)
    }

    fun delete(userId: String, qualificationId: String) {
        table().deleteItem(Key.builder().partitionValue(userId).sortValue(qualificationId).build())
    }
}

class QualificationItem {
    @get:DynamoDbSortKey
    @get:DynamoDbAttribute("qualificationId")
    val qualificationId: String? = null

    @get:DynamoDbPartitionKey
    @get:DynamoDbAttribute("userId")
    val userId: String? = null

    @get:DynamoDbAttribute("qualificationName")
    val qualificationName: String? = null

    @get:DynamoDbAttribute("abbreviation")
    val abbreviation: String? = null

    @get:DynamoDbAttribute("version")
    val version: String? = null

    @get:DynamoDbAttribute("status")
    val status: String? = null

    @get:DynamoDbAttribute("rank")
    val rank: String? = null

    @get:DynamoDbAttribute("organization")
    val organization: String? = null

    @get:DynamoDbAttribute("acquiredDate")
    val acquiredDate: String? = null

    @get:DynamoDbAttribute("expirationDate")
    val expirationDate: String? = null

    @get:DynamoDbAttribute("officialUrl")
    val officialUrl: String? = null

    @get:DynamoDbAttribute("certificationUrl")
    val certificationUrl: String? = null

    @get:DynamoDbAttribute("badgeUrl")
    val badgeUrl: String? = null
}
