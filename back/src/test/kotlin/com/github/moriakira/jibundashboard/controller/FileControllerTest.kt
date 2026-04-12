package com.github.moriakira.jibundashboard.controller

import com.github.moriakira.jibundashboard.component.CurrentAuth
import com.github.moriakira.jibundashboard.generated.model.PostUploadUrlRequest
import com.github.moriakira.jibundashboard.service.FileUploadService
import com.github.moriakira.jibundashboard.service.PresignedDownloadUrlResult
import com.github.moriakira.jibundashboard.service.PresignedUrlResult
import com.github.moriakira.jibundashboard.service.UserAssetService
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.springframework.http.HttpStatus
import java.net.URI
import java.time.OffsetDateTime
import java.util.UUID

class FileControllerTest :
    StringSpec({

        val currentAuth = mockk<CurrentAuth>(relaxed = true)
        val fileUploadService = mockk<FileUploadService>(relaxed = true)
        val userAssetService = mockk<UserAssetService>(relaxed = true)
        val controller = FileController(currentAuth, fileUploadService, userAssetService)

        beforeTest {
            every { currentAuth.userId } returns "user123"
        }

        "postUploadUrl: fileId と expiresIn 指定ありの場合は指定された値を使用する" {
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

            val res = controller.postUploadUrl(PostUploadUrlRequest(fileId = fileId, expiresIn = expiresIn))

            res.statusCode shouldBe HttpStatus.OK
            res.body!!.fileId shouldBe fileId
            res.body!!.uploadUrl shouldBe expectedUrl
            res.body!!.expireDateTime shouldBe expectedExpireDateTime
        }

        "postUploadUrl: すべてのパラメータが null の場合はデフォルト値が使用される" {
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

            val res = controller.postUploadUrl(null)

            res.statusCode shouldBe HttpStatus.OK
            res.body!!.fileId shouldBe generatedFileId
            res.body!!.uploadUrl shouldBe expectedUrl
            res.body!!.expireDateTime shouldBe expectedExpireDateTime
        }

        "getUserAssetsDownloadUrl: assetType と assetId を指定した場合は正しく DownloadUrl を返す" {
            val assetType = "qualification-certificate"
            val assetId = UUID.fromString("11111111-1111-1111-1111-111111111111")
            val expectedUrl = URI.create("https://s3.example.com/user-assets/$assetType/user123/$assetId")
            val expectedExpireDateTime = OffsetDateTime.now().plusSeconds(3600)

            every {
                userAssetService.issuePresignedGetUrl(assetType, "user123", assetId, null)
            } returns PresignedDownloadUrlResult(
                url = expectedUrl,
                expireDateTime = expectedExpireDateTime,
            )

            val res = controller.getUserAssetsDownloadUrl(assetType, assetId)

            res.statusCode shouldBe HttpStatus.OK
            res.body!!.downloadUrl shouldBe expectedUrl
            res.body!!.expireDateTime shouldBe expectedExpireDateTime
        }
    })
