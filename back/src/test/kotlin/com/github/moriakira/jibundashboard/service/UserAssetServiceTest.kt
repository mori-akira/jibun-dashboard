package com.github.moriakira.jibundashboard.service

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.CopyObjectRequest
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest
import java.net.URL
import java.util.UUID

class UserAssetServiceTest :
    StringSpec({

        val s3Client = mockk<S3Client>(relaxed = true)
        val presigner = mockk<S3Presigner>(relaxed = true)
        val uploadsBucketName = "test-uploads-bucket"
        val userAssetsBucketName = "test-user-assets-bucket"
        val defaultExpiresIn = 3600
        val service = UserAssetService(s3Client, presigner, uploadsBucketName, userAssetsBucketName, defaultExpiresIn)

        beforeTest {
            clearMocks(s3Client, presigner, answers = false, recordedCalls = true, childMocks = true)
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

        "issuePresignedGetUrl: デフォルトの有効期限で正しいキーの署名付き URL を返す" {
            val userId = "u3"
            val assetId = UUID.fromString("33333333-3333-3333-3333-333333333333")
            val assetType = "qualification-certificate"
            val expectedUrl = URL("https://s3.example.com/user-assets/$assetType/$userId/$assetId")
            val presignedResponse = mockk<PresignedGetObjectRequest>(relaxed = true)

            every { presignedResponse.url() } returns expectedUrl
            every { presigner.presignGetObject(any<GetObjectPresignRequest>()) } returns presignedResponse

            val result = service.issuePresignedGetUrl(assetType, userId, assetId)

            result.url shouldBe expectedUrl.toURI()
            verify(exactly = 1) {
                presigner.presignGetObject(
                    match<GetObjectPresignRequest> { req ->
                        req.getObjectRequest().bucket() == userAssetsBucketName &&
                            req.getObjectRequest().key() == "$assetType/$userId/$assetId" &&
                            req.signatureDuration().seconds == defaultExpiresIn.toLong()
                    },
                )
            }
        }

        "issuePresignedGetUrl: expires を指定した場合はその値を有効期限に使用する" {
            val userId = "u4"
            val assetId = UUID.fromString("44444444-4444-4444-4444-444444444444")
            val assetType = "qualification-certificate"
            val customExpires = 7200
            val expectedUrl = URL("https://s3.example.com/user-assets/$assetType/$userId/$assetId")
            val presignedResponse = mockk<PresignedGetObjectRequest>(relaxed = true)

            every { presignedResponse.url() } returns expectedUrl
            every { presigner.presignGetObject(any<GetObjectPresignRequest>()) } returns presignedResponse

            val result = service.issuePresignedGetUrl(assetType, userId, assetId, customExpires)

            result.url shouldBe expectedUrl.toURI()
            verify(exactly = 1) {
                presigner.presignGetObject(
                    match<GetObjectPresignRequest> { req ->
                        req.signatureDuration().seconds == customExpires.toLong()
                    },
                )
            }
        }
    })
