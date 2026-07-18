package com.example.chatboat.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.chatboat.data.auth.AuthRepository
import com.example.chatboat.data.session.SessionManager
import com.example.chatboat.util.NetworkMonitor

class AuthViewModelFactory(
    private val repository: AuthRepository,
    private val sessionManager: SessionManager,
    private val networkMonitor: NetworkMonitor
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(repository, sessionManager, networkMonitor) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
