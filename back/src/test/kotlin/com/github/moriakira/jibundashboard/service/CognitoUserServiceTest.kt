package com.github.moriakira.jibundashboard.service

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.verify
import org.springframework.http.HttpHeaders
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClientBuilder
import software.amazon.awssdk.services.cognitoidentityprovider.model.ChangePasswordRequest
import software.amazon.awssdk.services.cognitoidentityprovider.model.UpdateUserAttributesRequest

class CognitoUserServiceTest :
    StringSpec({

        val webClient = mockk<WebClient>()
        val service = CognitoUserService(
            webClient = webClient,
            baseUri = "https://idp.example.com",
            cognitoRegion = "ap-northeast-1",
        )

        beforeTest { clearAllMocks() }

        "fetch: 正常にユーザ情報を返す" {
            val reqUriSpec = mockk<WebClient.RequestHeadersUriSpec<*>>()
            val resSpec = mockk<WebClient.ResponseSpec>()

            every { webClient.get() } returns reqUriSpec
            every { reqUriSpec.uri("https://idp.example.com/oauth2/userInfo") } returns reqUriSpec
            every { reqUriSpec.header(HttpHeaders.AUTHORIZATION, "Bearer token-xyz") } returns reqUriSpec
            every { reqUriSpec.retrieve() } returns resSpec
            every { resSpec.bodyToMono(CognitoUserInfo::class.java) } returns Mono.just(
                CognitoUserInfo(
                    sub = "sub-1",
                    email = "u@example.com",
                ),
            )

            val result = service.fetch("token-xyz")

            result.sub shouldBe "sub-1"
            result.email shouldBe "u@example.com"
        }

        "fetch: レスポンスが空ならデフォルト値を返す" {
            val reqUriSpec = mockk<WebClient.RequestHeadersUriSpec<*>>()
            val resSpec = mockk<WebClient.ResponseSpec>()

            every { webClient.get() } returns reqUriSpec
            every { reqUriSpec.uri("https://idp.example.com/oauth2/userInfo") } returns reqUriSpec
            every { reqUriSpec.header(HttpHeaders.AUTHORIZATION, "Bearer token-empty") } returns reqUriSpec
            every { reqUriSpec.retrieve() } returns resSpec
            every { resSpec.bodyToMono(CognitoUserInfo::class.java) } returns Mono.empty()

            val result = service.fetch("token-empty")

            result.sub shouldBe null
            result.email shouldBe null
        }

        "updateEmail: AWS SDK へ正しい引数で委譲する" {
            // builder() の static をモック
            mockkStatic(CognitoIdentityProviderClient::class)
            val builder = mockk<CognitoIdentityProviderClientBuilder>(relaxed = true)
            val client = mockk<CognitoIdentityProviderClient>(relaxed = true)
            val reqSlot = slot<UpdateUserAttributesRequest>()

            every { CognitoIdentityProviderClient.builder() } returns builder
            every { builder.region(Region.of("ap-northeast-1")) } returns builder
            every { builder.build() } returns client
            every { client.updateUserAttributes(capture(reqSlot)) } returns mockk(relaxed = true)

            service.updateEmail("at-123", "new-email@example.com")

            verify(exactly = 1) { client.updateUserAttributes(reqSlot.captured) }
            reqSlot.captured.accessToken() shouldBe "at-123"
            reqSlot.captured.userAttributes().size shouldBe 1
            reqSlot.captured.userAttributes()[0].name() shouldBe "email"
            reqSlot.captured.userAttributes()[0].value() shouldBe "new-email@example.com"
        }

        "changePassword: AWS SDK へ正しい引数で委譲する" {
            // builder() の static をモック
            mockkStatic(CognitoIdentityProviderClient::class)
            val builder = mockk<CognitoIdentityProviderClientBuilder>(relaxed = true)
            val client = mockk<CognitoIdentityProviderClient>(relaxed = true)
            val reqSlot = slot<ChangePasswordRequest>()

            every { CognitoIdentityProviderClient.builder() } returns builder
            every { builder.region(Region.of("ap-northeast-1")) } returns builder
            every { builder.build() } returns client
            every { client.changePassword(capture(reqSlot)) } returns mockk(relaxed = true)

            service.changePassword("at-123", "old-1", "new-1")

            verify(exactly = 1) { client.changePassword(reqSlot.captured) }
            reqSlot.captured.accessToken() shouldBe "at-123"
            reqSlot.captured.previousPassword() shouldBe "old-1"
            reqSlot.captured.proposedPassword() shouldBe "new-1"
        }
    })
