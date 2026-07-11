package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_progress")
data class UserProgress(
    @PrimaryKey val id: Int = 1,
    val spherePoints: Int = 0,
    val streakCount: Int = 0,
    val lastActiveTime: Long = 0L,
    val unlockedBadgesJson: String = "", // Comma-separated list of badge IDs
    val completedTopicsJson: String = "", // Comma-separated list of completed topic IDs
    val completedQuizzesJson: String = "" // Comma-separated list of completed quiz topic IDs
)
