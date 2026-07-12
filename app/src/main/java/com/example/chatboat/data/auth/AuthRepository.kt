package com.example.chatboat.data.auth

import com.example.chatboat.data.remote.AuthApiService
import com.example.chatboat.data.remote.VerificationRequest
import kotlinx.coroutines.delay

enum class AuthProvider {
    GOOGLE, APPLE, PHONE
}

class AuthRepository(private val authApiService: AuthApiService) {

    suspend fun sendVerificationCode(provider: AuthProvider, identifier: String): Boolean {
        // Generate a random 6-digit code
        val code = (100000..999999).random().toString()

        return try {
            // CALLING THE BACKEND
            val response = authApiService.sendCode(
                VerificationRequest(
                    provider = provider.name,
                    identifier = identifier,
                    code = code
                )
            )
            response.success
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun verifyCode(provider: AuthProvider, identifier: String, code: String): Boolean {
        return try {
            // VERIFYING WITH THE BACKEND
            val response = authApiService.verifyCode(
                VerificationRequest(
                    provider = provider.name,
                    identifier = identifier,
                    code = code
                )
            )
            response.success
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
