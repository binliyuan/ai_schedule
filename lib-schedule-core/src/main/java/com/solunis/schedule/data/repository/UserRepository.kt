package com.solunis.schedule.data.repository

import com.solunis.schedule.data.local.TokenManager
import com.solunis.schedule.data.model.LoginRequest
import com.solunis.schedule.data.model.LoginResponse
import com.solunis.schedule.data.model.RegisterRequest
import com.solunis.schedule.data.model.User
import com.solunis.schedule.data.network.ApiService

class UserRepository(private val apiService: ApiService) {

    suspend fun login(username: String, password: String): Result<LoginResponse> {
        return try {
            val response = apiService.login(LoginRequest(username, password))
            val body = response.body()
            if (response.isSuccessful && body != null && body.isSuccess && body.data != null) {
                TokenManager.saveToken(body.data.token)
                TokenManager.saveUser(body.data.user)
                Result.success(body.data)
            } else {
                val msg = body?.msg ?: "登录失败"
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Result.failure(Exception("网络连接失败: ${e.message}"))
        }
    }

    suspend fun register(username: String, password: String, nickname: String): Result<User> {
        return try {
            val response = apiService.register(RegisterRequest(username, password, nickname))
            val body = response.body()
            if (response.isSuccessful && body != null && body.isSuccess && body.data != null) {
                Result.success(body.data)
            } else {
                val msg = body?.msg ?: "注册失败"
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Result.failure(Exception("网络连接失败: ${e.message}"))
        }
    }

    fun isLoggedIn(): Boolean = TokenManager.isLoggedIn()

    fun getCurrentUser(): User? = TokenManager.getUser()

    fun logout() {
        TokenManager.clear()
    }
}
