package com.github.moriakira.jibundashboard.component

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class RequestLoggingFilter : OncePerRequestFilter() {
    private val log = LoggerFactory.getLogger(RequestLoggingFilter::class.java)

    init {
        log.info("RequestLoggingFilter initialized")
    }

    override fun shouldNotFilter(request: HttpServletRequest): Boolean =
        request.requestURI?.startsWith("/api/v1/actuator") ?: true

    @Suppress("TooGenericExceptionCaught", "ThrowingExceptionFromFinally")
    @Throws(Throwable::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val start = System.currentTimeMillis()
        var thrown: Throwable? = null
        try {
            filterChain.doFilter(request, response)
        } catch (t: Exception) {
            thrown = t
        } finally {
            val elapsed = System.currentTimeMillis() - start
            val trace = request.getHeader("x-amzn-trace-id") ?: ""
            val ua = request.getHeader("User-Agent") ?: ""
            val ip = request.getHeader("X-Forwarded-For")?.split(",")?.firstOrNull() ?: request.remoteAddr

            // 1行サマリ（JSONログに乗る）
            val map = mapOf(
                "method" to request.method,
                "path" to request.requestURI,
                "query" to maskQuery(request.queryString),
                "status" to response.status,
                "elapsed_ms" to elapsed,
                "user_agent" to ua,
                "ip" to ip,
                "host" to request.getHeader("Host"),
                "xfp" to request.getHeader("X-Forwarded-Proto"),
                "xfh" to request.getHeader("X-Forwarded-Host"),
                "amzn_trace" to trace,
            )
            if (thrown == null) {
                log.info("request_summary: {}", map)
            } else {
                log.error("request_summary_error: {}", map, thrown)
                throw thrown
            }
        }
    }

    private fun maskQuery(q: String?): String =
        q?.replace(Regex("(code|token|access_token|id_token)=([^&]+)"), "$1=***") ?: ""
}
