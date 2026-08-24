package com.example.solosale.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.solosale.data.local.entity.BusinessSettingsEntity
import com.example.solosale.data.local.entity.UserEntity
import com.example.solosale.data.local.entity.UserRole
import com.example.solosale.data.repository.AuthRepository
import com.example.solosale.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val settings: StateFlow<BusinessSettingsEntity> = settingsRepository.settingsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = BusinessSettingsEntity()
        )

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage = _statusMessage.asStateFlow()

    fun updateSettings(updated: BusinessSettingsEntity) {
        viewModelScope.launch {
            settingsRepository.updateSettings(updated)
            _statusMessage.value = "Settings updated successfully"
        }
    }

    fun clearStatus() {
        _statusMessage.value = null
    }
}

class UserManagementViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    val users: StateFlow<List<UserEntity>> = authRepository.allUsers.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage = _statusMessage.asStateFlow()

    fun addStaff(fullName: String, username: String, pass: String, role: UserRole, onSuccess: () -> Unit) {
        if (fullName.isBlank() || username.isBlank() || pass.length < 4) {
            _statusMessage.value = "Please fill all fields. Password must be at least 4 chars."
            return
        }
        viewModelScope.launch {
            val success = authRepository.addStaffUser(fullName, username, pass, role)
            if (success) {
                _statusMessage.value = "User created successfully"
                onSuccess()
            } else {
                _statusMessage.value = "Username already exists"
            }
        }
    }

    fun toggleUserStatus(user: UserEntity) {
        viewModelScope.launch {
            authRepository.updateUser(user.copy(isActive = !user.isActive))
            _statusMessage.value = "User status updated"
        }
    }

    fun deleteUser(user: UserEntity) {
        viewModelScope.launch {
            authRepository.deleteUser(user)
            _statusMessage.value = "User deleted"
        }
    }

    fun clearStatus() {
        _statusMessage.value = null
    }
}
