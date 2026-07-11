package com.example.data

import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class SphereRepository(
    private val progressDao: ProgressDao,
    private val sphereDao: SphereDao
) {
    val userProgress: Flow<UserProgress?> = progressDao.getProgressFlow()
    val subjects: Flow<List<SubjectEntity>> = sphereDao.getAllSubjects()

    fun getTopicsForSubject(subjectId: String): Flow<List<TopicEntity>> = 
        sphereDao.getTopicsForSubject(subjectId)

    fun getLessonsForTopic(topicId: String): Flow<List<LessonEntity>> = 
        sphereDao.getLessonsForTopic(topicId)

    fun getQuizForTopic(topicId: String): Flow<List<QuizQuestionEntity>> = 
        sphereDao.getQuizForTopic(topicId)

    suspend fun addPoints(points: Int) {
        progressDao.addPoints(points)
    }

    suspend fun checkAndUnlockBadges() {
        val current = progressDao.getProgressDirect() ?: UserProgress()
        val completedTopics = current.completedTopicsJson.split(",").filter { it.isNotBlank() }.toSet()
        val currentUnlockedBadges = current.unlockedBadgesJson.split(",").filter { it.isNotBlank() }.toMutableSet()

        val allTopics = MockDataGenerator.getTopics()
        val topicsBySubject = allTopics.groupBy { it.subjectId }

        var progressChanged = false

        for ((subjectId, topics) in topicsBySubject) {
            val topicIds = topics.map { it.id }.toSet()
            val badgeId = "badge_$subjectId"
            if (completedTopics.containsAll(topicIds)) {
                if (currentUnlockedBadges.add(badgeId)) {
                    progressChanged = true
                }
            } else {
                if (currentUnlockedBadges.remove(badgeId)) {
                    progressChanged = true
                }
            }
        }

        if (progressChanged) {
            val updatedBadgesJson = currentUnlockedBadges.joinToString(",")
            progressDao.insertOrUpdateProgress(current.copy(unlockedBadgesJson = updatedBadgesJson))
        }
    }

    suspend fun toggleSubjectCompletion(subjectId: String, completeAll: Boolean) {
        val current = progressDao.getProgressDirect() ?: UserProgress()
        val completedTopics = current.completedTopicsJson.split(",")
            .filter { it.isNotBlank() }
            .toMutableSet()
        
        val allTopics = MockDataGenerator.getTopics()
        val subjectTopicIds = allTopics.filter { it.subjectId == subjectId }.map { it.id }
        
        if (completeAll) {
            completedTopics.addAll(subjectTopicIds)
        } else {
            completedTopics.removeAll(subjectTopicIds)
        }
        
        val updatedJson = completedTopics.joinToString(",")
        progressDao.insertOrUpdateProgress(current.copy(completedTopicsJson = updatedJson))
        
        checkAndUnlockBadges()
    }

    suspend fun completeTopic(topicId: String) {
        val current = progressDao.getProgressDirect() ?: UserProgress()
        val completedTopics = current.completedTopicsJson.split(",")
            .filter { it.isNotBlank() }
            .toMutableSet()
        if (completedTopics.add(topicId)) {
            val updatedJson = completedTopics.joinToString(",")
            progressDao.insertOrUpdateProgress(current.copy(completedTopicsJson = updatedJson))
            checkAndUnlockBadges()
        }
    }

    suspend fun completeQuiz(topicId: String) {
        val current = progressDao.getProgressDirect() ?: UserProgress()
        val completedQuizzes = current.completedQuizzesJson.split(",")
            .filter { it.isNotBlank() }
            .toMutableSet()
        if (completedQuizzes.add(topicId)) {
            val updatedJson = completedQuizzes.joinToString(",")
            progressDao.insertOrUpdateProgress(current.copy(completedQuizzesJson = updatedJson))
        }
    }

    suspend fun resetAllProgress() {
        val current = progressDao.getProgressDirect() ?: UserProgress()
        val resetProgress = current.copy(
            spherePoints = 0,
            streakCount = 0,
            lastActiveTime = 0L,
            unlockedBadgesJson = "",
            completedTopicsJson = "",
            completedQuizzesJson = ""
        )
        progressDao.insertOrUpdateProgress(resetProgress)
    }

    suspend fun updateStreak() {
        val current = progressDao.getProgressDirect() ?: UserProgress()
        val now = System.currentTimeMillis()
        val lastActive = current.lastActiveTime

        if (lastActive == 0L) {
            progressDao.insertOrUpdateProgress(current.copy(streakCount = 1, lastActiveTime = now))
            return
        }

        val calNow = Calendar.getInstance().apply { timeInMillis = now }
        val calLast = Calendar.getInstance().apply { timeInMillis = lastActive }

        val diffDays = (calNow.get(Calendar.DAY_OF_YEAR) - calLast.get(Calendar.DAY_OF_YEAR)) +
                (calNow.get(Calendar.YEAR) - calLast.get(Calendar.YEAR)) * 365

        val newStreak = when {
            diffDays == 1 -> current.streakCount + 1
            diffDays > 1 -> 1
            else -> current.streakCount // Same day
        }

        progressDao.insertOrUpdateProgress(
            current.copy(streakCount = newStreak, lastActiveTime = now)
        )
    }

    suspend fun populateMockData() {
        // Use the comprehensive data from MockDataGenerator
        sphereDao.insertSubjects(MockDataGenerator.getSubjects())
        sphereDao.insertTopics(MockDataGenerator.getTopics())
        sphereDao.insertLessons(MockDataGenerator.getLessons())
        sphereDao.insertQuizQuestions(MockDataGenerator.getQuizQuestions())
        sphereDao.insertBadges(MockDataGenerator.getBadges())
    }
}
