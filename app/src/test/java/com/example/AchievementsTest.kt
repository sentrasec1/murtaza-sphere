package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.MockDataGenerator
import com.example.data.SphereDatabase
import com.example.data.SphereRepository
import com.example.data.UserProgress
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import androidx.room.Room
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AchievementsTest {

    private lateinit var repository: SphereRepository
    private lateinit var database: SphereDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, SphereDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = SphereRepository(database.progressDao(), database.sphereDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testAchievementsSystem_unlockedAndLockedStates() = runBlocking {
        // Initialize DB
        repository.populateMockData()

        // Verify start state: no completed topics, no unlocked badges
        val initialProgress = repository.userProgress.first() ?: UserProgress()
        assertEquals("", initialProgress.completedTopicsJson)
        assertEquals("", initialProgress.unlockedBadgesJson)

        // Math topics: get the list from generator
        val allTopics = MockDataGenerator.getTopics()
        val mathTopicIds = allTopics.filter { it.subjectId == "math" }.map { it.id }
        assertEquals(15, mathTopicIds.size)

        // Complete 14 topics of Math
        for (i in 0..13) {
            repository.completeTopic(mathTopicIds[i])
        }

        // Verify badge is still locked
        val midProgress = repository.userProgress.first() ?: UserProgress()
        assertFalse(midProgress.unlockedBadgesJson.contains("badge_math"))

        // Complete the 15th topic
        repository.completeTopic(mathTopicIds[14])

        // Verify badge is now unlocked!
        val finalProgress = repository.userProgress.first() ?: UserProgress()
        assertTrue(finalProgress.unlockedBadgesJson.contains("badge_math"))

        // Now test resetting/toggling subject to incomplete
        repository.toggleSubjectCompletion("math", completeAll = false)

        // Verify badge is locked again
        val resetProgress = repository.userProgress.first() ?: UserProgress()
        assertFalse(resetProgress.unlockedBadgesJson.contains("badge_math"))
        assertEquals("", resetProgress.completedTopicsJson)
    }

    @Test
    fun testResetAllProgress_clearsAllData() = runBlocking {
        // Initialize DB
        repository.populateMockData()

        // Insert a default progress row so addPoints can update it
        database.progressDao().insertOrUpdateProgress(UserProgress())

        // Earn some points and unlock some topics
        repository.addPoints(150)
        val allTopics = MockDataGenerator.getTopics()
        val mathTopicIds = allTopics.filter { it.subjectId == "math" }.map { it.id }
        
        repository.completeTopic(mathTopicIds[0])
        repository.completeTopic(mathTopicIds[1])
        
        // Unlock badge (just to verify)
        repository.toggleSubjectCompletion("math", completeAll = true)
        
        val progressBeforeReset = repository.userProgress.first() ?: UserProgress()
        assertTrue(progressBeforeReset.spherePoints > 0)
        assertTrue(progressBeforeReset.completedTopicsJson.isNotEmpty())
        assertTrue(progressBeforeReset.unlockedBadgesJson.contains("badge_math"))

        // Perform full reset
        repository.resetAllProgress()

        // Verify everything is cleared back to defaults
        val progressAfterReset = repository.userProgress.first() ?: UserProgress()
        assertEquals(0, progressAfterReset.spherePoints)
        assertEquals(0, progressAfterReset.streakCount)
        assertEquals(0L, progressAfterReset.lastActiveTime)
        assertEquals("", progressAfterReset.unlockedBadgesJson)
        assertEquals("", progressAfterReset.completedTopicsJson)
        assertEquals("", progressAfterReset.completedQuizzesJson)
    }
}
