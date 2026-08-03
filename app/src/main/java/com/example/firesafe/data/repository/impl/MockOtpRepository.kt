package com.example.firesafe.data.repository.impl

import com.example.firesafe.data.repository.OtpRepository
import kotlinx.coroutines.delay

class MockOtpRepository : OtpRepository {
    override suspend fun sendOtp(phoneNumber: String): Boolean {
        delay(1000)
        return true
    }

    override suspend fun verifyOtp(code: String): Boolean {
        delay(1000)
        // Accept "123456" as the successful OTP, or any code for testing
        return code == "123456" || code == "111111"
    }
}
