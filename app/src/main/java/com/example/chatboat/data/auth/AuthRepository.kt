package com.example.chatboat.data.auth

import kotlinx.coroutines.delay

enum class AuthProvider {
    GOOGLE, APPLE, PHONE
}

class AuthRepository {

    private var generatedCode: String? = null
    private var lastIdentifier: String? = null

    suspend fun sendVerificationCode(provider: AuthProvider, identifier: String): Boolean {
        // Generate a random 6-digit code
        val code = (100000..999999).random().toString()
        generatedCode = code
        lastIdentifier = identifier

        // Simulate network delay
        delay(1500)

        // In a real app, you would use a service like:
        // 1. Firebase Authentication (Recommended for Android)
        // 2. SendGrid / Mailgun API (for Email)
        // 3. Twilio API (for SMS)
        
        // For development, we print the code to the system logs.
        // You can see this in the Logcat tab in Android Studio.
        android.util.Log.d("chatBoat_Auth", "--------------------------------------")
        android.util.Log.d("chatBoat_Auth", "VERIFICATION CODE for $identifier")
        android.util.Log.d("chatBoat_Auth", "Provider: $provider")
        android.util.Log.d("chatBoat_Auth", "Code: $code")
        android.util.Log.d("chatBoat_Auth", "--------------------------------------")

        return true
    }

    suspend fun verifyCode(provider: AuthProvider, identifier: String, code: String): Boolean {
        delay(1000)
        // Check if the entered code matches the one we generated and for the correct user
        return code == generatedCode && identifier == lastIdentifier
    }
}
