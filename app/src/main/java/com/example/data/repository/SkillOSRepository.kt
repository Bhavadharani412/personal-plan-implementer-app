package com.example.data.repository

import com.example.data.database.SkillOSDatabase
import com.example.data.entity.*
import kotlinx.coroutines.flow.Flow

class SkillOSRepository(private val database: SkillOSDatabase) {

    // Streams
    val plans: Flow<List<Plan>> = database.planDao().getAllPlans()
    val categories: Flow<List<Category>> = database.categoryDao().getAllCategories()
    val subjects: Flow<List<Subject>> = database.subjectDao().getAllSubjects()
    val topics: Flow<List<Topic>> = database.topicDao().getAllTopics()
    val sessions: Flow<List<Session>> = database.sessionDao().getAllSessions()
    val goals: Flow<List<Goal>> = database.goalDao().getAllGoals()
    val activeTimerFlow: Flow<ActiveTimer?> = database.activeTimerDao().getActiveTimerFlow()

    // Plan CRUD
    suspend fun insertPlan(plan: Plan) {
        database.planDao().insertPlan(plan)
    }

    suspend fun deletePlan(planId: Int) {
        database.planDao().softDeletePlan(planId)
    }

    // Category CRUD
    suspend fun insertCategory(category: Category) {
        database.categoryDao().insertCategory(category)
    }

    suspend fun deleteCategory(catId: Int) {
        database.categoryDao().softDeleteCategory(catId)
    }

    // Subject CRUD
    suspend fun insertSubject(subject: Subject) {
        database.subjectDao().insertSubject(subject)
    }

    suspend fun deleteSubject(subjectId: Int) {
        database.subjectDao().softDeleteSubject(subjectId)
    }

    // Topic CRUD
    suspend fun insertTopic(topic: Topic) {
        database.topicDao().insertTopic(topic)
    }

    suspend fun deleteTopic(topicId: Int) {
        database.topicDao().softDeleteTopic(topicId)
    }

    fun getTopicsForSubject(subjectId: Int): Flow<List<Topic>> {
        return database.topicDao().getTopicsForSubject(subjectId)
    }

    // Resource CRUD
    fun getResourcesForTopic(topicId: Int): Flow<List<Resource>> {
        return database.resourceDao().getResourcesForTopic(topicId)
    }

    suspend fun insertResource(resource: Resource) {
        database.resourceDao().insertResource(resource)
    }

    suspend fun deleteResource(id: Int) {
        database.resourceDao().softDeleteResource(id)
    }

    // Note CRUD
    fun getNoteForTopic(topicId: Int): Flow<Note?> {
        return database.noteDao().getNoteForTopic(topicId)
    }

    suspend fun insertNote(note: Note) {
        database.noteDao().insertNote(note)
    }

    // Session CRUD
    suspend fun insertSession(session: Session) {
        database.sessionDao().insertSession(session)
    }

    suspend fun deleteSession(id: Int) {
        database.sessionDao().softDeleteSession(id)
    }

    // Goal CRUD
    suspend fun insertGoal(goal: Goal) {
        database.goalDao().insertGoal(goal)
    }

    suspend fun deleteGoal(id: Int) {
        database.goalDao().deleteGoal(id)
    }

    // Active Timer CRUD
    suspend fun getActiveTimer(): ActiveTimer? {
        return database.activeTimerDao().getActiveTimer()
    }

    suspend fun saveActiveTimer(timer: ActiveTimer) {
        database.activeTimerDao().insertActiveTimer(timer)
    }

    suspend fun clearActiveTimer() {
        database.activeTimerDao().clearActiveTimer()
    }
}
