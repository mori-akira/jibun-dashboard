package com.github.moriakira.jibundashboard.component

import com.github.moriakira.jibundashboard.service.CognitoUserInfoService
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component
import org.springframework.web.context.annotation.RequestScope

@Component
@RequestScope
class CurrentAuth(
    private val cognitoUserInfoService: CognitoUserInfoService,
) {
    val jwt: Jwt by lazy {
        val auth = SecurityContextHolder.getContext().authentication ?: error("No Authentication in context")
        (auth as? JwtAuthenticationToken)?.token ?: error("Authentication is not JwtAuthenticationToken")
    }

    val userId: String get() = jwt.subject
    val email: String by lazy {
        jwt.getClaimAsString("email") ?: runCatching { cognitoUserInfoService.fetch(jwt.tokenValue).email }.getOrNull()
            ?: error("No email specified")
    }
}
