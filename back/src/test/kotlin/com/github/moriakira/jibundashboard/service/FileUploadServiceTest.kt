package com.github.moriakira.jibundashboard.service

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest
import java.net.URI
import java.time.Duration
import java.time.OffsetDateTime
import java.util.UUID

class FileUploadServiceTest :
    StringSpec({

        val presigner = mockk<S3Presigner>()
        val uploadsBucketName = "test-uploads-bucket"
        val defaultUrlExpirationSeconds = 3600
        val service = FileUploadService(presigner, uploadsBucketName, defaultUrlExpirationSeconds)

        beforeTest {
            clearMocks(presigner, answers = false, recordedCalls = true, childMocks = true)
        }

        "issuePresignedPutUrl: fileId 指定ありの場合は指定された fileId を使用する" {
            val userId = "user123"
            val fileId = UUID.fromString("12345678-1234-1234-1234-123456789abc")
            val expires = 7200

            val mockPresignedRequest = mockk<PresignedPutObjectRequest>()
            every {
                mockPresignedRequest.url()
            } returns URI.create(
                "https://s3.example.com/test-bucket/uploads/user123/12345678-1234-1234-1234-123456789abc",
            ).toURL()

            every { presigner.presignPutObject(any<PutObjectPresignRequest>()) } returns mockPresignedRequest

            val before = OffsetDateTime.now()
            val result = service.issuePresignedPutUrl(userId, fileId, expires)
            val after = OffsetDateTime.now().plusSeconds(expires.toLong())

            result.fileId shouldBe fileId
            result.url shouldBe URI.create(
                "https://s3.example.com/test-bucket/uploads/user123/12345678-1234-1234-1234-123456789abc",
            )
            result.expireDateTime shouldNotBe null
            result.expireDateTime.isAfter(before.plusSeconds(expires.toLong() - 1)) shouldBe true
            result.expireDateTime.isBefore(after.plusSeconds(1)) shouldBe true

            verify(exactly = 1) {
                presigner.presignPutObject(
                    match<PutObjectPresignRequest> { req ->
                        req.signatureDuration().equals(Duration.ofSeconds(expires.toLong())) &&
                            req.putObjectRequest().bucket() == uploadsBucketName &&
                            req.putObjectRequest().key() == "uploads/$userId/$fileId"
                    },
                )
            }
        }

        "issuePresignedPutUrl: fileId 指定なしの場合は UUID を自動生成する" {
            mockkStatic(UUID::class)
            val generatedFileId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
            every { UUID.randomUUID() } returns generatedFileId

            val userId = "user456"
            val expires = 1800

            val mockPresignedRequest = mockk<PresignedPutObjectRequest>()
            every {
                mockPresignedRequest.url()
            } returns URI.create(
                "https://s3.example.com/test-bucket/uploads/user456/aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
            ).toURL()

            every { presigner.presignPutObject(any<PutObjectPresignRequest>()) } returns mockPresignedRequest

            val result = service.issuePresignedPutUrl(userId, null, expires)

            result.fileId shouldBe generatedFileId
            result.url shouldBe URI.create(
                "https://s3.example.com/test-bucket/uploads/user456/aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
            )

            verify(exactly = 1) {
                presigner.presignPutObject(
                    match<PutObjectPresignRequest> { req ->
                        req.signatureDuration().equals(Duration.ofSeconds(expires.toLong())) &&
                            req.putObjectRequest().bucket() == uploadsBucketName &&
                            req.putObjectRequest().key() == "uploads/$userId/$generatedFileId"
                    },
                )
            }

            unmockkStatic(UUID::class)
        }

        "issuePresignedPutUrl: expires 指定なしの場合はデフォルト値を使用する" {
            val userId = "user789"
            val fileId = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb")

            val mockPresignedRequest = mockk<PresignedPutObjectRequest>()
            every {
                mockPresignedRequest.url()
            } returns URI.create(
                "https://s3.example.com/test-bucket/uploads/user789/bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb",
            ).toURL()

            every { presigner.presignPutObject(any<PutObjectPresignRequest>()) } returns mockPresignedRequest

            val before = OffsetDateTime.now()
            val result = service.issuePresignedPutUrl(userId, fileId, null)
            val after = OffsetDateTime.now().plusSeconds(defaultUrlExpirationSeconds.toLong())

            result.fileId shouldBe fileId
            result.url shouldBe URI.create(
                "https://s3.example.com/test-bucket/uploads/user789/bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb",
            )
            result.expireDateTime.isAfter(before.plusSeconds(defaultUrlExpirationSeconds.toLong() - 1)) shouldBe true
            result.expireDateTime.isBefore(after.plusSeconds(1)) shouldBe true

            verify(exactly = 1) {
                presigner.presignPutObject(
                    match<PutObjectPresignRequest> { req ->
                        req.signatureDuration().equals(
                            Duration.ofSeconds(defaultUrlExpirationSeconds.toLong()),
                        ) &&
                            req.putObjectRequest().bucket() == uploadsBucketName &&
                            req.putObjectRequest().key() == "uploads/$userId/$fileId"
                    },
                )
            }
        }

        "issuePresignedPutUrl: すべてのパラメータが null の場合はデフォルト値を使用する" {
            mockkStatic(UUID::class)
            val generatedFileId = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc")
            every { UUID.randomUUID() } returns generatedFileId

            val userId = "user000"

            val mockPresignedRequest = mockk<PresignedPutObjectRequest>()
            every {
                mockPresignedRequest.url()
            } returns URI.create(
                "https://s3.example.com/test-bucket/uploads/user000/cccccccc-cccc-cccc-cccc-cccccccccccc",
            ).toURL()

            every { presigner.presignPutObject(any<PutObjectPresignRequest>()) } returns mockPresignedRequest

            val result = service.issuePresignedPutUrl(userId, null, null)

            result.fileId shouldBe generatedFileId
            result.url shouldNotBe null

            verify(exactly = 1) {
                presigner.presignPutObject(
                    match<PutObjectPresignRequest> { req ->
                        req.signatureDuration().equals(
                            Duration.ofSeconds(defaultUrlExpirationSeconds.toLong()),
                        ) &&
                            req.putObjectRequest().bucket() == uploadsBucketName &&
                            req.putObjectRequest().key() == "uploads/$userId/$generatedFileId"
                    },
                )
            }

            unmockkStatic(UUID::class)
        }
    })
