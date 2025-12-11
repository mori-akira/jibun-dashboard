package com.github.moriakira.jibundashboard.controller

import com.github.moriakira.jibundashboard.component.CurrentAuth
import com.github.moriakira.jibundashboard.generated.model.Password
import com.github.moriakira.jibundashboard.generated.model.UserBase
import com.github.moriakira.jibundashboard.service.CognitoUserService
import com.github.moriakira.jibundashboard.service.UserModel
import com.github.moriakira.jibundashboard.service.UserService
import io.kotest.assertions.throwables.shouldThrow
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

        "getUser: 登録済みユーザを返す" {
            every { userService.get("u1") } returns UserModel(
                userId = "u1",
                userName = "Alice",
                emailAddress = "alice@example.com",
            )

            val res = controller.getUsers()

            res.statusCode shouldBe HttpStatus.OK
            res.body!!.userId shouldBe "u1"
            res.body!!.userName shouldBe "Alice"
            res.body!!.emailAddress shouldBe "alice@example.com"
            verify(exactly = 0) { userService.put(any()) }
        }

        "getUser: 未登録なら初期登録して返す" {
            every { userService.get("u1") } returns null
            every {
                userService.put(
                    UserModel(
                        userId = "u1",
                        userName = "u1@example.com",
                        emailAddress = "u1@example.com",
                    ),
                )
            } returns UserModel(
                userId = "u1",
                userName = "u1@example.com",
                emailAddress = "u1@example.com",
            )

            val res = controller.getUsers()

            res.statusCode shouldBe HttpStatus.OK
            res.body!!.userId shouldBe "u1"
            res.body!!.userName shouldBe "u1@example.com"
            res.body!!.emailAddress shouldBe "u1@example.com"
            verify(exactly = 1) {
                userService.put(
                    UserModel(
                        userId = "u1",
                        userName = "u1@example.com",
                        emailAddress = "u1@example.com",
                    ),
                )
            }
        }

        "putUser: ユーザ情報を更新して200を返す" {
            every {
                userService.put(
                    UserModel(
                        userId = "u1",
                        userName = "Bob",
                        emailAddress = "bob@example.com",
                    ),
                )
            } returns UserModel("u1", "Bob", "bob@example.com")

            val res = controller.putUsers(UserBase("Bob", "bob@example.com"))

            res.statusCode shouldBe HttpStatus.OK
            verify(exactly = 1) {
                userService.put(
                    UserModel(
                        userId = "u1",
                        userName = "Bob",
                        emailAddress = "bob@example.com",
                    ),
                )
            }
            verify(exactly = 1) {
                cognitoUserService.updateEmail(
                    accessToken = "token-123",
                    email = "bob@example.com",
                )
            }
        }

        "putUser: null は例外" {
            shouldThrow<IllegalArgumentException> {
                controller.putUsers(null)
            }
        }

        "postPassword: 変更を委譲して204を返す" {
            val password = Password(oldPassword = "old-pass", newPassword = "new-pass")

            val res = controller.postPassword(password)

            res.statusCode shouldBe HttpStatus.NO_CONTENT
            verify(exactly = 1) {
                cognitoUserService.changePassword(
                    accessToken = "token-123",
                    oldPassword = "old-pass",
                    newPassword = "new-pass",
                )
            }
        }

        "postPassword: null は例外" {
            shouldThrow<IllegalArgumentException> {
                controller.postPassword(null)
            }
        }
    })
