package com.example.solosale.data.repository

import com.example.solosale.data.local.dao.SessionDao
import com.example.solosale.data.local.dao.UserDao
import com.example.solosale.data.local.entity.SessionEntity
import com.example.solosale.data.local.entity.UserEntity
import com.example.solosale.data.local.entity.UserRole
import com.example.solosale.utils.PasswordHasher
import com.example.solosale.utils.TokenManager
import kotlinx.coroutines.flow.Flow

sealed class AuthResult {
    data class Success(val user: UserEntity, val token: String) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

class AuthRepository(
    private val userDao: UserDao,
    private val sessionDao: SessionDao,
    private val tokenManager: TokenManager
) {
    val allUsers: Flow<List<UserEntity>> = userDao.getAllUsers()

    suspend fun getUserCount(): Int = userDao.getUserCount()

    suspend fun registerFirstAdmin(fullName: String, username: String, password: String): AuthResult {
        if (fullName.isBlank() || username.isBlank() || password.length < 4) {
            return AuthResult.Error("Please fill all fields. Password must be at least 4 characters.")
        }
        val existing = userDao.getUserByUsername(username.trim())
        if (existing != null) {
            return AuthResult.Error("Username already exists. Please choose another.")
        }
        val adminUser = UserEntity(
            username = username.trim(),
            passwordHash = PasswordHasher.hashPassword(password),
            fullName = fullName.trim(),
            role = UserRole.ADMIN,
            isActive = true
        )
        val userId = userDao.insertUser(adminUser)
        val token = PasswordHasher.generateSessionToken()
        val session = SessionEntity(token = token, userId = userId)
        sessionDao.insertSession(session)
        tokenManager.saveSession(token, userId, adminUser.role.name, adminUser.fullName)
        return AuthResult.Success(adminUser.copy(userId = userId), token)
    }

    suspend fun login(username: String, password: String): AuthResult {
        val user = userDao.getUserByUsername(username.trim())
            ?: return AuthResult.Error("User not found or inactive.")

        if (!PasswordHasher.verifyPassword(password, user.passwordHash)) {
            return AuthResult.Error("Incorrect password.")
        }

        val token = PasswordHasher.generateSessionToken()
        val session = SessionEntity(token = token, userId = user.userId)
        sessionDao.insertSession(session)
        tokenManager.saveSession(token, user.userId, user.role.name, user.fullName)
        return AuthResult.Success(user, token)
    }

    suspend fun validateSession(token: String): UserEntity? {
        val session = sessionDao.getActiveSession(token) ?: return null
        return userDao.getUserById(session.userId)
    }

    suspend fun logout(token: String?) {
        if (token != null) {
            sessionDao.invalidateSession(token)
        }
        tokenManager.clearSession()
    }

    suspend fun addStaffUser(fullName: String, username: String, password: String, role: UserRole): Boolean {
        if (userDao.getUserByUsername(username.trim()) != null) return false
        val user = UserEntity(
            fullName = fullName.trim(),
            username = username.trim(),
            passwordHash = PasswordHasher.hashPassword(password),
            role = role,
            isActive = true
        )
        userDao.insertUser(user)
        return true
    }

    suspend fun updateUser(user: UserEntity) = userDao.updateUser(user)

    suspend fun deleteUser(user: UserEntity) = userDao.deleteUser(user)
}
