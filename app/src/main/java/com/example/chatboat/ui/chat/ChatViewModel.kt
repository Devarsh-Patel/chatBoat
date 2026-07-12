package com.example.chatboat.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatboat.data.local.ChatMessageEntity
import com.example.chatboat.data.local.ChatSessionEntity
import com.example.chatboat.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ChatViewModel(private val repository: ChatRepository) : ViewModel() {

    private val _sessions = MutableStateFlow<List<ChatSessionEntity>>(emptyList())
    val sessions: StateFlow<List<ChatSessionEntity>> = _sessions.asStateFlow()

    private val _currentSessionId = MutableStateFlow<Long?>(null)
    val currentSessionId: StateFlow<Long?> = _currentSessionId.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessageEntity>>(emptyList())
    val messages: StateFlow<List<ChatMessageEntity>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadSessions()
    }

    private fun loadSessions() {
        viewModelScope.launch {
            repository.getAllSessions().collectLatest {
                _sessions.value = it
            }
        }
    }

    fun selectSession(sessionId: Long) {
        _currentSessionId.value = sessionId
        viewModelScope.launch {
            repository.getMessagesForSession(sessionId).collectLatest {
                _messages.value = it
            }
        }
    }

    fun startNewChat(title: String) {
        viewModelScope.launch {
            val id = repository.createNewSession(title)
            selectSession(id)
        }
    }

    fun sendMessage(content: String) {
        val sessionId = _currentSessionId.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.sendMessage(sessionId, content)
            } catch (e: Exception) {
                // Handle error (e.g., save an error message to DB or show a toast)
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteSession(sessionId: Long) {
        viewModelScope.launch {
            repository.deleteSession(sessionId)
            if (_currentSessionId.value == sessionId) {
                _currentSessionId.value = null
                _messages.value = emptyList()
            }
        }
    }
}
