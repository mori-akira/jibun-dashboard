package com.github.moriakira.jibundashboard.service

import com.github.moriakira.jibundashboard.repository.CardbookCardItem
import com.github.moriakira.jibundashboard.repository.CardbookCardRepository
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
import java.time.OffsetDateTime
import java.util.UUID

class CardbookCardServiceTest :
    StringSpec({

        val repository = mockk<CardbookCardRepository>(relaxed = true)
        val service = CardbookCardService(repository)

        val fixedNow = OffsetDateTime.parse("2025-06-01T12:00:00Z")

        beforeTest {
            clearMocks(repository, answers = false, recordedCalls = true, childMocks = true)
        }

        fun item(
            id: String = "card1",
            cardbookId: String = "cb1",
            userId: String = "u1",
            createdDateTime: String = "2025-01-01T00:00:00Z",
        ) = CardbookCardItem().apply {
            cardId = id
            this.cardbookId = cardbookId
            this.userId = userId
            front = "Front text"
            back = "Back text"
            this.createdDateTime = createdDateTime
        }

        fun model(id: String = "card1") =
            CardbookCardModel(
                cardId = id,
                cardbookId = "cb1",
                userId = "u1",
                front = "Front text",
                back = "Back text",
            )

        "listByCardbookId: createdDateTime の昇順でソートされる" {
            val older = item("card1", createdDateTime = "2025-01-01T00:00:00Z")
            val newer = item("card2", createdDateTime = "2025-06-01T00:00:00Z")
            every { repository.findByCardbookId("cb1") } returns listOf(newer, older)

            val res = service.listByCardbookId("cb1")

            res.shouldHaveSize(2)
            res[0].cardId shouldBe "card1"
            res[1].cardId shouldBe "card2"
        }

        "getByCardIdForUser: 存在すれば返す、存在しなければ null" {
            every { repository.getByUserAndCardId("u1", "card1") } returns item(id = "card1")
            every { repository.getByUserAndCardId("u1", "missing") } returns null

            service.getByCardIdForUser("card1", "u1")!!.cardId shouldBe "card1"
            service.getByCardIdForUser("missing", "u1") shouldBe null
        }

        "create: UUID と createdDateTime を採番して保存する" {
            mockkStatic(UUID::class)
            mockkStatic(OffsetDateTime::class)
            val fixed = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc")
            every { UUID.randomUUID() } returns fixed
            every { OffsetDateTime.now() } returns fixedNow
            val capt = slot<CardbookCardItem>()
            every { repository.put(capture(capt)) } returns Unit

            val returnedId = service.create(model(id = ""))

            returnedId shouldBe fixed.toString()
            capt.captured.cardId shouldBe fixed.toString()
            capt.captured.createdDateTime shouldBe fixedNow.toString()
            capt.captured.updatedDateTime shouldBe fixedNow.toString()

            unmockkStatic(UUID::class)
            unmockkStatic(OffsetDateTime::class)
        }

        "put: 既存の場合は createdDateTime を保持し updatedDateTime を now で更新する" {
            mockkStatic(OffsetDateTime::class)
            every { OffsetDateTime.now() } returns fixedNow
            every { repository.getByUserAndCardId("u1", "card-exist") } returns item(id = "card-exist")
            val capt = slot<CardbookCardItem>()
            every { repository.put(capture(capt)) } returns Unit

            service.put(model(id = "card-exist"))

            capt.captured.createdDateTime shouldBe "2025-01-01T00:00:00Z"
            capt.captured.updatedDateTime shouldBe fixedNow.toString()

            unmockkStatic(OffsetDateTime::class)
        }

        "toDomain: updatedDateTime が無い既存レコードは createdDateTime にフォールバックする" {
            every { repository.getByUserAndCardId("u1", "legacy") } returns
                item(id = "legacy").apply { updatedDateTime = null }

            val res = service.getByCardIdForUser("legacy", "u1")!!

            res.updatedDateTime shouldBe "2025-01-01T00:00:00Z"
        }

        "put: 新規の場合は createdDateTime を now で設定する" {
            mockkStatic(OffsetDateTime::class)
            every { OffsetDateTime.now() } returns fixedNow
            every { repository.getByUserAndCardId("u1", "card-new") } returns null
            val capt = slot<CardbookCardItem>()
            every { repository.put(capture(capt)) } returns Unit

            service.put(model(id = "card-new"))

            capt.captured.createdDateTime shouldBe fixedNow.toString()

            unmockkStatic(OffsetDateTime::class)
        }

        "delete: 削除を委譲" {
            service.delete("u1", "card-1")
            verify(exactly = 1) { repository.delete("u1", "card-1") }
        }
    })
