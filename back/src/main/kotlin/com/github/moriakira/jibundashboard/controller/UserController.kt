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
        val user = userService.getOrInit(currentAuth.userId, currentAuth.email)
        return ResponseEntity.ok(
            User(
                userId = user.userId,
                userName = user.userName,
                emailAddress = user.emailAddress,
            ),
        )
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

    override fun putPassword(password: Password?): ResponseEntity<Unit> {
        requireNotNull(password) { "Request body is required." }
        cognitoUserService.changePassword(
            accessToken = currentAuth.jwt.tokenValue,
            oldPassword = password.oldPassword,
            newPassword = password.newPassword,
        )
        return ResponseEntity.noContent().build()
    }
}
