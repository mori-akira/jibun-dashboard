package com.github.moriakira.jibundashboard.controller

import com.github.moriakira.jibundashboard.component.CurrentAuth
import com.github.moriakira.jibundashboard.generated.model.QualificationBase
import com.github.moriakira.jibundashboard.service.QualificationModel
import com.github.moriakira.jibundashboard.service.QualificationService
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.http.HttpStatus
import java.net.URI
import java.time.LocalDate
import java.util.UUID

class QualificationControllerTest :
    StringSpec({

        val currentAuth = mockk<CurrentAuth>(relaxed = true)
        val qualificationService = mockk<QualificationService>(relaxed = true)
        val controller = QualificationController(currentAuth, qualificationService)

        beforeTest {
            every { currentAuth.userId } returns "u1"
            clearMocks(qualificationService, answers = false, recordedCalls = true, childMocks = true)
        }

        fun model(
            id: String = "11111111-1111-1111-1111-111111111111",
            userId: String = "u1",
            certificationAssetId: String? = null,
        ) = QualificationModel(
            qualificationId = id,
            userId = userId,
            order = 1,
            qualificationName = "AWS SAA",
            abbreviation = "SAA",
            version = "v1",
            status = "acquired",
            rank = "A",
            organization = "AWS",
            acquiredDate = "2025-01-01",
            expirationDate = "2028-01-01",
            officialUrl = "https://example.com/off",
            certificationUrl = "https://example.com/cert",
            badgeUrl = "https://example.com/badge",
            certificationAssetId = certificationAssetId,
        )

        fun req() = QualificationBase(
            order = 1,
            qualificationName = "AWS SAA",
            status = QualificationBase.Status.acquired,
            rank = QualificationBase.Rank.A,
            organization = "AWS",
            officialUrl = URI.create("https://example.com/off"),
            abbreviation = null,
            version = null,
            acquiredDate = null,
            expirationDate = null,
            certificationUrl = null,
            badgeUrl = null,
        )

        "getQualification: 条件指定で一覧を返す" {
            every {
                qualificationService.listByConditions(
                    "u1", "AWS SAA", listOf("acquired"), listOf("A"),
                    "2025-01-01", "AWS", "2025-12-31", "2025-02-01", "2025-11-01",
                )
            } returns listOf(model())

            val res = controller.getQualifications(
                qualificationName = "AWS SAA",
                status = listOf("acquired"),
                rank = listOf("A"),
                organization = "AWS",
                acquiredDateFrom = LocalDate.parse("2025-01-01"),
                acquiredDateTo = LocalDate.parse("2025-12-31"),
                expirationDateFrom = LocalDate.parse("2025-02-01"),
                expirationDateTo = LocalDate.parse("2025-11-01"),
            )

            res.statusCode shouldBe HttpStatus.OK
            res.body!!.shouldHaveSize(1)
            res.body!![0].qualificationName shouldBe "AWS SAA"
        }

        "postQualification: 新規作成で 200" {
            every { qualificationService.create(any()) } returns "11111111-1111-1111-1111-111111111111"

            val res = controller.postQualifications(req())

            res.statusCode shouldBe HttpStatus.OK
            res.body!!.qualificationId.toString() shouldBe "11111111-1111-1111-1111-111111111111"
            verify(exactly = 0) { qualificationService.put(any()) }
        }

        "putQualification: 200 または 404" {
            val id = "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"
            val missingId = "cccccccc-cccc-cccc-cccc-cccccccccccc"
            every { qualificationService.getByQualificationIdForUser(id, "u1") } returns model(id = id)
            every { qualificationService.getByQualificationIdForUser(missingId, "u1") } returns null
            every { qualificationService.put(any()) } returns id

            controller.putQualificationsById(UUID.fromString(id), req()).statusCode shouldBe HttpStatus.OK
            controller.putQualificationsById(UUID.fromString(missingId), req()).also {
                it.statusCode shouldBe HttpStatus.NOT_FOUND
                verify(exactly = 0) { qualificationService.put(any()) }
            }
        }

        "getQualificationById: 200 または 404" {
            val id = "eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee"
            val missingId = "ffffffff-ffff-ffff-ffff-ffffffffffff"
            every { qualificationService.getByQualificationIdForUser(id, "u1") } returns model(id = id)
            every { qualificationService.getByQualificationIdForUser(missingId, "u1") } returns null

            controller.getQualificationsById(UUID.fromString(id)).statusCode shouldBe HttpStatus.OK
            controller.getQualificationsById(UUID.fromString(missingId)).statusCode shouldBe HttpStatus.NOT_FOUND
        }

        "deleteQualification: 204 または 404" {
            val id = "14141414-1414-1414-1414-141414141414"
            val missingId = "12121212-1212-1212-1212-121212121212"
            every { qualificationService.getByQualificationIdForUser(id, "u1") } returns model(id = id)
            every { qualificationService.getByQualificationIdForUser(missingId, "u1") } returns null

            controller.deleteQualificationsById(UUID.fromString(id)).statusCode shouldBe HttpStatus.NO_CONTENT
            controller.deleteQualificationsById(UUID.fromString(missingId)).statusCode shouldBe HttpStatus.NOT_FOUND
        }
    })
