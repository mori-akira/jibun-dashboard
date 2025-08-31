package com.github.moriakira.jibundashboard.api

import com.github.moriakira.jibundashboard.CurrentAuth
import com.github.moriakira.jibundashboard.generated.api.UserApi
import com.github.moriakira.jibundashboard.generated.model.User
import com.github.moriakira.jibundashboard.service.UserModel
import com.github.moriakira.jibundashboard.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController(
    private val currentAuth: CurrentAuth,
    private val userService: UserService,
) : UserApi {

    override fun getUser(): ResponseEntity<User> {
        val userId = currentAuth.userId
        val user = userService.getUser(userId)
        return if (user != null) {
            ResponseEntity.ok(
                User(
                    userId = user.userId,
                    userName = user.userName,
                    emailAddress = user.emailAddress,
                ),
            )
        } else {
            ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        }
    }

    override fun putUser(user: User?): ResponseEntity<Unit> {
        val userId = currentAuth.userId
        userService.createUser(
            UserModel(
                userId = userId,
                userName = user?.userName ?: "",
                emailAddress = user?.emailAddress ?: "",
            ),
        )
        return ResponseEntity.ok().build()
    }
}
