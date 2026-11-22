package com.github.moriakira.jibundashboard.component

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import jakarta.servlet.http.HttpServletResponse
import org.springframework.mock.web.MockFilterChain
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse

class OriginVerifyFilterTest :
    StringSpec({
        fun run(filter: OriginVerifyFilter, uri: String, header: String?): Pair<Int, Boolean> {
            val request = MockHttpServletRequest("GET", uri).apply {
                contextPath = "/api/v1"
                header?.let { addHeader(OriginVerifyFilter.HEADER_NAME, it) }
            }
            val response = MockHttpServletResponse()
            val chain = MockFilterChain()
            filter.doFilter(request, response, chain)
            return response.status to (chain.request != null)
        }

        val filter = OriginVerifyFilter("s3cret")

        "ヘッダが一致すれば通す" {
            run(filter, "/api/v1/salaries", "s3cret") shouldBe (HttpServletResponse.SC_OK to true)
        }

        "ヘッダがない・一致しなければ 403" {
            run(filter, "/api/v1/salaries", null) shouldBe (HttpServletResponse.SC_FORBIDDEN to false)
            run(filter, "/api/v1/salaries", "wrong") shouldBe (HttpServletResponse.SC_FORBIDDEN to false)
        }

        "ヘルスチェックはヘッダなしでも通す" {
            run(filter, "/api/v1/actuator/health", null) shouldBe (HttpServletResponse.SC_OK to true)
        }

        "シークレット未設定なら検証しない" {
            run(OriginVerifyFilter(""), "/api/v1/salaries", null) shouldBe (HttpServletResponse.SC_OK to true)
        }
    })
