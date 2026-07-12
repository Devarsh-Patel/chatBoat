package com.example.chatboat.data.remote

import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.POST

@Serializable
data class VerificationRequest(
    val provider: String,
    val identifier: String,
    val code: String
)

@Serializable
data class AuthResponse(
    val success: Boolean,
    val message: String? = null
)

interface AuthApiService {
    @POST("api/auth/send-code")
    suspend fun sendCode(@Body request: VerificationRequest): AuthResponse

    @POST("api/auth/verify-code")
    suspend fun verifyCode(@Body request: VerificationRequest): AuthResponse
}
