package com.github.moriakira.jibundashboard.service

import io.kotest.core.spec.style.StringSpec
import io.mockk.clearMocks
import io.mockk.mockk
import io.mockk.verify
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.CopyObjectRequest
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import java.util.UUID

class UserAssetServiceTest :
    StringSpec({

        val s3Client = mockk<S3Client>(relaxed = true)
        val uploadsBucketName = "test-uploads-bucket"
        val userAssetsBucketName = "test-user-assets-bucket"
        val service = UserAssetService(s3Client, uploadsBucketName, userAssetsBucketName)

        beforeTest {
            clearMocks(s3Client, answers = false, recordedCalls = true, childMocks = true)
        }

        "copyFromUploads: 正しいバケットとキーで S3 コピーを実行する" {
            val userId = "u1"
            val fileId = UUID.fromString("11111111-1111-1111-1111-111111111111")
            val assetType = "qualification-certifications"

            service.copyFromUploads(assetType, userId, fileId)

            verify(exactly = 1) {
                s3Client.copyObject(
                    match<CopyObjectRequest> { req ->
                        req.sourceBucket() == uploadsBucketName &&
                            req.sourceKey() == "uploads/$userId/$fileId" &&
                            req.destinationBucket() == userAssetsBucketName &&
                            req.destinationKey() == "$assetType/$userId/$fileId"
                    },
                )
            }
        }

        "delete: 正しいバケットとキーで S3 削除を実行する" {
            val userId = "u2"
            val fileId = UUID.fromString("22222222-2222-2222-2222-222222222222")
            val assetType = "qualification-certifications"

            service.delete(assetType, userId, fileId)

            verify(exactly = 1) {
                s3Client.deleteObject(
                    match<DeleteObjectRequest> { req ->
                        req.bucket() == userAssetsBucketName &&
                            req.key() == "$assetType/$userId/$fileId"
                    },
                )
            }
        }
    })
