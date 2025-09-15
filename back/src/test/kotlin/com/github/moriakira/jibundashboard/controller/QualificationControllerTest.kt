package com.github.moriakira.jibundashboard.controller

import com.github.moriakira.jibundashboard.component.CurrentAuth
import com.github.moriakira.jibundashboard.generated.model.Qualification
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
        )

        "getQualification: 条件指定で一覧を返す" {
            every {
                qualificationService.listByConditions(
                    "u1",
                    "AWS SAA",
                    listOf("acquired"),
                    listOf("A"),
                    "2025-01-01",
                    "AWS",
                    "2025-12-31",
                    "2025-02-01",
                    "2025-11-01",
                )
            } returns listOf(model())

            val res = controller.getQualification(
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
            res.body!![0].status shouldBe Qualification.Status.acquired
            res.body!![0].rank shouldBe Qualification.Rank.A
            verify(exactly = 1) {
                qualificationService.listByConditions(
                    "u1",
                    "AWS SAA",
                    listOf("acquired"),
                    listOf("A"),
                    "2025-01-01",
                    "AWS",
                    "2025-12-31",
                    "2025-02-01",
                    "2025-11-01",
                )
            }
        }

        "putQualification: 新規作成で 201" {
            val req = Qualification(
                order = 1,
                qualificationName = "AWS SAA",
                status = Qualification.Status.acquired,
                rank = Qualification.Rank.A,
                organization = "AWS",
                officialUrl = URI.create("https://example.com/off"),
                qualificationId = null,
            )
            val id = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"
            every { qualificationService.put(any()) } returns id

            val res = controller.putQualification(req)

            res.statusCode shouldBe HttpStatus.CREATED
            res.body!!.qualificationId.toString() shouldBe id
        }

        "putQualification: 更新で 200 (所有者OK)" {
            val id = "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"
            every { qualificationService.getByQualificationId(id) } returns model(id = id, userId = "u1")
            every { qualificationService.put(any()) } returns id

            val req = Qualification(
                order = 1,
                qualificationName = "AWS SAA",
                status = Qualification.Status.acquired,
                rank = Qualification.Rank.A,
                organization = "AWS",
                officialUrl = URI.create("https://example.com/off"),
                qualificationId = UUID.fromString(id),
            )

            val res = controller.putQualification(req)

            res.statusCode shouldBe HttpStatus.OK
            res.body!!.qualificationId.toString() shouldBe id
            verify(exactly = 1) { qualificationService.getByQualificationId(id) }
            verify(exactly = 1) { qualificationService.put(any()) }
        }

        "putQualification: 更新対象が無ければ 404、put は呼ばれない" {
            val id = "cccccccc-cccc-cccc-cccc-cccccccccccc"
            every { qualificationService.getByQualificationId(id) } returns null

            val req = Qualification(
                order = 1,
                qualificationName = "AWS SAA",
                status = Qualification.Status.acquired,
                rank = Qualification.Rank.A,
                organization = "AWS",
                officialUrl = URI.create("https://example.com/off"),
                qualificationId = UUID.fromString(id),
            )

            val res = controller.putQualification(req)

            res.statusCode shouldBe HttpStatus.NOT_FOUND
            verify(exactly = 0) { qualificationService.put(any()) }
        }

        "putQualification: 他ユーザなら 404、put は呼ばれない" {
            val id = "dddddddd-dddd-dddd-dddd-dddddddddddd"
            every { qualificationService.getByQualificationId(id) } returns model(id = id, userId = "other")

            val req = Qualification(
                order = 1,
                qualificationName = "AWS SAA",
                status = Qualification.Status.acquired,
                rank = Qualification.Rank.A,
                organization = "AWS",
                officialUrl = URI.create("https://example.com/off"),
                qualificationId = UUID.fromString(id),
            )

            val res = controller.putQualification(req)

            res.statusCode shouldBe HttpStatus.NOT_FOUND
            verify(exactly = 0) { qualificationService.put(any()) }
        }

        "getQualificationById: 所有者なら 200" {
            val id = "eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee"
            every { qualificationService.getByQualificationId(id) } returns model(id = id, userId = "u1")

            val res = controller.getQualificationById(UUID.fromString(id))

            res.statusCode shouldBe HttpStatus.OK
            res.body!!.qualificationId.toString() shouldBe id
        }

        "getQualificationById: 見つからなければ 404" {
            val id = "ffffffff-ffff-ffff-ffff-ffffffffffff"
            every { qualificationService.getByQualificationId(id) } returns null

            val res = controller.getQualificationById(UUID.fromString(id))

            res.statusCode shouldBe HttpStatus.NOT_FOUND
        }

        "getQualificationById: 他ユーザなら 404" {
            val id = "99999999-9999-9999-9999-999999999999"
            every { qualificationService.getByQualificationId(id) } returns model(id = id, userId = "other")

            val res = controller.getQualificationById(UUID.fromString(id))

            res.statusCode shouldBe HttpStatus.NOT_FOUND
        }

        "deleteQualification: 見つからなければ 204 (no-op)" {
            val id = "12121212-1212-1212-1212-121212121212"
            every { qualificationService.getByQualificationId(id) } returns null

            val res = controller.deleteQualification(UUID.fromString(id))

            res.statusCode shouldBe HttpStatus.NO_CONTENT
            verify(exactly = 0) { qualificationService.deleteByQualificationId(any(), any()) }
        }

        "deleteQualification: 他ユーザなら 204 (no-op)" {
            val id = "13131313-1313-1313-1313-131313131313"
            every { qualificationService.getByQualificationId(id) } returns model(id = id, userId = "other")

            val res = controller.deleteQualification(UUID.fromString(id))

            res.statusCode shouldBe HttpStatus.NO_CONTENT
            verify(exactly = 0) { qualificationService.deleteByQualificationId(any(), any()) }
        }

        "deleteQualification: 所有者なら削除して 204" {
            val id = "14141414-1414-1414-1414-141414141414"
            every { qualificationService.getByQualificationId(id) } returns model(id = id, userId = "u1")

            val res = controller.deleteQualification(UUID.fromString(id))

            res.statusCode shouldBe HttpStatus.NO_CONTENT
            verify(exactly = 1) { qualificationService.deleteByQualificationId("u1", id) }
        }
    })
