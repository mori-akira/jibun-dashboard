package com.github.moriakira.jibundashboard.controller

import com.github.moriakira.jibundashboard.service.ResourceService
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.maps.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.http.HttpStatus

class ResourceControllerTest :
    StringSpec({

        val resourceService = mockk<ResourceService>()
        val controller = ResourceController(resourceService)

        "getI18n: 指定ロケールのメッセージを返す" {
            val locale = "ja"
            val messages = mapOf(
                "hello" to "こんにちは",
                "bye" to "さようなら",
            )
            every { resourceService.getI18nMessages(locale) } returns messages

            val res = controller.getI18n(locale)

            res.statusCode shouldBe HttpStatus.OK
            res.body!!.localeCode shouldBe locale
            res.body!!.messages!!.shouldContainExactly(messages)
            verify(exactly = 1) { resourceService.getI18nMessages(locale) }
        }

        "getI18n: メッセージが空でもOKを返す" {
            val locale = "en"
            every { resourceService.getI18nMessages(locale) } returns emptyMap()

            val res = controller.getI18n(locale)

            res.statusCode shouldBe HttpStatus.OK
            res.body!!.localeCode shouldBe locale
            res.body!!.messages!!.isEmpty() shouldBe true
            verify(exactly = 1) { resourceService.getI18nMessages(locale) }
        }
    })
