package com.example.solosale.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UserRole {
    ADMIN,
    STAFF
}

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val userId: Long = 0,
    val username: String,
    val passwordHash: String,
    val fullName: String,
    val phone: String = "",
    val email: String = "",
    val role: UserRole = UserRole.STAFF,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey
    val token: String,
    val userId: Long,
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000), // 30 days
    val isActive: Boolean = true
)
