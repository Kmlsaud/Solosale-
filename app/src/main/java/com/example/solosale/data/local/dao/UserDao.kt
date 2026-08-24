package com.example.solosale.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.solosale.data.local.entity.SessionEntity
import com.example.solosale.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users ORDER BY createdAt ASC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    suspend fun getUserById(userId: Long): UserEntity?

    @Query("SELECT * FROM users WHERE username = :username AND isActive = 1 LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Delete
    suspend fun deleteUser(user: UserEntity)
}

@Dao
interface SessionDao {
    @Query("SELECT * FROM sessions WHERE token = :token AND isActive = 1 AND expiresAt > :currentTime LIMIT 1")
    suspend fun getActiveSession(token: String, currentTime: Long = System.currentTimeMillis()): SessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionEntity)

    @Query("UPDATE sessions SET isActive = 0 WHERE token = :token")
    suspend fun invalidateSession(token: String)

    @Query("UPDATE sessions SET isActive = 0 WHERE userId = :userId")
    suspend fun invalidateUserSessions(userId: Long)

    @Query("DELETE FROM sessions WHERE expiresAt < :currentTime OR isActive = 0")
    suspend fun clearExpiredSessions(currentTime: Long = System.currentTimeMillis())
}
