package com.github.moriakira.jibundashboard

import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.context.annotation.RequestScope

@Component
@RequestScope
class CurrentAuth(
    private val request: HttpServletRequest,
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    val userId: String by lazy {
        request.getHeader("x-user-sub")?.also {
            log.info("Authenticated userId (sub) = {}", it)
        } ?: error("Missing header: x-user-sub")
    }

    val email: String? by lazy {
        request.getHeader("x-user-email")?.also {
            log.info("Authenticated email = {}", it)
        }
    }
}
