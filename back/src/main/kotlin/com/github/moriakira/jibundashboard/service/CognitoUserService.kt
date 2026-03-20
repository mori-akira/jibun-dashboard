package com.github.moriakira.jibundashboard.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminGetUserRequest
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType
import software.amazon.awssdk.services.cognitoidentityprovider.model.ChangePasswordRequest
import software.amazon.awssdk.services.cognitoidentityprovider.model.UpdateUserAttributesRequest

@Service
class CognitoUserService(
    @param:Value("\${app.security.cognito.user-pool-id:}") private val userPoolId: String?,
    @param:Value("\${app.security.cognito.region:ap-northeast-1}") private val cognitoRegion: String,
) {
    fun fetch(username: String): CognitoUserInfo {
        val email = CognitoIdentityProviderClient.builder()
            .region(Region.of(cognitoRegion))
            .build()
            .use { client ->
                client.adminGetUser(
                    AdminGetUserRequest.builder()
                        .userPoolId(userPoolId)
                        .username(username)
                        .build(),
                ).userAttributes().find { it.name() == "email" }?.value()
            }
        return CognitoUserInfo(sub = username, email = email)
    }

    fun updateEmail(accessToken: String, email: String) {
        CognitoIdentityProviderClient.builder()
            .region(Region.of(cognitoRegion))
            .build()
            .use { client ->
                val emailAttr = AttributeType.builder()
                    .name("email")
                    .value(email)
                    .build()
                val request = UpdateUserAttributesRequest.builder()
                    .accessToken(accessToken)
                    .userAttributes(emailAttr)
                    .build()
                client.updateUserAttributes(request)
            }
    }

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
                        .build(),
                )
            }
    }
}

data class CognitoUserInfo(
    val sub: String? = null,
    val email: String? = null,
)
