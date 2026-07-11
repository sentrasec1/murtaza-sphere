package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey val id: String,
    val title: String,
    val icon: String,
    val color: String
)

@Entity(tableName = "topics")
data class TopicEntity(
    @PrimaryKey val id: String,
    val subjectId: String,
    val title: String
)

@Entity(tableName = "lessons")
data class LessonEntity(
    @PrimaryKey val id: String,
    val topicId: String,
    val title: String,
    val content: String,
    val audioScript: String,
    val videoYoutubeId: String? = null, // New field for learning videos
    val xpReward: Int = 20
)

@Entity(tableName = "quiz_questions")
data class QuizQuestionEntity(
    @PrimaryKey val id: String,
    val topicId: String,
    val question: String,
    val options: String, // Comma separated or JSON
    val correctIndex: Int,
    val explanation: String
)

@Entity(tableName = "badges")
data class BadgeEntity(
    @PrimaryKey val id: String,
    val name: String,
    val icon: String,
    val description: String,
    val requirement: String
)
