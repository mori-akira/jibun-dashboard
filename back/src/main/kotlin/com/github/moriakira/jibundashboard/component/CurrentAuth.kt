package com.github.moriakira.jibundashboard.component

import com.github.moriakira.jibundashboard.service.CognitoUserService
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component
import org.springframework.web.context.annotation.RequestScope

@Component
@RequestScope
class CurrentAuth(
    private val cognitoUserInfoService: CognitoUserService,
) {
    private val logger = LoggerFactory.getLogger(CurrentAuth::class.java)

    val jwt: Jwt by lazy {
        val auth = SecurityContextHolder.getContext().authentication
            ?: throw AuthenticationCredentialsNotFoundException("No Authentication in context")
        (auth as? JwtAuthenticationToken)?.token
            ?: throw AuthenticationCredentialsNotFoundException("Authentication is not JwtAuthenticationToken")
    }

    val userId: String get() = jwt.subject
    val email: String by lazy {
        jwt.getClaimAsString("email") ?: run {
            val userInfo = runCatching { cognitoUserInfoService.fetch(jwt.subject) }
                .onFailure { logger.warn("Failed to fetch userInfo from Cognito: ${it.message}", it) }
                .getOrNull()
            userInfo?.email ?: error("No email specified")
        }
    }
}
