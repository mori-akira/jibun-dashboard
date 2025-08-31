package com.github.moriakira.jibundashboard.api

import com.github.moriakira.jibundashboard.generated.api.ResourceApi
import com.github.moriakira.jibundashboard.generated.model.I18n
import com.github.moriakira.jibundashboard.service.ResourceService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class ResourceController(
    private val resourceService: ResourceService,
) : ResourceApi {

    override fun getI18n(localeCode: String): ResponseEntity<I18n> {
        val messages = resourceService.getI18nMessages(localeCode)
        return ResponseEntity.ok(
            I18n(
                localeCode = localeCode,
                messages,
            ),
        )
    }
}
