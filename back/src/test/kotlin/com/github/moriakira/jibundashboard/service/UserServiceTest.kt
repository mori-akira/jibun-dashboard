package com.github.moriakira.jibundashboard.service

import com.github.moriakira.jibundashboard.repository.UserItem
import com.github.moriakira.jibundashboard.repository.UserRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import io.mockk.verify
import java.util.UUID

class UserServiceTest :
    StringSpec({

        val repository = mockk<UserRepository>(relaxed = true)
        val service = UserService(repository)

        beforeTest {
            clearMocks(repository, answers = false, recordedCalls = true, childMocks = true)
        }

        "getUser: 存在する場合は UserModel を返す" {
            val item = UserItem().apply {
                userId = "u1"
                userName = "Alice"
                emailAddress = "alice@example.com"
            }
            every { repository.get("u1") } returns item

            val result = service.get("u1")

            result shouldBe UserModel(
                userId = "u1",
                userName = "Alice",
                emailAddress = "alice@example.com",
            )
            verify(exactly = 1) { repository.get("u1") }
        }

        "getUser: 見つからない場合は null" {
            every { repository.get("nope") } returns null

            val result = service.get("nope")

            result shouldBe null
            verify(exactly = 1) { repository.get("nope") }
        }

        "putUser: userId 指定ありなら同じ ID で保存" {
            val slotItem = slot<UserItem>()
            every { repository.put(capture(slotItem)) } returns Unit

            val input = UserModel(
                userId = "u2",
                userName = "Bob",
                emailAddress = "bob@example.com",
            )

            val result = service.put(input)

            result shouldBe UserModel(
                userId = "u2",
                userName = "Bob",
                emailAddress = "bob@example.com",
            )
            slotItem.captured.userId shouldBe "u2"
            slotItem.captured.userName shouldBe "Bob"
            slotItem.captured.emailAddress shouldBe "bob@example.com"
            verify(exactly = 1) { repository.put(any()) }
        }

        "putUser: userId なしなら UUID を採番して保存" {
            mockkStatic(UUID::class)
            val fixed = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
            every { UUID.randomUUID() } returns fixed
            val slotItem = slot<UserItem>()
            every { repository.put(capture(slotItem)) } returns Unit

            val input = UserModel(
                userId = null,
                userName = "Carol",
                emailAddress = "carol@example.com",
            )

            val result = service.put(input)

            result shouldBe UserModel(
                userId = fixed.toString(),
                userName = "Carol",
                emailAddress = "carol@example.com",
            )
            slotItem.captured.userId shouldBe fixed.toString()
            slotItem.captured.userName shouldBe "Carol"
            slotItem.captured.emailAddress shouldBe "carol@example.com"
            verify(exactly = 1) { repository.put(any()) }

            unmockkStatic(UUID::class)
        }
    })
