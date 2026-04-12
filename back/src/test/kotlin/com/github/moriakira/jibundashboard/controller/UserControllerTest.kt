package com.github.moriakira.jibundashboard.controller

import com.github.moriakira.jibundashboard.component.CurrentAuth
import com.github.moriakira.jibundashboard.generated.model.Password
import com.github.moriakira.jibundashboard.generated.model.UserBase
import com.github.moriakira.jibundashboard.service.CognitoUserService
import com.github.moriakira.jibundashboard.service.UserModel
import com.github.moriakira.jibundashboard.service.UserService
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.http.HttpStatus
import org.springframework.security.oauth2.jwt.Jwt

class UserControllerTest :
    StringSpec({

        val currentAuth = mockk<CurrentAuth>(relaxed = true)
        val userService = mockk<UserService>(relaxed = true)
        val cognitoUserService = mockk<CognitoUserService>(relaxed = true)
        val controller = UserController(currentAuth, userService, cognitoUserService)

        beforeTest {
            every { currentAuth.userId } returns "u1"
            every { currentAuth.email } returns "u1@example.com"
            val jwt = mockk<Jwt>()
            every { jwt.tokenValue } returns "token-123"
            every { currentAuth.jwt } returns jwt
        }

        "getUser: getOrInit の結果を返す" {
            every { userService.getOrInit("u1", "u1@example.com") } returns UserModel(
                userId = "u1",
                userName = "Alice",
                emailAddress = "alice@example.com",
            )

            val res = controller.getUsers()

            res.statusCode shouldBe HttpStatus.OK
            res.body!!.userId shouldBe "u1"
            res.body!!.userName shouldBe "Alice"
        }

        "putUser: ユーザ情報を更新して 200 を返す" {
            every {
                userService.put(UserModel(userId = "u1", userName = "Bob", emailAddress = "bob@example.com"))
            } returns UserModel("u1", "Bob", "bob@example.com")

            val res = controller.putUsers(UserBase("Bob", "bob@example.com"))

            res.statusCode shouldBe HttpStatus.OK
            verify(exactly = 1) {
                cognitoUserService.updateEmail(accessToken = "token-123", email = "bob@example.com")
            }
        }

        "putPassword: 変更を委譲して 204 を返す" {
            val res = controller.putPassword(Password(oldPassword = "old-pass", newPassword = "new-pass"))

            res.statusCode shouldBe HttpStatus.NO_CONTENT
            verify(exactly = 1) {
                cognitoUserService.changePassword(
                    accessToken = "token-123",
                    oldPassword = "old-pass",
                    newPassword = "new-pass",
                )
            }
        }
    })
