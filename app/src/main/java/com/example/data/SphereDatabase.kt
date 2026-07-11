package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserProgress::class,
        SubjectEntity::class,
        TopicEntity::class,
        LessonEntity::class,
        QuizQuestionEntity::class,
        BadgeEntity::class
    ],
    version = 7,
    exportSchema = false
)
abstract class SphereDatabase : RoomDatabase() {
    abstract fun progressDao(): ProgressDao
    abstract fun sphereDao(): SphereDao

    companion object {
        @Volatile
        private var INSTANCE: SphereDatabase? = null

        fun getDatabase(context: Context): SphereDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SphereDatabase::class.java,
                    "sphere_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
