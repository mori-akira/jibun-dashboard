package com.github.moriakira.jibundashboard.controller

import com.github.moriakira.jibundashboard.component.CurrentAuth
import com.github.moriakira.jibundashboard.generated.api.FileApi
import com.github.moriakira.jibundashboard.generated.model.UploadUrl
import com.github.moriakira.jibundashboard.service.FileUploadService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class FileController(
    private val currentAuth: CurrentAuth,
    private val fileUploadService: FileUploadService,
) : FileApi {

    override fun getUploadUrl(fileId: UUID?, expiresIn: Int?): ResponseEntity<UploadUrl> =
        fileUploadService.issuePresignedPutUrl(
            currentAuth.userId,
            fileId,
            expiresIn,
        ).let { res ->
            ResponseEntity.ok(
                UploadUrl(
                    fileId = res.fileId,
                    uploadUrl = res.url,
                    expireDateTime = res.expireDateTime,
                ),
            )
        }
}
