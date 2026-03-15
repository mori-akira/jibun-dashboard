package com.github.moriakira.jibundashboard.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.CopyObjectRequest
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import java.util.UUID

@Service
class UserAssetService(
    private val s3Client: S3Client,
    @param:Value("\${app.s3.uploads-bucket.name}") private val uploadsBucketName: String,
    @param:Value("\${app.s3.user-assets-bucket.name}") private val userAssetsBucketName: String,
) {
    fun copyFromUploads(assetType: String, userId: String, fileId: UUID) {
        val sourceKey = "uploads/$userId/$fileId"
        val destKey = "$assetType/$userId/$fileId"
        s3Client.copyObject(
            CopyObjectRequest.builder()
                .sourceBucket(uploadsBucketName)
                .sourceKey(sourceKey)
                .destinationBucket(userAssetsBucketName)
                .destinationKey(destKey)
                .build(),
        )
    }

    fun delete(assetType: String, userId: String, fileId: UUID) {
        val key = "$assetType/$userId/$fileId"
        s3Client.deleteObject(
            DeleteObjectRequest.builder()
                .bucket(userAssetsBucketName)
                .key(key)
                .build(),
        )
    }
}
