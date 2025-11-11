package com.github.moriakira.jibundashboard.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest
import java.net.URI
import java.time.Duration
import java.time.OffsetDateTime
import java.util.UUID

@Service
class FileUploadService(
    private val presigner: S3Presigner,
    @param:Value("\${app.s3.uploads-bucket.name}") private val uploadsBucketName: String,
    @param:Value("\${app.s3.uploads-bucket.default-expires-in-seconds}") private val defaultUrlExpirationSeconds: Int,
) {

    fun issuePresignedPutUrl(
        userId: String,
        fileId: UUID?,
        expires: Int?,
    ): PresignedUrlResult {
        val appliedFileId = fileId ?: UUID.randomUUID()
        val appliedExpires = expires ?: defaultUrlExpirationSeconds
        val key = uploadKey(userId, appliedFileId)
        val putObj: PutObjectRequest = PutObjectRequest.builder().bucket(uploadsBucketName).key(key).build()
        val presignReq: PutObjectPresignRequest =
            PutObjectPresignRequest.builder().signatureDuration(Duration.ofSeconds(appliedExpires.toLong()))
                .putObjectRequest(putObj).build()
        val presigned: PresignedPutObjectRequest = presigner.presignPutObject(presignReq)
        return PresignedUrlResult(
            fileId = appliedFileId,
            url = presigned.url().toURI(),
            expireDateTime = OffsetDateTime.now().plusSeconds(appliedExpires.toLong()),
        )
    }

    fun uploadKey(userId: String, fileId: UUID): String = "uploads/$userId/$fileId"
}

data class PresignedUrlResult(
    val fileId: UUID,
    val url: URI,
    val expireDateTime: OffsetDateTime,
)
