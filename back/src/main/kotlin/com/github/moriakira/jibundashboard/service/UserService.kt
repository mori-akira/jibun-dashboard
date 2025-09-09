package com.github.moriakira.jibundashboard.service

import com.github.moriakira.jibundashboard.repository.UserItem
import com.github.moriakira.jibundashboard.repository.UserRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UserService(
    private val userRepository: UserRepository,
) {

    fun getUser(userId: String): UserModel? = userRepository.get(userId)?.toDomain()

    fun putUser(user: UserModel): UserModel {
        val targetId = user.userId ?: UUID.randomUUID().toString()
        val item = UserItem().apply {
            this.userId = targetId
            this.userName = user.userName
            this.emailAddress = user.emailAddress
        }
        userRepository.put(item)
        return item.toDomain()
    }

    private fun UserItem.toDomain(): UserModel = UserModel(
        userId = this.userId!!,
        userName = this.userName!!,
        emailAddress = this.emailAddress!!,
    )
}

data class UserModel(
    val userId: String?,
    val userName: String,
    val emailAddress: String,
)
