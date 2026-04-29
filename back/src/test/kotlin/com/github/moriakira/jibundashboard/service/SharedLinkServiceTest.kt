package com.github.moriakira.jibundashboard.service

import com.github.moriakira.jibundashboard.exception.ForbiddenException
import com.github.moriakira.jibundashboard.exception.GoneException
import com.github.moriakira.jibundashboard.repository.SharedLinkItem
import com.github.moriakira.jibundashboard.repository.SharedLinkRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.time.LocalDate

class SharedLinkServiceTest :
    StringSpec({

        val repository = mockk<SharedLinkRepository>(relaxed = true)
        val service = SharedLinkService(repository, "http://localhost:3333")

        val token = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"

        fun item(
            dataTypes: List<String> = listOf("salary"),
            expiresAt: String = LocalDate.now().plusDays(1).toString(),
            userId: String = "u1",
        ) = SharedLinkItem().also {
            it.token = token
            it.userId = userId
            it.dataTypes = dataTypes
            it.expiresAt = expiresAt
        }

        "create: DynamoDBに保存してモデルを返す" {
            val slot = slot<SharedLinkItem>()
            every { repository.put(capture(slot)) } returns Unit
            val model = service.create("u1", listOf("salary"), "2026-12-31")
            model.userId shouldBe "u1"
            model.dataTypes shouldBe listOf("salary")
            model.shareUrl shouldBe "http://localhost:3333/share/${model.token}"
            verify { repository.put(any()) }
        }

        "validateAndGet: 正常なトークンとdataTypeで成功" {
            every { repository.getByToken(token) } returns item()
            val model = service.validateAndGet(token, "salary")
            model.token shouldBe token
            model.userId shouldBe "u1"
        }

        "validateAndGet: トークンが存在しない場合にNoSuchElementExceptionをスロー" {
            every { repository.getByToken(token) } returns null
            shouldThrow<NoSuchElementException> { service.validateAndGet(token, "salary") }
        }

        "validateAndGet: 期限切れの場合にGoneExceptionをスロー" {
            every { repository.getByToken(token) } returns item(expiresAt = "2020-01-01")
            shouldThrow<GoneException> { service.validateAndGet(token, "salary") }
        }

        "validateAndGet: dataTypeが含まれない場合にForbiddenExceptionをスロー" {
            every { repository.getByToken(token) } returns item(dataTypes = listOf("qualification"))
            shouldThrow<ForbiddenException> { service.validateAndGet(token, "salary") }
        }

        "delete: 他ユーザーのトークンはForbiddenException" {
            every { repository.getByToken(token) } returns item(userId = "other")
            shouldThrow<ForbiddenException> { service.delete(token, "u1") }
        }
    })
