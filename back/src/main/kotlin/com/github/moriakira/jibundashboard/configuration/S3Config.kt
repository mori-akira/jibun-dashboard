package com.github.moriakira.jibundashboard.configuration

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Configuration
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import java.net.URI

@Configuration
class S3Config(
    @param:Value("\${app.s3.region}") private val region: String,
    @param:Value("\${app.s3.endpoint:}") private val endpoint: String?,
    @param:Value("\${app.s3.enable-path-style-access:}") private val enablePathStyleAccess: Boolean?,
) {

    @Bean(destroyMethod = "close")
    fun s3Presigner(): S3Presigner {
        val builder = S3Presigner.builder()
            .region(Region.of(region))
            .credentialsProvider(DefaultCredentialsProvider.builder().build())

        if (!endpoint.isNullOrBlank()) {
            builder.endpointOverride(URI.create(endpoint))
        }
        if (enablePathStyleAccess == true) {
            builder.serviceConfiguration(
                S3Configuration.builder()
                    .pathStyleAccessEnabled(true)
                    .build(),
            )
        }
        return builder.build()
    }
}
