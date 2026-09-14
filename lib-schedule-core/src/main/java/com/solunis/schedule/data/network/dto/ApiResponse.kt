package com.solunis.schedule.data.network.dto

data class ApiResponse<T>(
    val code: Int,
    val data: T?,
    val msg: String
) {
    val isSuccess: Boolean get() = code == 0
}
