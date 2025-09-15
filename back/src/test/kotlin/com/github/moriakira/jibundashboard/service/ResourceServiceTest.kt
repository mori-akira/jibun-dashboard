package com.github.moriakira.jibundashboard.service

import com.github.moriakira.jibundashboard.repository.I18nItem
import com.github.moriakira.jibundashboard.repository.ResourceRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.maps.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class ResourceServiceTest :
    StringSpec({

        val repository = mockk<ResourceRepository>()
        val service = ResourceService(repository)

        "getI18nMessages: 正常に Map を返す" {
            val i1 = I18nItem().apply {
                localeCode = "ja"
                messageKey = "hello"
                message = "こんにちは"
            }
            val i2 = I18nItem().apply {
                localeCode = "ja"
                messageKey = "bye"
                message = "さようなら"
            }
            every { repository.findI18nByLocaleCode("ja") } returns listOf(i1, i2)

            val result = service.getI18nMessages("ja")

            result.shouldContainExactly(mapOf("hello" to "こんにちは", "bye" to "さようなら"))
            verify(exactly = 1) { repository.findI18nByLocaleCode("ja") }
        }

        "getI18nMessages: 該当なしなら空 Map" {
            every { repository.findI18nByLocaleCode("en") } returns emptyList()

            val result = service.getI18nMessages("en")

            result.isEmpty() shouldBe true
            verify(exactly = 1) { repository.findI18nByLocaleCode("en") }
        }
    })
