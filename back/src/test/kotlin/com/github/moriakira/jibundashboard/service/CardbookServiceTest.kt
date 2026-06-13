package com.github.moriakira.jibundashboard.service

import com.github.moriakira.jibundashboard.repository.CardbookItem
import com.github.moriakira.jibundashboard.repository.CardbookRepository
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

class CardbookServiceTest :
    StringSpec({

        val repository = mockk<CardbookRepository>(relaxed = true)
        val service = CardbookService(repository)

        val fixedNow = OffsetDateTime.parse("2025-06-01T12:00:00Z")

        beforeTest {
            clearMocks(repository, answers = false, recordedCalls = true, childMocks = true)
        }

        fun item(id: String = "cb1", userId: String = "u1") =
            CardbookItem().apply {
                cardbookId = id
                this.userId = userId
                name = "My Cardbook"
                createdDateTime = "2025-01-01T00:00:00Z"
                updatedDateTime = "2025-01-01T00:00:00Z"
            }

        fun model(id: String = "cb1") =
            CardbookModel(
                cardbookId = id,
                userId = "u1",
                name = "My Cardbook",
            )

        "listByUser: 変換して返す" {
            every { repository.findByUser("u1") } returns listOf(item("cb1"), item("cb2"))

            val res = service.listByUser("u1")

            res.shouldHaveSize(2)
            res[0].cardbookId shouldBe "cb1"
            res[0].name shouldBe "My Cardbook"
        }

        "listByUser: updatedDateTime の降順でソートされる" {
            val older = item("cb1").also { it.updatedDateTime = "2025-01-01T00:00:00Z" }
            val newer = item("cb2").also { it.updatedDateTime = "2025-06-01T00:00:00Z" }
            every { repository.findByUser("u1") } returns listOf(older, newer)

            val res = service.listByUser("u1")

            res[0].cardbookId shouldBe "cb2"
            res[1].cardbookId shouldBe "cb1"
        }

        "getByCardbookIdForUser: 所有者なら返す、他ユーザなら null" {
            every { repository.getByUserAndCardbookId("u1", "cb1") } returns item(id = "cb1", userId = "u1")
            every { repository.getByUserAndCardbookId("u1", "missing") } returns null

            service.getByCardbookIdForUser("cb1", "u1")!!.cardbookId shouldBe "cb1"
            service.getByCardbookIdForUser("missing", "u1") shouldBe null
        }

        "put: 新規の場合は createdDateTime と updatedDateTime を設定する" {
            mockkStatic(OffsetDateTime::class)
            every { OffsetDateTime.now() } returns fixedNow
            every { repository.getByUserAndCardbookId("u1", "cb-new") } returns null
            val capt = slot<CardbookItem>()
            every { repository.put(capture(capt)) } returns Unit

            service.put(model(id = "cb-new"))

            capt.captured.createdDateTime shouldBe fixedNow.toString()
            capt.captured.updatedDateTime shouldBe fixedNow.toString()

            unmockkStatic(OffsetDateTime::class)
        }

        "put: 既存の場合は createdDateTime を保持し updatedDateTime を更新する" {
            mockkStatic(OffsetDateTime::class)
            every { OffsetDateTime.now() } returns fixedNow
            every { repository.getByUserAndCardbookId("u1", "cb-exist") } returns item(id = "cb-exist")
            val capt = slot<CardbookItem>()
            every { repository.put(capture(capt)) } returns Unit

            service.put(model(id = "cb-exist"))

            capt.captured.createdDateTime shouldBe "2025-01-01T00:00:00Z"
            capt.captured.updatedDateTime shouldBe fixedNow.toString()

            unmockkStatic(OffsetDateTime::class)
        }

        "create: UUID を採番して put する" {
            mockkStatic(UUID::class)
            mockkStatic(OffsetDateTime::class)
            val fixed = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb")
            every { UUID.randomUUID() } returns fixed
            every { OffsetDateTime.now() } returns fixedNow
            every { repository.getByUserAndCardbookId("u1", fixed.toString()) } returns null
            val capt = slot<CardbookItem>()
            every { repository.put(capture(capt)) } returns Unit

            val returnedId = service.create(model(id = ""))

            returnedId shouldBe fixed.toString()
            capt.captured.cardbookId shouldBe fixed.toString()

            unmockkStatic(UUID::class)
            unmockkStatic(OffsetDateTime::class)
        }

        "delete: 削除を委譲" {
            service.delete("u1", "cb-1")
            verify(exactly = 1) { repository.delete("u1", "cb-1") }
        }
    })
