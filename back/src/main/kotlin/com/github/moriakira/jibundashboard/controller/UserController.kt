package com.github.moriakira.jibundashboard.controller

import com.github.moriakira.jibundashboard.component.CurrentAuth
import com.github.moriakira.jibundashboard.generated.api.UserApi
import com.github.moriakira.jibundashboard.generated.model.Password
import com.github.moriakira.jibundashboard.generated.model.User
import com.github.moriakira.jibundashboard.generated.model.UserBase
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

    override fun getUsers(): ResponseEntity<User> {
        var user = userService.get(currentAuth.userId)
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
            user = userService.put(
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

    override fun putUsers(userBase: UserBase?): ResponseEntity<Unit> {
        requireNotNull(userBase) { "Request body is required." }
        userBase.let {
            userService.put(
                UserModel(
                    userId = currentAuth.userId,
                    userName = it.userName,
                    emailAddress = it.emailAddress,
                ),
            )
            cognitoUserService.updateEmail(
                accessToken = currentAuth.jwt.tokenValue,
                email = userBase.emailAddress,
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
