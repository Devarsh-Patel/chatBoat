package com.example.chatboat.data.auth

import com.example.chatboat.data.remote.BackendApiService
import com.example.chatboat.data.remote.VerificationRequest

enum class AuthProvider {
    GOOGLE, APPLE, PHONE
}

class AuthRepository(private val backendApiService: BackendApiService) {

    suspend fun sendVerificationCode(provider: AuthProvider, identifier: String): Boolean {
        // Generate a random 6-digit code
        val code = (100000..999999).random().toString()

        return try {
            val response = backendApiService.sendCode(
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
            val response = backendApiService.verifyCode(
                VerificationRequest(
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
