package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.SkillOSDatabase
import com.example.data.entity.*
import com.example.data.repository.SkillOSRepository
import kotlinx.coroutines.Delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.TimeUnit

class SkillOSViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SkillOSRepository
    private var timerJob: Job? = null

    // Base databases streams
    val plans: StateFlow<List<Plan>>
    val categories: StateFlow<List<Category>>
    val subjects: StateFlow<List<Subject>>
    val topics: StateFlow<List<Topic>>
    val sessions: StateFlow<List<Session>>
    val goals: StateFlow<List<Goal>>
    val activeTimer: StateFlow<ActiveTimer?>

    // Running UI timer details
    private val _timerDisplaySeconds = MutableStateFlow(0L)
    val timerDisplaySeconds: StateFlow<Long> = _timerDisplaySeconds.asStateFlow()

    init {
        val db = SkillOSDatabase.getDatabase(application, viewModelScope)
        repository = SkillOSRepository(db)

        plans = repository.plans
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        categories = repository.categories
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        subjects = repository.subjects
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        topics = repository.topics
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        sessions = repository.sessions
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        goals = repository.goals
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        activeTimer = repository.activeTimerFlow
            .stateIn(viewModelScope, SharingStarted.Eagerly, null)

        // Observe active timer database state to sync/drive background ticking
        viewModelScope.launch {
            activeTimer.collect { timer ->
                if (timer != null) {
                    startTimerTicker(timer)
                } else {
                    stopTimerTicker()
                }
            }
        }
    }

    // --- TIMER SYSTEM ENGINE ---
    private fun startTimerTicker(timer: ActiveTimer) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch(Dispatchers.Default) {
            while (true) {
                val now = System.currentTimeMillis()
                val totalMs = if (timer.isPaused) {
                    timer.lastPauseTimestamp - timer.startTime - timer.pausedDurationMs
                } else {
                    val pausedDiff = if (timer.lastPauseTimestamp > 0) {
                        now - timer.lastPauseTimestamp
                    } else 0L
                    now - timer.startTime - timer.pausedDurationMs - pausedDiff
                }
                _timerDisplaySeconds.value = maxOf(0L, totalMs / 1000L)
                delay(1000L)
            }
        }
    }

    private fun stopTimerTicker() {
        timerJob?.cancel()
        timerJob = null
        _timerDisplaySeconds.value = 0L
    }

    fun startSession(topicId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val timer = ActiveTimer(
                id = 1,
                topicId = topicId,
                startTime = System.currentTimeMillis(),
                isPaused = false,
                pausedDurationMs = 0,
                lastPauseTimestamp = 0
            )
            repository.saveActiveTimer(timer)
        }
    }

    fun pauseSession() {
        viewModelScope.launch(Dispatchers.IO) {
            val current = repository.getActiveTimer() ?: return@launch
            if (!current.isPaused) {
                val updated = current.copy(
                    isPaused = true,
                    lastPauseTimestamp = System.currentTimeMillis()
                )
                repository.saveActiveTimer(updated)
            }
        }
    }

    fun resumeSession() {
        viewModelScope.launch(Dispatchers.IO) {
            val current = repository.getActiveTimer() ?: return@launch
            if (current.isPaused) {
                val now = System.currentTimeMillis()
                val pauseDelta = now - current.lastPauseTimestamp
                val updated = current.copy(
                    isPaused = false,
                    pausedDurationMs = current.pausedDurationMs + pauseDelta,
                    lastPauseTimestamp = 0L
                )
                repository.saveActiveTimer(updated)
            }
        }
    }

    fun completeActiveSession(markTopicCompleted: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            val timer = repository.getActiveTimer() ?: return@launch
            val now = System.currentTimeMillis()
            val finalPausedMs = if (timer.isPaused) {
                timer.pausedDurationMs + (now - timer.lastPauseTimestamp)
            } else {
                timer.pausedDurationMs
            }
            val elapsedMs = now - timer.startTime - finalPausedMs
            val elapsedMinutes = maxOf(1, (elapsedMs / (1000L * 60L)).toInt())

            // Create saved Session record
            val session = Session(
                topicId = timer.topicId,
                startTime = timer.startTime,
                endTime = now,
                durationMinutes = elapsedMinutes
            )
            repository.insertSession(session)

            // Auto complete original topic if requested
            if (markTopicCompleted) {
                val allTopicsList = topics.value
                val topic = allTopicsList.find { it.id == timer.topicId }
                if (topic != null) {
                    repository.insertTopic(topic.copy(isCompleted = true, updatedAt = now))
                }
            }

            // Remove/reset timer DB singleton entry
            repository.clearActiveTimer()
        }
    }

    fun cancelActiveSession() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearActiveTimer()
        }
    }

    // --- PLAN CRUD OPERATIONS ---
    fun addPlan(name: String, startDateMs: Long, endDateMs: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val plan = Plan(
                name = name,
                startDate = startDateMs,
                endDate = endDateMs
            )
            repository.insertPlan(plan)
        }
    }

    fun softDeletePlan(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deletePlan(id)
        }
    }

    // --- CATEGORY CRUD OPERATIONS ---
    fun addCategory(name: String, colorHex: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val category = Category(name = name, color = colorHex)
            repository.insertCategory(category)
        }
    }

    fun softDeleteCategory(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteCategory(id)
        }
    }

    // --- SUBJECT CRUD OPERATIONS ---
    fun addSubject(name: String, categoryId: Int, icon: String = "book") {
        viewModelScope.launch(Dispatchers.IO) {
            val subject = Subject(name = name, categoryId = categoryId, icon = icon)
            repository.insertSubject(subject)
        }
    }

    fun softDeleteSubject(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteSubject(id)
        }
    }

    // --- TOPIC CRUD OPERATIONS ---
    fun addTopic(name: String, subjectId: Int, description: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val topic = Topic(name = name, subjectId = subjectId, description = description)
            repository.insertTopic(topic)
        }
    }

    fun setTopicCompleted(id: Int, isCompleted: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val list = topics.value
            val current = list.find { it.id == id } ?: return@launch
            repository.insertTopic(current.copy(isCompleted = isCompleted, updatedAt = System.currentTimeMillis()))
        }
    }

    fun softDeleteTopic(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteTopic(id)
        }
    }

    // --- RESOURCE CRUD OPERATIONS ---
    fun addResource(topicId: Int, title: String, url: String, type: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val resource = Resource(topicId = topicId, title = title, url = url, type = type)
            repository.insertResource(resource)
        }
    }

    fun softDeleteResource(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteResource(id)
        }
    }

    // --- NOTE CRUD OPERATIONS ---
    fun saveNote(topicId: Int, noteId: Int, content: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val note = Note(id = if (noteId == 0) 0 else noteId, topicId = topicId, content = content)
            repository.insertNote(note)
        }
    }

    fun getNoteFlowForTopic(topicId: Int): Flow<Note?> = repository.getNoteForTopic(topicId)
    fun getResourcesFlowForTopic(topicId: Int): Flow<List<Resource>> = repository.getResourcesForTopic(topicId)

    // --- GOAL CRUD OPERATIONS ---
    fun addGoal(subjectId: Int, targetMinutes: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val goal = Goal(subjectId = subjectId, targetMinutes = targetMinutes)
            repository.insertGoal(goal)
        }
    }

    fun deleteGoal(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteGoal(id)
        }
    }

    // --- STREAK ENGINE ---
    // Computes current streak based on 30 minutes daily UTC threshold mapped to user system local timeline
    fun calculateCurrentStreak(thresholdMins: Int = 30): Int {
        val sList = sessions.value.filter { !it.isDeleted }
        if (sList.isEmpty()) return 0
        val localZone = ZoneId.systemDefault()

        val durationByDate = mutableMapOf<LocalDate, Int>()
        for (session in sList) {
            val localDate = Instant.ofEpochMilli(session.startTime)
                .atZone(localZone)
                .toLocalDate()
            durationByDate[localDate] = (durationByDate[localDate] ?: 0) + session.durationMinutes
        }

        var streak = 0
        var checkDate = LocalDate.now()

        val todayMins = durationByDate[checkDate] ?: 0
        if (todayMins >= thresholdMins) {
            streak++
            checkDate = checkDate.minusDays(1)
            while (true) {
                val mins = durationByDate[checkDate] ?: 0
                if (mins >= thresholdMins) {
                    streak++
                    checkDate = checkDate.minusDays(1)
                } else {
                    break
                }
            }
        } else {
            val yesterday = checkDate.minusDays(1)
            val yesterdayMins = durationByDate[yesterday] ?: 0
            if (yesterdayMins >= thresholdMins) {
                checkDate = yesterday
                while (true) {
                    val mins = durationByDate[checkDate] ?: 0
                    if (mins >= thresholdMins) {
                        streak++
                        checkDate = checkDate.minusDays(1)
                    } else {
                        break
                    }
                }
            } else {
                streak = 0
            }
        }
        return streak
    }

    // --- HEATMAP GENERATION ENGINE ---
    // Returns list of pairs (LocalDate, minutesInvested) for the past 140 days
    fun getHeatmapData(
        filterType: String = "Overall", // "Overall", "Category", "Subject", "Topic"
        filterId: Int = 0
    ): Map<LocalDate, Int> {
        val sList = sessions.value.filter { !it.isDeleted }
        val allTopics = topics.value
        val allSubjects = subjects.value
        val localZone = ZoneId.systemDefault()

        val filteredSessions = sList.filter { session ->
            when (filterType) {
                "Overall" -> true
                "Topic" -> session.topicId == filterId
                "Subject" -> {
                    val topicObj = allTopics.find { it.id == session.topicId }
                    topicObj != null && topicObj.subjectId == filterId
                }
                "Category" -> {
                    val topicObj = allTopics.find { it.id == session.topicId }
                    val subObj = if (topicObj != null) allSubjects.find { it.id == topicObj.subjectId } else null
                    subObj != null && subObj.categoryId == filterId
                }
                else -> true
            }
        }

        val map = mutableMapOf<LocalDate, Int>()
        for (session in filteredSessions) {
            val date = Instant.ofEpochMilli(session.startTime).atZone(localZone).toLocalDate()
            map[date] = (map[date] ?: 0) + session.durationMinutes
        }
        return map
    }

    // --- NEGLECT DETECTION SYSTEM ---
    data class NeglectWarning(
        val type: String, // "Subject" or "Category"
        val name: String,
        val idleDays: Int
    )

    fun getNeglectWarnings(): List<NeglectWarning> {
        val now = System.currentTimeMillis()
        val sList = sessions.value.filter { !it.isDeleted }
        val allTopics = topics.value
        val allSubjects = subjects.value.filter { !it.isDeleted }
        val allCategories = categories.value.filter { !it.isDeleted }

        val warnings = mutableListOf<NeglectWarning>()

        // Check Subject Inactivity (> 7 Days)
        for (subject in allSubjects) {
            // Find topics in this subject
            val sTopics = allTopics.filter { it.subjectId == subject.id && !it.isDeleted }.map { it.id }
            if (sTopics.isEmpty()) continue

            // Find sessions for these topics
            val sSessions = sList.filter { it.topicId in sTopics }
            if (sSessions.isEmpty()) {
                // Never started tracking - let's treat it as inactive based on creation date or default
                val days = TimeUnit.MILLISECONDS.toDays(now - subject.createdAt).toInt()
                if (days >= 7) {
                    warnings.add(NeglectWarning("Subject", subject.name, days))
                }
            } else {
                val lastActivity = sSessions.maxOf { it.endTime }
                val idleMs = now - lastActivity
                val idleDays = TimeUnit.MILLISECONDS.toDays(idleMs).toInt()
                if (idleDays >= 7) {
                    warnings.add(NeglectWarning("Subject", subject.name, idleDays))
                }
            }
        }

        // Check Category Inactivity (> 7 Days)
        for (cat in allCategories) {
            val catSubs = allSubjects.filter { it.categoryId == cat.id }.map { it.id }
            val catTopics = allTopics.filter { it.subjectId in catSubs && !it.isDeleted }.map { it.id }
            val catSessions = sList.filter { it.topicId in catTopics }

            if (catSessions.isEmpty()) {
                val days = TimeUnit.MILLISECONDS.toDays(now - cat.createdAt).toInt()
                if (days >= 7) {
                    warnings.add(NeglectWarning("Category", cat.name, days))
                }
            } else {
                val lastActivity = catSessions.maxOf { it.endTime }
                val idleMs = now - lastActivity
                val idleDays = TimeUnit.MILLISECONDS.toDays(idleMs).toInt()
                if (idleDays >= 7) {
                    warnings.add(NeglectWarning("Category", cat.name, idleDays))
                }
            }
        }

        return warnings.sortedByDescending { it.idleDays }
    }

    // --- SMART INSIGHTS ---
    data class SmartInsight(
        val title: String,
        val value: String,
        val description: String,
        val icon: String
    )

    fun getSmartInsights(): List<SmartInsight> {
        val sList = sessions.value.filter { !it.isDeleted }
        val allTopics = topics.value
        val allSubjects = subjects.value
        val allGoals = goals.value
        val insights = mutableListOf<SmartInsight>()

        if (sList.isEmpty()) {
            return listOf(
                SmartInsight(
                    "Welcome Creator",
                    "0m Invested",
                    "Three simple taps is all it takes to record your first deep focus timer session.",
                    "star"
                )
            )
        }

        // 1. Most Focused Subject
        val minsBySubject = mutableMapOf<Int, Int>()
        for (session in sList) {
            val topicObj = allTopics.find { it.id == session.topicId }
            if (topicObj != null) {
                minsBySubject[topicObj.subjectId] = (minsBySubject[topicObj.subjectId] ?: 0) + session.durationMinutes
            }
        }

        var mostFocusedSub: Subject? = null
        var maxMins = 0
        var leastFocusedSub: Subject? = null
        var minMins = Int.MAX_VALUE

        for ((subId, mins) in minsBySubject) {
            val subObj = allSubjects.find { it.id == subId && !it.isDeleted }
            if (subObj != null) {
                if (mins > maxMins) {
                    maxMins = mins
                    mostFocusedSub = subObj
                }
                if (mins < minMins) {
                    minMins = mins
                    leastFocusedSub = subObj
                }
            }
        }

        if (mostFocusedSub != null) {
            val hours = String.format("%.1fh", maxMins / 60.0)
            insights.add(
                SmartInsight(
                    "Most Focused Area",
                    mostFocusedSub.name,
                    "You invested $hours of deep focus into this subject.",
                    "trending_up"
                )
            )
        }

        if (leastFocusedSub != null && leastFocusedSub.id != mostFocusedSub?.id) {
            val hours = String.format("%.1fh", minMins / 60.0)
            insights.add(
                SmartInsight(
                    "Velocity Review Needed",
                    leastFocusedSub.name,
                    "Minimal tracking recorded highlights standard neglect. Trailing at $hours.",
                    "trending_down"
                )
            )
        }

        // 2. Schedule Tracking (Behind vs Ahead)
        // Match Goals vs. Actual session minutes in current week
        val startOfWeek = LocalDate.now().minusDays(7)
        val endOfWeek = LocalDate.now()
        val localZone = ZoneId.systemDefault()

        val weekSessions = sList.filter {
            val d = Instant.ofEpochMilli(it.startTime).atZone(localZone).toLocalDate()
            !d.isBefore(startOfWeek) && !d.isAfter(endOfWeek)
        }

        var totalGoalMins = 0
        for (goal in allGoals) {
            totalGoalMins += goal.targetMinutes
        }

        val totalActualMins = weekSessions.sumOf { it.durationMinutes }
        if (totalGoalMins > 0) {
            val diffMins = totalActualMins - totalGoalMins
            if (diffMins < 0) {
                val hoursBehind = String.format("%.1fh", Math.abs(diffMins) / 60.0)
                insights.add(
                    SmartInsight(
                        "Schedule Pace",
                        "Behind Goal Targets",
                        "You are $hoursBehind behind your weekly target thresholds. Tap in a quick sprint!",
                        "warning"
                    )
                )
            } else {
                val hoursAhead = String.format("%.1fh", diffMins / 60.0)
                insights.add(
                    SmartInsight(
                        "Schedule Pace",
                        "Ahead of Schedule",
                        "Outstanding consistency! You are $hoursAhead ahead of weekly goals.",
                        "check_circle"
                    )
                )
            }
        }

        // 3. Longest Streak
        val longest = calculateLongestStreak()
        insights.add(
            SmartInsight(
                "Peak Performance",
                "$longest Day Streak",
                "Your all-time longest streak maintaining > 30 minutes focus per day.",
                "local_fire_department"
            )
        )

        return insights
    }

    private fun calculateLongestStreak(thresholdMins: Int = 30): Int {
        val sList = sessions.value.filter { !it.isDeleted }
        if (sList.isEmpty()) return 0
        val localZone = ZoneId.systemDefault()

        val dates = sList.map {
            Instant.ofEpochMilli(it.startTime).atZone(localZone).toLocalDate()
        }.distinct().sorted()

        val durationByDate = mutableMapOf<LocalDate, Int>()
        for (session in sList) {
            val date = Instant.ofEpochMilli(session.startTime).atZone(localZone).toLocalDate()
            durationByDate[date] = (durationByDate[date] ?: 0) + session.durationMinutes
        }

        var maxStreak = 0
        var currentStreak = 0
        var prevDate: LocalDate? = null

        for (date in dates) {
            val mins = durationByDate[date] ?: 0
            if (mins >= thresholdMins) {
                if (prevDate == null || date == prevDate.plusDays(1)) {
                    currentStreak++
                } else if (date != prevDate) {
                    if (currentStreak > maxStreak) maxStreak = currentStreak
                    currentStreak = 1
                }
                prevDate = date
            } else {
                if (currentStreak > maxStreak) maxStreak = currentStreak
                currentStreak = 0
            }
        }
        if (currentStreak > maxStreak) maxStreak = currentStreak
        return maxStreak
    }

    // --- REPORT BUILDER ---
    data class ReportData(
        val title: String,
        val totalHours: Double,
        val sessionCount: Int,
        val activeTopicsCount: Int,
        val topCategory: String,
        val topSubject: String,
        val avgSessionMins: Double
    )

    fun generateReport(type: String): ReportData {
        // "Daily", "Weekly", "Monthly", "Plan"
        val now = LocalDate.now()
        val sList = sessions.value.filter { !it.isDeleted }
        val allTopics = topics.value
        val allSubjects = subjects.value
        val allCategories = categories.value
        val localZone = ZoneId.systemDefault()

        val filtered = sList.filter {
            val date = Instant.ofEpochMilli(it.startTime).atZone(localZone).toLocalDate()
            when (type) {
                "Daily" -> date == now
                "Weekly" -> !date.isBefore(now.minusDays(7))
                "Monthly" -> !date.isBefore(now.minusDays(30))
                else -> true // Entire history / current Plan
            }
        }

        val totalMinutes = filtered.sumOf { it.durationMinutes }
        val sessionCount = filtered.size
        val uniqueTopicsTracked = filtered.map { it.topicId }.distinct().size

        // Find Top category & subject
        val minsByCat = mutableMapOf<Int, Int>()
        val minsBySub = mutableMapOf<Int, Int>()

        for (session in filtered) {
            val t = allTopics.find { it.id == session.topicId } ?: continue
            minsBySub[t.subjectId] = (minsBySub[t.subjectId] ?: 0) + session.durationMinutes

            val s = allSubjects.find { it.id == t.subjectId } ?: continue
            minsByCat[s.categoryId] = (minsByCat[s.categoryId] ?: 0) + session.durationMinutes
        }

        val topCategoryName = minsByCat.maxByOrNull { it.value }?.key?.let { catId ->
            allCategories.find { it.id == catId }?.name
        } ?: "None"

        val topSubjectName = minsBySub.maxByOrNull { it.value }?.key?.let { subId ->
            allSubjects.find { it.id == subId }?.name
        } ?: "None"

        val avgMins = if (sessionCount > 0) totalMinutes.toDouble() / sessionCount else 0.0

        return ReportData(
            title = "$type Performance Audit",
            totalHours = totalMinutes / 60.0,
            sessionCount = sessionCount,
            activeTopicsCount = uniqueTopicsTracked,
            topCategory = topCategoryName,
            topSubject = topSubjectName,
            avgSessionMins = avgMins
        )
    }
}
