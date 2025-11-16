package com.github.moriakira.jibundashboard.configuration

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sqs.SqsClient

@Configuration
class SqsConfig(
    @param:Value("\${app.sqs.region}") val region: String,
) {

    @Bean(destroyMethod = "close")
    fun sqsClient(): SqsClient = SqsClient.builder()
        .region(Region.of(region))
        .credentialsProvider(DefaultCredentialsProvider.builder().build())
        .build()
}
