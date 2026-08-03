package com.example.firesafe.data.repository.impl.network

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface Msg91ApiService {
    @POST("api/v5/otp")
    suspend fun sendOtp(
        @Header("authkey") authKey: String,
        @Query("template_id") templateId: String,
        @Query("mobile") mobile: String,
        @Query("channel") channel: String = "sms"
    ): Response<Msg91Response>

    @GET("api/v5/otp/verify")
    suspend fun verifyOtp(
        @Header("authkey") authKey: String,
        @Query("mobile") mobile: String,
        @Query("otp") otp: String
    ): Response<Msg91Response>
}

data class Msg91Response(
    val message: String?,
    val type: String?
)
