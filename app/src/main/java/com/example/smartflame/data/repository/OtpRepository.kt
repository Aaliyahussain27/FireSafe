package com.example.smartflame.data.repository

interface OtpRepository {
    suspend fun sendOtp(phoneNumber: String): Boolean
    suspend fun verifyOtp(code: String): Boolean
}
