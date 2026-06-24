package com.example.data.dao

import androidx.room.*
import com.example.data.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PlanDao {
    @Query("SELECT * FROM plans WHERE isDeleted = 0 ORDER BY startDate DESC")
    fun getAllPlans(): Flow<List<Plan>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlan(plan: Plan)

    @Query("UPDATE plans SET isDeleted = 1, updatedAt = :timestamp WHERE id = :planId")
    suspend fun softDeletePlan(planId: Int, timestamp: Long = System.currentTimeMillis())
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE isDeleted = 0 ORDER BY name ASC")
    fun getAllCategories(): Flow<List<Category>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category)

    @Query("UPDATE categories SET isDeleted = 1, updatedAt = :timestamp WHERE id = :catId")
    suspend fun softDeleteCategory(catId: Int, timestamp: Long = System.currentTimeMillis())
}

@Dao
interface SubjectDao {
    @Query("SELECT * FROM subjects WHERE isDeleted = 0 ORDER BY name ASC")
    fun getAllSubjects(): Flow<List<Subject>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: Subject)

    @Query("UPDATE subjects SET isDeleted = 1, updatedAt = :timestamp WHERE id = :subjectId")
    suspend fun softDeleteSubject(subjectId: Int, timestamp: Long = System.currentTimeMillis())
}

@Dao
interface TopicDao {
    @Query("SELECT * FROM topics WHERE isDeleted = 0")
    fun getAllTopics(): Flow<List<Topic>>

    @Query("SELECT * FROM topics WHERE subjectId = :subjectId AND isDeleted = 0")
    fun getTopicsForSubject(subjectId: Int): Flow<List<Topic>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopic(topic: Topic)

    @Query("UPDATE topics SET isDeleted = 1, updatedAt = :timestamp WHERE id = :topicId")
    suspend fun softDeleteTopic(topicId: Int, timestamp: Long = System.currentTimeMillis())
}

@Dao
interface ResourceDao {
    @Query("SELECT * FROM resources WHERE topicId = :topicId AND isDeleted = 0")
    fun getResourcesForTopic(topicId: Int): Flow<List<Resource>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResource(resource: Resource)

    @Query("UPDATE resources SET isDeleted = 1, updatedAt = :timestamp WHERE id = :resourceId")
    suspend fun softDeleteResource(resourceId: Int, timestamp: Long = System.currentTimeMillis())
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE topicId = :topicId LIMIT 1")
    fun getNoteForTopic(topicId: Int): Flow<Note?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note)
}

@Dao
interface SessionDao {
    @Query("SELECT * FROM sessions WHERE isDeleted = 0 ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<Session>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: Session)

    @Query("UPDATE sessions SET isDeleted = 1, updatedAt = :timestamp WHERE id = :sessionId")
    suspend fun softDeleteSession(sessionId: Int, timestamp: Long = System.currentTimeMillis())
}

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals ORDER BY createdAt DESC")
    fun getAllGoals(): Flow<List<Goal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: Goal)

    @Query("DELETE FROM goals WHERE id = :goalId")
    suspend fun deleteGoal(goalId: Int)
}

@Dao
interface ActiveTimerDao {
    @Query("SELECT * FROM active_timer WHERE id = 1 LIMIT 1")
    suspend fun getActiveTimer(): ActiveTimer?

    @Query("SELECT * FROM active_timer WHERE id = 1 LIMIT 1")
    fun getActiveTimerFlow(): Flow<ActiveTimer?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActiveTimer(timer: ActiveTimer)

    @Query("DELETE FROM active_timer WHERE id = 1")
    suspend fun clearActiveTimer()
}
