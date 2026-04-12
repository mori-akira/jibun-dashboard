package com.github.moriakira.jibundashboard.controller

import com.github.moriakira.jibundashboard.service.ResourceService
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.maps.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.springframework.http.HttpStatus

class ResourceControllerTest :
    StringSpec({

        val resourceService = mockk<ResourceService>()
        val controller = ResourceController(resourceService)

        "getI18n: 指定ロケールのメッセージを返す" {
            val messages = mapOf("hello" to "こんにちは", "bye" to "さようなら")
            every { resourceService.getI18nMessages("ja") } returns messages

            val res = controller.getResourcesI18n("ja")

            res.statusCode shouldBe HttpStatus.OK
            res.body!!.localeCode shouldBe "ja"
            res.body!!.messages!!.shouldContainExactly(messages)
        }
    })
