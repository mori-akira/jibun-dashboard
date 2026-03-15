package com.github.moriakira.jibundashboard.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.CopyObjectRequest
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
import java.net.URI
import java.time.Duration
import java.time.OffsetDateTime
import java.util.UUID

@Service
class UserAssetService(
    private val s3Client: S3Client,
    private val presigner: S3Presigner,
    @param:Value("\${app.s3.uploads-bucket.name}") private val uploadsBucketName: String,
    @param:Value("\${app.s3.user-assets-bucket.name}") private val userAssetsBucketName: String,
    @param:Value("\${app.s3.user-assets-bucket.default-expires-in-seconds}")
    private val defaultUrlExpirationSeconds: Int,
) {
    fun copyFromUploads(assetType: String, userId: String, fileId: UUID) {
        val sourceKey = "uploads/$userId/$fileId"
        val destKey = "$assetType/$userId/$fileId"
        s3Client.copyObject(
            CopyObjectRequest.builder().sourceBucket(uploadsBucketName).sourceKey(sourceKey)
                .destinationBucket(userAssetsBucketName).destinationKey(destKey).build(),
        )
    }

    fun delete(assetType: String, userId: String, fileId: UUID) {
        val key = "$assetType/$userId/$fileId"
        s3Client.deleteObject(
            DeleteObjectRequest.builder().bucket(userAssetsBucketName).key(key).build(),
        )
    }

    fun issuePresignedGetUrl(
        assetType: String,
        userId: String,
        assetId: UUID,
        expires: Int? = null,
    ): PresignedDownloadUrlResult {
        val appliedExpires = expires ?: defaultUrlExpirationSeconds
        val key = "$assetType/$userId/$assetId"
        val getObj = GetObjectRequest.builder().bucket(userAssetsBucketName).key(key).build()
        val presignReq =
            GetObjectPresignRequest.builder().signatureDuration(Duration.ofSeconds(appliedExpires.toLong()))
                .getObjectRequest(getObj).build()
        val presigned = presigner.presignGetObject(presignReq)
        return PresignedDownloadUrlResult(
            url = presigned.url().toURI(),
            expireDateTime = OffsetDateTime.now().plusSeconds(appliedExpires.toLong()),
        )
    }
}

data class PresignedDownloadUrlResult(
    val url: URI,
    val expireDateTime: OffsetDateTime,
)
