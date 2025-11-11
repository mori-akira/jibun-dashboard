package com.github.moriakira.jibundashboard.configuration

import com.openai.client.OpenAIClient
import com.openai.client.okhttp.OpenAIOkHttpClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenAIConfig {
    @Bean
    fun openAIClient(): OpenAIClient = OpenAIOkHttpClient.fromEnv()
}
