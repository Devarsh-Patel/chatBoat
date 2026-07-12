package com.example.chatboat.data

import android.content.Context
import com.example.chatboat.data.local.ChatDatabase
import com.example.chatboat.data.remote.GeminiApiService
import com.example.chatboat.data.repository.ChatRepository
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

    private fun provideGeminiApiService(): GeminiApiService {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .client(provideOkHttpClient())
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(GeminiApiService::class.java)
    }

    fun provideChatRepository(context: Context): ChatRepository {
        val database = ChatDatabase.getDatabase(context)
        return ChatRepository(database.chatDao(), provideGeminiApiService())
    }
}
