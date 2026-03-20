package com.github.moriakira.jibundashboard.service

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.verify
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClientBuilder
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminGetUserRequest
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminGetUserResponse
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType
import software.amazon.awssdk.services.cognitoidentityprovider.model.ChangePasswordRequest
import software.amazon.awssdk.services.cognitoidentityprovider.model.UpdateUserAttributesRequest

class CognitoUserServiceTest :
    StringSpec({

        val service = CognitoUserService(
            userPoolId = "pool-id",
            cognitoRegion = "ap-northeast-1",
        )

        beforeTest { clearAllMocks() }

        "fetch: 正常にユーザ情報を返す" {
            mockkStatic(CognitoIdentityProviderClient::class)
            val builder = mockk<CognitoIdentityProviderClientBuilder>(relaxed = true)
            val client = mockk<CognitoIdentityProviderClient>(relaxed = true)
            val reqSlot = slot<AdminGetUserRequest>()

            every { CognitoIdentityProviderClient.builder() } returns builder
            every { builder.region(Region.of("ap-northeast-1")) } returns builder
            every { builder.build() } returns client
            every { client.adminGetUser(capture(reqSlot)) } returns AdminGetUserResponse.builder()
                .userAttributes(
                    AttributeType.builder().name("email").value("u@example.com").build(),
                )
                .build()

            val result = service.fetch("sub-1")

            verify(exactly = 1) { client.adminGetUser(reqSlot.captured) }
            reqSlot.captured.userPoolId() shouldBe "pool-id"
            reqSlot.captured.username() shouldBe "sub-1"
            result.sub shouldBe "sub-1"
            result.email shouldBe "u@example.com"
        }

        "fetch: emailが存在しない場合はnullを返す" {
            mockkStatic(CognitoIdentityProviderClient::class)
            val builder = mockk<CognitoIdentityProviderClientBuilder>(relaxed = true)
            val client = mockk<CognitoIdentityProviderClient>(relaxed = true)

            every { CognitoIdentityProviderClient.builder() } returns builder
            every { builder.region(Region.of("ap-northeast-1")) } returns builder
            every { builder.build() } returns client
            every { client.adminGetUser(any<AdminGetUserRequest>()) } returns AdminGetUserResponse.builder()
                .userAttributes(emptyList<AttributeType>())
                .build()

            val result = service.fetch("sub-empty")

            result.sub shouldBe "sub-empty"
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
