package com.github.moriakira.jibundashboard.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import java.time.Duration

@Service
class CognitoUserInfoService(
    private val webClient: WebClient,
    @param:Value("\${app.security.cognito.base-uri:}") private val baseUri: String?,
) {
    fun fetch(accessToken: String): CognitoUserInfo = webClient.get()
        .uri("$baseUri/oauth2/userInfo")
        .header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
        .retrieve()
        .bodyToMono(CognitoUserInfo::class.java)
        .timeout(Duration.ofSeconds(5))
        .block() ?: CognitoUserInfo()
}

data class CognitoUserInfo(
    val sub: String? = null,
    val email: String? = null,
)
