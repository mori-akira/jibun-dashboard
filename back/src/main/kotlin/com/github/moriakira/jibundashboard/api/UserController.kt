package com.github.moriakira.jibundashboard.api

import com.github.moriakira.jibundashboard.component.CurrentAuth
import com.github.moriakira.jibundashboard.generated.api.UserApi
import com.github.moriakira.jibundashboard.generated.model.User
import com.github.moriakira.jibundashboard.service.UserModel
import com.github.moriakira.jibundashboard.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController(
    private val currentAuth: CurrentAuth,
    private val userService: UserService,
) : UserApi {

    override fun getUser(): ResponseEntity<User> {
        var user = userService.getUser(currentAuth.userId)

        // ユーザが登録済みなら返却
        if (user != null) {
            return ResponseEntity.ok(
                User(
                    userId = user.userId,
                    userName = user.userName,
                    emailAddress = user.emailAddress,
                ),
            )
        }

        // 未登録の場合、初期登録
        user = userService.putUser(
            UserModel(
                userId = currentAuth.userId,
                userName = currentAuth.email ?: "",
                emailAddress = currentAuth.email ?: "",
            ),
        )

        // 登録したユーザ情報を返却
        return ResponseEntity.ok(
            User(
                userId = user.userId,
                userName = user.userName,
                emailAddress = user.emailAddress,
            ),
        )
    }

    override fun putUser(user: User?): ResponseEntity<Unit> {
        if (user != null) {
            userService.putUser(
                UserModel(
                    userId = currentAuth.userId,
                    userName = user.userName,
                    emailAddress = user.emailAddress,
                ),
            )
        }
        return ResponseEntity.ok().build()
    }
}
