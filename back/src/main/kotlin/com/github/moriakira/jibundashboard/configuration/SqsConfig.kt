package com.github.moriakira.jibundashboard.configuration

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sqs.SqsClient
import java.net.URI

@Configuration
class SqsConfig(
    @param:Value("\${app.sqs.region}") private val region: String,
    @param:Value("\${app.sqs.endpoint:}") private val endpoint: String?,
) {

    @Bean(destroyMethod = "close")
    fun sqsClient(): SqsClient {
        val builder = SqsClient.builder()
            .region(Region.of(region))
            .credentialsProvider(DefaultCredentialsProvider.builder().build())

        if (!endpoint.isNullOrBlank()) {
            builder.endpointOverride(URI.create(endpoint))
        }

        return builder.build()
    }
}
