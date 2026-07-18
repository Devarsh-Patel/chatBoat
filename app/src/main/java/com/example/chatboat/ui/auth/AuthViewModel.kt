package com.example.chatboat.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatboat.data.auth.AuthProvider
import com.example.chatboat.data.auth.AuthRepository
import com.example.chatboat.data.session.SessionManager
import com.example.chatboat.util.NetworkMonitor
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface AuthUiState {
    data object Landing : AuthUiState
    data class InputIdentifier(val provider: AuthProvider) : AuthUiState
    data class Verification(val provider: AuthProvider, val identifier: String) : AuthUiState
    data object Success : AuthUiState
}

class AuthViewModel(
    private val repository: AuthRepository,
    private val sessionManager: SessionManager,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Landing)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    val isOnline = networkMonitor.isOnline.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    fun selectProvider(provider: AuthProvider) {
        _uiState.value = AuthUiState.InputIdentifier(provider)
    }

    fun goBack() {
        _uiState.value = when (val current = _uiState.value) {
            is AuthUiState.Verification -> AuthUiState.InputIdentifier(current.provider)
            is AuthUiState.InputIdentifier -> AuthUiState.Landing
            else -> AuthUiState.Landing
        }
    }

    fun requestVerificationCode(identifier: String) {
        val currentState = _uiState.value as? AuthUiState.InputIdentifier ?: return
        
        viewModelScope.launch {
            if (!isOnline.value) {
                _error.value = "No internet connection. Please turn on Wi-Fi or Mobile Data."
                return@launch
            }

            _isLoading.value = true
            _error.value = null
            val success = repository.sendVerificationCode(currentState.provider, identifier)
            _isLoading.value = false
            
            if (success) {
                _uiState.value = AuthUiState.Verification(currentState.provider, identifier)
            } else {
                _error.value = "Unable to reach the server. Please ensure the backend is running and you are connected to the internet."
                android.util.Log.e("chatBoat_Auth", "Failed to send code to $identifier")
            }
        }
    }

    fun verifyCode(code: String) {
        val currentState = _uiState.value as? AuthUiState.Verification ?: return
        
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val success = repository.verifyCode(currentState.provider, currentState.identifier, code)
            _isLoading.value = false
            
            if (success) {
                viewModelScope.launch {
                    sessionManager.saveSession(currentState.identifier)
                }
                _uiState.value = AuthUiState.Success
            } else {
                _error.value = "Invalid verification code."
            }
        }
    }
}
