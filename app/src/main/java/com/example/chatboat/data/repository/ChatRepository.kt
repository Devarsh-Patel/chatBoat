package com.example.chatboat.data.repository

import com.example.chatboat.BuildConfig
import com.example.chatboat.data.local.ChatDao
import com.example.chatboat.data.local.ChatMessageEntity
import com.example.chatboat.data.local.ChatSessionEntity
import com.example.chatboat.data.remote.GeminiApiService
import com.example.chatboat.data.remote.GeminiContent
import com.example.chatboat.data.remote.GeminiPart
import com.example.chatboat.data.remote.GeminiRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class ChatRepository(
    private val chatDao: ChatDao,
    private val geminiApiService: GeminiApiService
) {
    private val apiKey = BuildConfig.API_KEY
    private val model = "gemini-2.5-flash"

    fun getAllSessions(): Flow<List<ChatSessionEntity>> = chatDao.getAllSessions()

    fun getMessagesForSession(sessionId: Long): Flow<List<ChatMessageEntity>> =
        chatDao.getMessagesForSession(sessionId)

    suspend fun createNewSession(title: String): Long {
        return chatDao.insertSession(ChatSessionEntity(title = title))
    }

    suspend fun sendMessage(sessionId: Long, userMessage: String): String {
        // 1. Save user message to local DB
        chatDao.insertMessage(
            ChatMessageEntity(
                sessionId = sessionId,
                role = "user",
                content = userMessage
            )
        )

        // 2. Prepare context for Gemini (recent messages)
        val history = chatDao.getMessagesForSession(sessionId).first()
        val contents = history.map {
            GeminiContent(
                role = if (it.role == "user") "user" else "model",
                parts = listOf(GeminiPart(text = it.content))
            )
        }

        // 3. Call Gemini API
        val request = GeminiRequest(contents = contents)
        val response = geminiApiService.generateContent(
            model = model,
            apiKey = apiKey,
            request = request
        )

        val aiResponseContent = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            ?: "Sorry, I couldn't generate a response."

        // 4. Save AI response to local DB
        chatDao.insertMessage(
            ChatMessageEntity(
                sessionId = sessionId,
                role = "model",
                content = aiResponseContent
            )
        )

        return aiResponseContent
    }

    suspend fun deleteSession(sessionId: Long) {
        chatDao.deleteSession(sessionId)
    }
}
