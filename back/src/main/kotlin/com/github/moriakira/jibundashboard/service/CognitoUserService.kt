package com.github.moriakira.jibundashboard.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient
import software.amazon.awssdk.services.cognitoidentityprovider.model.ChangePasswordRequest
import software.amazon.awssdk.services.cognitoidentityprovider.model.CognitoIdentityProviderException
import java.time.Duration

@Service
class CognitoUserService(
    private val webClient: WebClient,
    @param:Value("\${app.security.cognito.base-uri:}") private val baseUri: String?,
    @param:Value("\${app.security.cognito.region:ap-northeast-1}") private val cognitoRegion: String
) {
    fun fetch(accessToken: String): CognitoUserInfo = webClient.get()
        .uri("$baseUri/oauth2/userInfo")
        .header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
        .retrieve()
        .bodyToMono(CognitoUserInfo::class.java)
        .timeout(Duration.ofSeconds(5))
        .block() ?: CognitoUserInfo()

    fun changePassword(accessToken: String, oldPassword: String, newPassword: String) {
        CognitoIdentityProviderClient.builder()
            .region(Region.of(cognitoRegion))
            .build()
            .use { client ->
                client.changePassword(
                    ChangePasswordRequest.builder()
                        .accessToken(accessToken)
                        .previousPassword(oldPassword)
                        .proposedPassword(newPassword)
                        .build()
                )
            }
    }
}

data class CognitoUserInfo(
    val sub: String? = null,
    val email: String? = null,
)
