package com.example.solosale.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.solosale.data.local.entity.UserEntity
import com.example.solosale.data.repository.AuthRepository
import com.example.solosale.data.repository.AuthResult
import com.example.solosale.utils.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class AuthUiState {
    object Initial : AuthUiState()
    object Loading : AuthUiState()
    data class Authenticated(val user: UserEntity, val token: String) : AuthUiState()
    object Unauthenticated : AuthUiState()
    object NeedsFirstAdmin : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Initial)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val currentUserName: StateFlow<String?> = tokenManager.userNameFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "Staff"
    )

    val currentUserRole: StateFlow<String?> = tokenManager.userRoleFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "STAFF"
    )

    fun checkAuthStatus() {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val userCount = authRepository.getUserCount()
            if (userCount == 0) {
                _uiState.value = AuthUiState.NeedsFirstAdmin
                return@launch
            }

            val token = tokenManager.tokenFlow.firstOrNull()
            if (token != null) {
                val user = authRepository.validateSession(token)
                if (user != null) {
                    _uiState.value = AuthUiState.Authenticated(user, token)
                } else {
                    tokenManager.clearSession()
                    _uiState.value = AuthUiState.Unauthenticated
                }
            } else {
                _uiState.value = AuthUiState.Unauthenticated
            }
        }
    }

    fun registerAdmin(fullName: String, username: String, password: String, confirmPass: String) {
        if (password != confirmPass) {
            _uiState.value = AuthUiState.Error("Passwords do not match.")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            when (val result = authRepository.registerFirstAdmin(fullName, username, password)) {
                is AuthResult.Success -> {
                    _uiState.value = AuthUiState.Authenticated(result.user, result.token)
                }
                is AuthResult.Error -> {
                    _uiState.value = AuthUiState.Error(result.message)
                }
            }
        }
    }

    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState.Error("Please enter username and password.")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            when (val result = authRepository.login(username, password)) {
                is AuthResult.Success -> {
                    _uiState.value = AuthUiState.Authenticated(result.user, result.token)
                }
                is AuthResult.Error -> {
                    _uiState.value = AuthUiState.Error(result.message)
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            val token = tokenManager.tokenFlow.firstOrNull()
            authRepository.logout(token)
            _uiState.value = AuthUiState.Unauthenticated
        }
    }

    fun clearError() {
        if (_uiState.value is AuthUiState.Error) {
            _uiState.value = AuthUiState.Unauthenticated
        }
    }
}
