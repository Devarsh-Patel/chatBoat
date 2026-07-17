package com.example.chatboat.data

import android.content.Context
import com.example.chatboat.data.local.ChatDatabase
import com.example.chatboat.data.remote.BackendApiService
import com.example.chatboat.data.repository.ChatRepository
import com.example.chatboat.data.auth.AuthRepository
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

object DataModule {
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    private fun provideBackendApiService(): BackendApiService {
        val contentType = "application/json".toMediaType()
        // 10.0.2.2 is the special IP for Android Emulator to access host machine
        return Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080/")
            .client(provideOkHttpClient())
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(BackendApiService::class.java)
    }

    fun provideChatRepository(context: Context): ChatRepository {
        val database = ChatDatabase.getDatabase(context)
        return ChatRepository(database.chatDao(), provideBackendApiService())
    }

    fun provideAuthRepository(): AuthRepository {
        return AuthRepository(provideBackendApiService())
    }
}
