package com.solunis.schedule.data.network

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    @GET("schedule")
    suspend fun getSchedule(
        @Query("studentId") studentId: String,
        @Query("semester") semester: String
    ): Response<ResponseBody>

    @POST("grade")
    suspend fun getGrades(
        @Query("studentId") studentId: String,
        @Query("semester") semester: String
    ): Response<ResponseBody>
}
