package com.github.moriakira.jibundashboard.controller

import com.github.moriakira.jibundashboard.component.CurrentAuth
import com.github.moriakira.jibundashboard.service.FileUploadService
import com.github.moriakira.jibundashboard.service.PresignedUrlResult
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.http.HttpStatus
import java.net.URI
import java.time.OffsetDateTime
import java.util.UUID

class FileControllerTest :
    StringSpec({

        val currentAuth = mockk<CurrentAuth>(relaxed = true)
        val fileUploadService = mockk<FileUploadService>(relaxed = true)
        val controller = FileController(currentAuth, fileUploadService)

        beforeTest {
            every { currentAuth.userId } returns "user123"
        }

        "getUploadUrl: fileId と expiresIn 指定ありの場合は指定された値を使用する" {
            val fileId = UUID.fromString("12345678-1234-1234-1234-123456789abc")
            val expiresIn = 7200
            val expectedUrl = URI.create("https://s3.example.com/test-bucket/uploads/user123/$fileId")
            val expectedExpireDateTime = OffsetDateTime.now().plusSeconds(expiresIn.toLong())

            every {
                fileUploadService.issuePresignedPutUrl("user123", fileId, expiresIn)
            } returns PresignedUrlResult(
                fileId = fileId,
                url = expectedUrl,
                expireDateTime = expectedExpireDateTime,
            )

            val res = controller.getUploadUrl(fileId, expiresIn)

            res.statusCode shouldBe HttpStatus.OK
            res.body!!.fileId shouldBe fileId
            res.body!!.uploadUrl shouldBe expectedUrl
            res.body!!.expireDateTime shouldBe expectedExpireDateTime
            verify(exactly = 1) {
                fileUploadService.issuePresignedPutUrl("user123", fileId, expiresIn)
            }
        }

        "getUploadUrl: fileId 指定なしの場合は自動生成される" {
            val generatedFileId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
            val expiresIn = 3600
            val expectedUrl = URI.create("https://s3.example.com/test-bucket/uploads/user123/$generatedFileId")
            val expectedExpireDateTime = OffsetDateTime.now().plusSeconds(expiresIn.toLong())

            every {
                fileUploadService.issuePresignedPutUrl("user123", null, expiresIn)
            } returns PresignedUrlResult(
                fileId = generatedFileId,
                url = expectedUrl,
                expireDateTime = expectedExpireDateTime,
            )

            val res = controller.getUploadUrl(null, expiresIn)

            res.statusCode shouldBe HttpStatus.OK
            res.body!!.fileId shouldBe generatedFileId
            res.body!!.uploadUrl shouldBe expectedUrl
            res.body!!.expireDateTime shouldBe expectedExpireDateTime
            verify(exactly = 1) {
                fileUploadService.issuePresignedPutUrl("user123", null, expiresIn)
            }
        }

        "getUploadUrl: expiresIn 指定なしの場合はデフォルト値が使用される" {
            val fileId = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb")
            val expectedUrl = URI.create("https://s3.example.com/test-bucket/uploads/user123/$fileId")
            val expectedExpireDateTime = OffsetDateTime.now().plusSeconds(3600)

            every {
                fileUploadService.issuePresignedPutUrl("user123", fileId, null)
            } returns PresignedUrlResult(
                fileId = fileId,
                url = expectedUrl,
                expireDateTime = expectedExpireDateTime,
            )

            val res = controller.getUploadUrl(fileId, null)

            res.statusCode shouldBe HttpStatus.OK
            res.body!!.fileId shouldBe fileId
            res.body!!.uploadUrl shouldBe expectedUrl
            res.body!!.expireDateTime shouldBe expectedExpireDateTime
            verify(exactly = 1) {
                fileUploadService.issuePresignedPutUrl("user123", fileId, null)
            }
        }

        "getUploadUrl: すべてのパラメータが null の場合はデフォルト値が使用される" {
            val generatedFileId = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc")
            val expectedUrl = URI.create("https://s3.example.com/test-bucket/uploads/user123/$generatedFileId")
            val expectedExpireDateTime = OffsetDateTime.now().plusSeconds(3600)

            every {
                fileUploadService.issuePresignedPutUrl("user123", null, null)
            } returns PresignedUrlResult(
                fileId = generatedFileId,
                url = expectedUrl,
                expireDateTime = expectedExpireDateTime,
            )

            val res = controller.getUploadUrl(null, null)

            res.statusCode shouldBe HttpStatus.OK
            res.body!!.fileId shouldBe generatedFileId
            res.body!!.uploadUrl shouldBe expectedUrl
            res.body!!.expireDateTime shouldBe expectedExpireDateTime
            verify(exactly = 1) {
                fileUploadService.issuePresignedPutUrl("user123", null, null)
            }
        }

        "getUploadUrl: currentAuth.userId がサービスに正しく渡される" {
            val fileId = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd")
            val expiresIn = 1800
            val expectedUrl = URI.create("https://s3.example.com/test-bucket/uploads/user123/$fileId")
            val expectedExpireDateTime = OffsetDateTime.now().plusSeconds(expiresIn.toLong())

            every { currentAuth.userId } returns "user123"
            every {
                fileUploadService.issuePresignedPutUrl("user123", fileId, expiresIn)
            } returns PresignedUrlResult(
                fileId = fileId,
                url = expectedUrl,
                expireDateTime = expectedExpireDateTime,
            )

            val res = controller.getUploadUrl(fileId, expiresIn)

            res.statusCode shouldBe HttpStatus.OK
            verify(exactly = 1) {
                fileUploadService.issuePresignedPutUrl("user123", fileId, expiresIn)
            }
        }
    })
