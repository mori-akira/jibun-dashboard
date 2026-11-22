package com.github.moriakira.jibundashboard.controller

import com.github.moriakira.jibundashboard.component.CurrentAuth
import com.github.moriakira.jibundashboard.generated.api.SharedLinkApi
import com.github.moriakira.jibundashboard.generated.model.SharedLink
import com.github.moriakira.jibundashboard.generated.model.SharedLinkBase
import com.github.moriakira.jibundashboard.mapper.toApi
import com.github.moriakira.jibundashboard.service.SharedLinkService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class SharedLinkController(private val currentAuth: CurrentAuth, private val sharedLinkService: SharedLinkService) :
    SharedLinkApi {

    override fun getSharedLinks(): ResponseEntity<List<SharedLink>> {
        val list = sharedLinkService.listByUser(currentAuth.userId)
        return ResponseEntity.ok(list.map { it.toApi() })
    }

    override fun postSharedLinks(sharedLinkBase: SharedLinkBase?): ResponseEntity<SharedLink> {
        requireNotNull(sharedLinkBase) { "Request body is required." }
        val model = sharedLinkService.create(
            userId = currentAuth.userId,
            dataTypes = sharedLinkBase.dataTypes.map { it.value },
            expiresAt = sharedLinkBase.expiresAt.toString(),
        )
        return ResponseEntity.status(201).body(model.toApi())
    }

    override fun deleteSharedLinksById(token: UUID): ResponseEntity<Unit> {
        sharedLinkService.delete(token.toString(), currentAuth.userId)
        return ResponseEntity.noContent().build()
    }
}
