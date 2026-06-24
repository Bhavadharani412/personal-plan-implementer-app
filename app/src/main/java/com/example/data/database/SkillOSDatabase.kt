package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.*
import com.example.data.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Plan::class,
        Category::class,
        Subject::class,
        Topic::class,
        Resource::class,
        Note::class,
        Session::class,
        Goal::class,
        ActiveTimer::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SkillOSDatabase : RoomDatabase() {
    abstract fun planDao(): PlanDao
    abstract fun categoryDao(): CategoryDao
    abstract fun subjectDao(): SubjectDao
    abstract fun topicDao(): TopicDao
    abstract fun resourceDao(): ResourceDao
    abstract fun noteDao(): NoteDao
    abstract fun sessionDao(): SessionDao
    abstract fun goalDao(): GoalDao
    abstract fun activeTimerDao(): ActiveTimerDao

    companion object {
        @Volatile
        private var INSTANCE: SkillOSDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): SkillOSDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SkillOSDatabase::class.java,
                    "skillos_database"
                )
                .addCallback(SkillOSDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class SkillOSDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database)
                }
            }
        }

        suspend fun populateDatabase(db: SkillOSDatabase) {
            val catDao = db.categoryDao()
            val subDao = db.subjectDao()
            val topicDao = db.topicDao()
            val planDao = db.planDao()

            // Seed Categories
            val categories = listOf(
                Category(id = 1, name = "Learning", color = "#A3C1E2"),
                Category(id = 2, name = "Projects", color = "#47B5A8"),
                Category(id = 3, name = "Career", color = "#F7E289"),
                Category(id = 4, name = "Freelancing", color = "#FBB28B"),
                Category(id = 5, name = "Personal Growth", color = "#EA5E86")
            )
            for (category in categories) {
                catDao.insertCategory(category)
            }

            // Seed a default 90-Day Sprint Plan spanning from "now"
            val now = System.currentTimeMillis()
            val ninetyDaysInMillis = 90L * 24L * 60L * 60L * 1000L
            val defaultPlan = Plan(
                id = 1,
                name = "90 Day Full Stack Sprint",
                startDate = now,
                endDate = now + ninetyDaysInMillis,
                isCompleted = false
            )
            planDao.insertPlan(defaultPlan)

            // Seed Default Subjects
            // Category 1: Learning -> DSA, MERN
            val sub1 = Subject(id = 1, categoryId = 1, name = "MERN Stack", icon = "laptop")
            val sub2 = Subject(id = 2, categoryId = 1, name = "Data Structures & Algorithms", icon = "code")
            // Category 2: Projects -> HireFlow ATS, Portfolio
            val sub3 = Subject(id = 3, categoryId = 2, name = "HireFlow ATS", icon = "build")
            // Category 4: Freelancing -> Client Outreach
            val sub4 = Subject(id = 4, categoryId = 4, name = "Client Outreach", icon = "mail")
            // Category 3: Career -> Resume, LinkedIn
            val sub5 = Subject(id = 5, categoryId = 3, name = "Resume & Portfolio", icon = "description")

            subDao.insertSubject(sub1)
            subDao.insertSubject(sub2)
            subDao.insertSubject(sub3)
            subDao.insertSubject(sub4)
            subDao.insertSubject(sub5)

            // Seed Default Topics
            // MERN Stack topics
            topicDao.insertTopic(Topic(id = 1, subjectId = 1, name = "React Hooks", description = "Deep-dive into useState, useEffect, and custom hooks."))
            topicDao.insertTopic(Topic(id = 2, subjectId = 1, name = "Node.js Express API", description = "REST routing, middleware, and request validation."))
            // DSA topics
            topicDao.insertTopic(Topic(id = 3, subjectId = 2, name = "Sliding Window", description = "Fixed and variable window patterns for subarray problems."))
            topicDao.insertTopic(Topic(id = 4, subjectId = 2, name = "Two Pointers", description = "Optimizing search in sorted collections with twin cursors."))
            // Projects
            topicDao.insertTopic(Topic(id = 5, subjectId = 3, name = "Authentication Module", description = "Integrate secure sign-in with JWT and refresh tokens."))
            // Freelancing
            topicDao.insertTopic(Topic(id = 6, subjectId = 4, name = "Proposal Writing", description = "Crafting high-conversion proposal templates for clients."))
            // Career
            topicDao.insertTopic(Topic(id = 7, subjectId = 5, name = "Resume Revision", description = "Tailoring experience write-ups to highlight metric impacts."))
        }
    }
}
