package com.solunis.schedule.data.model

data class User(
    val id: Int,
    val username: String,
    val nickname: String,
    val avatar: String = "",
    val phone: String = "",
    val email: String = "",
    val studentId: String = ""
)

data class LoginRequest(
    val username: String,
    val password: String
)

data class RegisterRequest(
    val username: String,
    val password: String,
    val nickname: String = ""
)

data class LoginResponse(
    val user: User,
    val token: String
)
