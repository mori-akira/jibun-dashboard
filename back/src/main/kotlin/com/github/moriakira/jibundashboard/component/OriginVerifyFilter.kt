package com.github.moriakira.jibundashboard.component

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.security.MessageDigest

/**
 * App Runner の公開 URL を直接叩かれると API Gateway の認可やレート制限を迂回されるため、
 * API Gateway だけが付与するヘッダを持たないリクエストを拒否する。
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class OriginVerifyFilter(@param:Value("\${app.security.origin-verify-secret}") secret: String) :
    OncePerRequestFilter() {
    private val expected = secret.toByteArray(Charsets.UTF_8)

    // App Runner のヘルスチェックはコンテナへ直接届き、ヘッダを付けられない
    override fun shouldNotFilter(request: HttpServletRequest): Boolean = expected.isEmpty() ||
        request.requestURI.removePrefix(request.contextPath) == "/actuator/health"

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val actual = request.getHeader(HEADER_NAME)?.toByteArray(Charsets.UTF_8)
        if (actual == null || !MessageDigest.isEqual(actual, expected)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN)
            return
        }
        filterChain.doFilter(request, response)
    }

    companion object {
        const val HEADER_NAME = "X-Origin-Verify"
    }
}
