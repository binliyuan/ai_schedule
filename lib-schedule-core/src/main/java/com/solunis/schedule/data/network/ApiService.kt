package com.solunis.schedule.data.network

import com.solunis.schedule.data.model.LoginRequest
import com.solunis.schedule.data.model.LoginResponse
import com.solunis.schedule.data.model.RegisterRequest
import com.solunis.schedule.data.model.User
import com.solunis.schedule.data.network.dto.ApiResponse
import com.solunis.schedule.data.network.dto.ScheduleResponse
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // Auth
    @POST("user/register")
    suspend fun register(@Body req: RegisterRequest): Response<ApiResponse<User>>

    @POST("user/login")
    suspend fun login(@Body req: LoginRequest): Response<ApiResponse<LoginResponse>>

    @GET("user/info")
    suspend fun getUserInfo(): Response<ApiResponse<User>>

    // Schedule sync
    @GET("schedule/sync")
    suspend fun syncSchedule(@Query("tableId") tableId: Int? = null): Response<ApiResponse<ScheduleResponse>>

    // Course CRUD
    @POST("schedule/course")
    suspend fun createCourse(
        @Query("tableId") tableId: Int,
        @Body course: Map<String, @JvmSuppressWildcards Any>
    ): Response<ApiResponse<Any>>

    @PUT("schedule/course/{id}")
    suspend fun updateCourse(
        @Path("id") id: Int,
        @Body course: Map<String, @JvmSuppressWildcards Any>
    ): Response<ApiResponse<Any>>

    @DELETE("schedule/course/{id}")
    suspend fun deleteCourse(@Path("id") id: Int): Response<ApiResponse<Any>>
}
