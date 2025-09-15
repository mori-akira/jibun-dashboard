package com.github.moriakira.jibundashboard.service

import com.github.moriakira.jibundashboard.repository.QualificationItem
import com.github.moriakira.jibundashboard.repository.QualificationRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify

class QualificationServiceTest :
    StringSpec({

        val repository = mockk<QualificationRepository>(relaxed = true)
        val service = QualificationService(repository)

        beforeTest {
            clearMocks(repository, answers = false, recordedCalls = true, childMocks = true)
        }

        fun item(
            id: String = "q1",
            userId: String = "u1",
            order: Int = 1,
            withOptionals: Boolean = true,
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

        "deleteByQualificationId: 削除を委譲" {
            service.deleteByQualificationId("u1", "qid-1")
            verify(exactly = 1) { repository.delete("u1", "qid-1") }
        }
    })
