package com.example.chatboat.data.auth

import kotlinx.coroutines.delay

enum class AuthProvider {
    GOOGLE, APPLE, PHONE
}

class AuthRepository {

    suspend fun sendVerificationCode(provider: AuthProvider, identifier: String): Boolean {
        // Simulate network delay
        delay(1500)
        // In a real app, this would call Firebase Auth or a backend API
        // to send an OTP to the email or phone number.
        println("Sending verification code to $identifier via $provider")
        return true
    }

    suspend fun verifyCode(provider: AuthProvider, identifier: String, code: String): Boolean {
        delay(1000)
        // Mock verification: any 6-digit code works for now
        return code.length == 6
    }
}
