package com.example.solosale.utils

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.UUID

object PasswordHasher {
    private const val SALT = "SoloSale_Secured_Salt_Nepal_2026"

    fun hashPassword(password: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val combined = password + SALT
        val digest = md.digest(combined.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    fun verifyPassword(password: String, storedHash: String): Boolean {
        val hash = hashPassword(password)
        return hash.equals(storedHash, ignoreCase = true)
    }

    fun generateSessionToken(): String {
        return UUID.randomUUID().toString() + "-" + System.currentTimeMillis()
    }
}
