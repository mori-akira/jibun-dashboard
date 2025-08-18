package com.github.moriakira.jibundashboard.resource

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class RequestLoggingFilter : OncePerRequestFilter() {
    private val log = LoggerFactory.getLogger(this::class.java)

    override fun doFilterInternal(
        request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain
    ) {
        val start = System.currentTimeMillis()
        var thrown: Throwable? = null
        try {
            filterChain.doFilter(request, response)
        } catch (t: Throwable) {
            thrown = t
            throw t
        } finally {
            val elapsed = System.currentTimeMillis() - start
            val trace = request.getHeader("x-amzn-trace-id") ?: ""
            val ua = request.getHeader("User-Agent") ?: ""
            val ip = request.getHeader("X-Forwarded-For")?.split(",")?.firstOrNull() ?: request.remoteAddr

            // 1行サマリ（JSONログに乗る）
            val map = mapOf(
                "method" to request.method,
                "path" to request.requestURI,
                "query" to (request.queryString ?: ""),
                "status" to response.status,
                "elapsed_ms" to elapsed,
                "user_agent" to ua,
                "ip" to ip,
                "amzn_trace" to trace
            )
            if (thrown == null) {
                log.info("request_summary: {}", map)
            } else {
                log.error("request_summary_error: {}", map, thrown)
            }
        }
    }
}