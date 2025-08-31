package com.github.moriakira.jibundashboard

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component
import org.springframework.web.context.annotation.RequestScope

@Component
@RequestScope
class CurrentAuth {
    val jwt: Jwt by lazy {
        val auth = SecurityContextHolder.getContext().authentication
            ?: error("No Authentication in context")
        val token = auth as? JwtAuthenticationToken
            ?: error("No Authentication in context")
        token.token
    }
    val userId: String get() = jwt.subject
    val email: String? get() = jwt.claims["email"] as? String
}
