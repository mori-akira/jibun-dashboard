package com.github.moriakira.jibundashboard.resource.api

import com.github.moriakira.jibundashboard.resource.generated.api.ResourceApi
import com.github.moriakira.jibundashboard.resource.generated.model.I18n
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class ResourceController : ResourceApi {
    override fun getI18n(localeCode: String): ResponseEntity<I18n> = ResponseEntity.ok(
        I18n(
            localeCode = "en",
            messages = mapOf(
                "message.info.completeSuccessfully" to "Process completed successfully.",
                "message.confirm.checkUnsavedChanges" to "You have unsaved changes. Continue?",
                "message.error.invalidInput" to "There is an error in your input.",
            ),
        ),
    )
}
