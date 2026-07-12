package com.example.chatboat.data.remote

import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.POST

@Serializable
data class VerificationRequest(
    val provider: String? = null,
    val identifier: String,
    val code: String? = null
)

@Serializable
data class AuthResponse(
    val success: Boolean,
    val message: String? = null
)

interface BackendApiService {
    @POST("api/auth/send-code")
    suspend fun sendCode(@Body request: VerificationRequest): AuthResponse

    @POST("api/auth/verify-code")
    suspend fun verifyCode(@Body request: VerificationRequest): AuthResponse

    @POST("api/ai/chat")
    suspend fun chat(@Body request: GeminiRequest): GeminiResponse
}
