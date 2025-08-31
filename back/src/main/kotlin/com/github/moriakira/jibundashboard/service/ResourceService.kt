package com.github.moriakira.jibundashboard.service

import com.github.moriakira.jibundashboard.repository.ResourceRepository
import org.springframework.stereotype.Service

@Service
class ResourceService(
    private val resourceRepository: ResourceRepository,
) {

    fun getI18nMessages(localeCode: String): Map<String, String> {
        val items = resourceRepository.findI18nByLocaleCode(localeCode)
        return items.associate { it.messageKey!! to it.message!! }
    }
}
