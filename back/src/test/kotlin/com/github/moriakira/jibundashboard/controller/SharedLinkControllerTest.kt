package com.github.moriakira.jibundashboard.controller

import com.github.moriakira.jibundashboard.component.CurrentAuth
import com.github.moriakira.jibundashboard.generated.model.SharedLinkBase
import com.github.moriakira.jibundashboard.service.SharedLinkModel
import com.github.moriakira.jibundashboard.service.SharedLinkService
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.http.HttpStatus
import java.time.LocalDate
import java.util.UUID

class SharedLinkControllerTest :
    StringSpec({

        val currentAuth = mockk<CurrentAuth>(relaxed = true)
        val sharedLinkService = mockk<SharedLinkService>(relaxed = true)
        val controller = SharedLinkController(currentAuth, sharedLinkService)

        val token = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"

        beforeTest {
            every { currentAuth.userId } returns "u1"
            clearMocks(sharedLinkService, answers = false, recordedCalls = true)
        }

        fun model(dataTypes: List<String> = listOf("salary")) = SharedLinkModel(
            token = token,
            userId = "u1",
            dataTypes = dataTypes,
            expiresAt = "2026-12-31",
            shareUrl = "http://localhost:3333/share/$token",
        )

        "getSharedLinks: ユーザーのリンク一覧を返す" {
            every { sharedLinkService.listByUser("u1") } returns listOf(model())
            val res = controller.getSharedLinks()
            res.statusCode shouldBe HttpStatus.OK
            res.body!!.size shouldBe 1
            res.body!![0].token shouldBe UUID.fromString(token)
        }

        "postSharedLinks: リンクを作成して返す" {
            every {
                sharedLinkService.create("u1", listOf("salary"), "2026-12-31")
            } returns model()
            val req = SharedLinkBase(
                dataTypes = setOf(SharedLinkBase.DataTypes.salary),
                expiresAt = LocalDate.parse("2026-12-31"),
            )
            val res = controller.postSharedLinks(req)
            res.statusCode shouldBe HttpStatus.CREATED
            res.body!!.token shouldBe UUID.fromString(token)
        }

        "deleteSharedLinksById: リンクを削除する" {
            val res = controller.deleteSharedLinksById(UUID.fromString(token))
            res.statusCode shouldBe HttpStatus.NO_CONTENT
            verify { sharedLinkService.delete(token, "u1") }
        }
    })
