package com.github.moriakira.jibundashboard

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.DynamoDbClientBuilder
import java.net.URI

@Configuration
class DynamoDbConfig(
    @param:Value("\${app.dynamodb.endpoint:}") private val endpoint: String?,
    @param:Value("\${app.dynamodb.region}") private val region: String,
) {
    @Bean
    fun dynamoDbClient(): DynamoDbClient {
        val builder: DynamoDbClientBuilder = DynamoDbClient.builder().region(Region.of(region)).credentialsProvider(
            DefaultCredentialsProvider.builder().build(),
        )

        if (!endpoint.isNullOrBlank()) {
            builder.endpointOverride(URI.create(endpoint))
        }
        return builder.build()
    }

    @Bean
    fun dynamoDbEnhancedClient(dynamoDbClient: DynamoDbClient): DynamoDbEnhancedClient =
        DynamoDbEnhancedClient.builder().dynamoDbClient(dynamoDbClient).build()
}
