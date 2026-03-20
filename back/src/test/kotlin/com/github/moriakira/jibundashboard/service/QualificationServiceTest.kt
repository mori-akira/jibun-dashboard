package com.github.moriakira.jibundashboard.service

import com.github.moriakira.jibundashboard.repository.QualificationItem
import com.github.moriakira.jibundashboard.repository.QualificationRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import io.mockk.verify
import java.util.UUID

class QualificationServiceTest :
    StringSpec({

        val repository = mockk<QualificationRepository>(relaxed = true)
        val userAssetService = mockk<UserAssetService>(relaxed = true)
        val service = QualificationService(repository, userAssetService)

        beforeTest {
            clearMocks(repository, answers = false, recordedCalls = true, childMocks = true)
            clearMocks(userAssetService, answers = false, recordedCalls = true, childMocks = true)
        }

        fun item(
            id: String = "q1",
            userId: String = "u1",
            order: Int = 1,
            withOptionals: Boolean = true,
            certificationAssetId: String? = null,
        ) = QualificationItem().apply {
            qualificationId = id
            this.userId = userId
            this.order = order
            qualificationName = "AWS SAA"
            abbreviation = if (withOptionals) "SAA" else null
            version = if (withOptionals) "v1" else null
            status = "acquired"
            rank = "A"
            organization = "AWS"
            acquiredDate = if (withOptionals) "2025-01-01" else null
            expirationDate = if (withOptionals) "2028-01-01" else null
            officialUrl = "https://example.com/off"
            certificationUrl = if (withOptionals) "https://example.com/cert" else null
            badgeUrl = if (withOptionals) "https://example.com/badge" else null
            this.certificationAssetId = certificationAssetId
        }

        "listAll: 変換して返す" {
            every { repository.query("u1") } returns listOf(item(), item(id = "q2", order = 2))

            val res = service.listAll("u1")

            res.shouldHaveSize(2)
            res[0].qualificationId shouldBe "q1"
            res[0].userId shouldBe "u1"
            res[0].order shouldBe 1
            res[0].qualificationName shouldBe "AWS SAA"
            res[0].status shouldBe "acquired"
            res[0].rank shouldBe "A"
            res[0].officialUrl shouldBe "https://example.com/off"
            res[0].certificationUrl shouldBe "https://example.com/cert"
            res[0].badgeUrl shouldBe "https://example.com/badge"
        }

        "listByConditions: 条件を委譲" {
            every {
                repository.query(
                    userId = "u9",
                    qualificationName = "AWS",
                    statuses = listOf("acquired"),
                    ranks = listOf("A"),
                    acquiredDateFrom = "2025-01-01",
                    organization = "AWS",
                    acquiredDateTo = "2025-12-31",
                    expirationDateFrom = "2025-02-01",
                    expirationDateTo = "2025-11-01",
                )
            } returns listOf(item(id = "q9", userId = "u9"))

            val res = service.listByConditions(
                userId = "u9",
                qualificationName = "AWS",
                statuses = listOf("acquired"),
                ranks = listOf("A"),
                acquiredDateFrom = "2025-01-01",
                organization = "AWS",
                acquiredDateTo = "2025-12-31",
                expirationDateFrom = "2025-02-01",
                expirationDateTo = "2025-11-01",
            )

            res.shouldHaveSize(1)
            res[0].qualificationId shouldBe "q9"
            verify(exactly = 1) {
                repository.query(
                    userId = "u9",
                    qualificationName = "AWS",
                    statuses = listOf("acquired"),
                    ranks = listOf("A"),
                    acquiredDateFrom = "2025-01-01",
                    organization = "AWS",
                    acquiredDateTo = "2025-12-31",
                    expirationDateFrom = "2025-02-01",
                    expirationDateTo = "2025-11-01",
                )
            }
        }

        "getByQualificationId: 存在すれば返す、無ければ null" {
            every { repository.getByQualificationId("qid-1") } returns item(id = "qid-1")
            every { repository.getByQualificationId("nope") } returns null

            service.getByQualificationId("qid-1")!!.qualificationId shouldBe "qid-1"
            service.getByQualificationId("nope") shouldBe null
        }

        "put: モデルを保存して ID を返す" {
            val capt = slot<QualificationItem>()
            every { repository.put(capture(capt)) } returns Unit

            val model = QualificationModel(
                qualificationId = "qid-7",
                userId = "u7",
                order = 7,
                qualificationName = "GCP ACE",
                abbreviation = "ACE",
                version = "v2",
                status = "planning",
                rank = "B",
                organization = "GCP",
                acquiredDate = null,
                expirationDate = null,
                officialUrl = "https://example.com/gcp",
                certificationUrl = null,
                badgeUrl = null,
                certificationAssetId = null,
            )

            val ret = service.put(model)

            ret shouldBe "qid-7"
            capt.captured.qualificationId shouldBe "qid-7"
            capt.captured.userId shouldBe "u7"
            capt.captured.order shouldBe 7
            capt.captured.qualificationName shouldBe "GCP ACE"
            capt.captured.status shouldBe "planning"
            capt.captured.rank shouldBe "B"
            capt.captured.officialUrl shouldBe "https://example.com/gcp"
        }

        fun model(
            id: String = "qid-s3",
            certificationAssetId: String? = null,
        ) = QualificationModel(
            qualificationId = id,
            userId = "u1",
            order = 1,
            qualificationName = "AWS SAA",
            abbreviation = null,
            version = null,
            status = "acquired",
            rank = "A",
            organization = "AWS",
            acquiredDate = null,
            expirationDate = null,
            officialUrl = "https://example.com/off",
            certificationUrl = null,
            badgeUrl = null,
            certificationAssetId = certificationAssetId,
        )

        "put: certificationAssetId 新規設定時は uploads からコピーする" {
            val assetId = "33333333-3333-3333-3333-333333333333"
            every { repository.getByQualificationId("qid-new") } returns null

            service.put(model(id = "qid-new", certificationAssetId = assetId))

            verify(exactly = 1) {
                userAssetService.copyFromUploads(
                    "qualification-certifications",
                    "u1",
                    UUID.fromString(assetId),
                )
            }
            verify(exactly = 0) { userAssetService.delete(any(), any(), any()) }
        }

        "put: certificationAssetId が変わった場合は新しい ID でコピーする" {
            val oldAssetId = "44444444-4444-4444-4444-444444444444"
            val newAssetId = "55555555-5555-5555-5555-555555555555"
            every { repository.getByQualificationId("qid-upd") } returns
                item(id = "qid-upd", certificationAssetId = oldAssetId)

            service.put(model(id = "qid-upd", certificationAssetId = newAssetId))

            verify(exactly = 1) {
                userAssetService.copyFromUploads(
                    "qualification-certifications",
                    "u1",
                    UUID.fromString(newAssetId),
                )
            }
            verify(exactly = 0) { userAssetService.delete(any(), any(), any()) }
        }

        "put: certificationAssetId が変わらない場合は S3 処理をしない" {
            val assetId = "66666666-6666-6666-6666-666666666666"
            every { repository.getByQualificationId("qid-same") } returns
                item(id = "qid-same", certificationAssetId = assetId)

            service.put(model(id = "qid-same", certificationAssetId = assetId))

            verify(exactly = 0) { userAssetService.copyFromUploads(any(), any(), any()) }
            verify(exactly = 0) { userAssetService.delete(any(), any(), any()) }
        }

        "put: certificationAssetId が削除された場合は user-assets から削除する" {
            val oldAssetId = "77777777-7777-7777-7777-777777777777"
            every { repository.getByQualificationId("qid-rm") } returns
                item(id = "qid-rm", certificationAssetId = oldAssetId)

            service.put(model(id = "qid-rm", certificationAssetId = null))

            verify(exactly = 0) { userAssetService.copyFromUploads(any(), any(), any()) }
            verify(exactly = 1) {
                userAssetService.delete(
                    "qualification-certifications",
                    "u1",
                    UUID.fromString(oldAssetId),
                )
            }
        }

        "put: certificationAssetId が元々なく引き続き null の場合は S3 処理をしない" {
            every { repository.getByQualificationId("qid-null") } returns item(id = "qid-null")

            service.put(model(id = "qid-null", certificationAssetId = null))

            verify(exactly = 0) { userAssetService.copyFromUploads(any(), any(), any()) }
            verify(exactly = 0) { userAssetService.delete(any(), any(), any()) }
        }

        "deleteByQualificationId: 削除を委譲" {
            service.delete("u1", "qid-1")
            verify(exactly = 1) { repository.delete("u1", "qid-1") }
        }

        "getByQualificationIdForUser: 所有者なら返す" {
            every { repository.getByQualificationId("qid-1") } returns item(id = "qid-1", userId = "u1")

            val result = service.getByQualificationIdForUser("qid-1", "u1")

            result!!.qualificationId shouldBe "qid-1"
            result.userId shouldBe "u1"
        }

        "getByQualificationIdForUser: 他ユーザなら null" {
            every { repository.getByQualificationId("qid-2") } returns item(id = "qid-2", userId = "other")

            val result = service.getByQualificationIdForUser("qid-2", "u1")

            result shouldBe null
        }

        "getByQualificationIdForUser: 存在しなければ null" {
            every { repository.getByQualificationId("nope") } returns null

            val result = service.getByQualificationIdForUser("nope", "u1")

            result shouldBe null
        }

        "create: UUID を採番して put する" {
            mockkStatic(UUID::class)
            val fixed = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
            every { UUID.randomUUID() } returns fixed
            val capt = slot<QualificationItem>()
            every { repository.put(capture(capt)) } returns Unit
            every { repository.getByQualificationId(fixed.toString()) } returns null

            val inputModel = model(id = "", certificationAssetId = null)
            val returnedId = service.create(inputModel)

            returnedId shouldBe fixed.toString()
            capt.captured.qualificationId shouldBe fixed.toString()

            unmockkStatic(UUID::class)
        }
    })
