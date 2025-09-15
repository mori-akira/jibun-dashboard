package com.github.moriakira.jibundashboard.controller

import com.github.moriakira.jibundashboard.component.CurrentAuth
import com.github.moriakira.jibundashboard.generated.api.UserApi
import com.github.moriakira.jibundashboard.generated.model.Password
import com.github.moriakira.jibundashboard.generated.model.User
import com.github.moriakira.jibundashboard.service.CognitoUserService
import com.github.moriakira.jibundashboard.service.UserModel
import com.github.moriakira.jibundashboard.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController(
    private val currentAuth: CurrentAuth,
    private val userService: UserService,
    private val cognitoUserService: CognitoUserService,
) : UserApi {

    override fun getUser(): ResponseEntity<User> {
        var user = userService.getUser(currentAuth.userId)
        return user?.let {
            // ユーザが登録済みなら返却
            ResponseEntity.ok(
                User(
                    userId = user.userId,
                    userName = user.userName,
                    emailAddress = user.emailAddress,
                ),
            )
        } ?: run {
            // 未登録の場合、初期登録
            user = userService.putUser(
                UserModel(
                    userId = currentAuth.userId,
                    userName = currentAuth.email,
                    emailAddress = currentAuth.email,
                ),
            )

            // 登録したユーザ情報を返却
            ResponseEntity.ok(
                User(
                    userId = user.userId,
                    userName = user.userName,
                    emailAddress = user.emailAddress,
                ),
            )
        }
    }

    override fun putUser(user: User?): ResponseEntity<Unit> {
        requireNotNull(user) { "Request body is required." }
        user.let {
            userService.putUser(
                UserModel(
                    userId = currentAuth.userId,
                    userName = it.userName,
                    emailAddress = it.emailAddress,
                ),
            )
        }
        return ResponseEntity.ok().build()
    }

    override fun postPassword(password: Password?): ResponseEntity<Unit> {
        requireNotNull(password) { "Request body is required." }
        cognitoUserService.changePassword(
            accessToken = currentAuth.jwt.tokenValue,
            oldPassword = password.oldPassword,
            newPassword = password.newPassword,
        )
        return ResponseEntity.noContent().build()
    }
}
