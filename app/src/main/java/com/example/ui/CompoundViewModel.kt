package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CompoundViewModel(application: Application) : AndroidViewModel(application) {
    private val database = CompoundDatabase.getDatabase(application)
    private val repository = CompoundRepository(
        taskDao = database.taskDao(),
        journalDao = database.journalDao(),
        focusSessionDao = database.focusSessionDao(),
        dayProgressDao = database.dayProgressDao(),
        settingsDao = database.settingsDao()
    )

    // Current navigation tab state
    private val _currentTab = MutableStateFlow("DASHBOARD")
    val currentTab: StateFlow<String> = _currentTab.asStateFlow()

    // UI Feedback state (Toast replacement)
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    fun showToast(msg: String) {
        _toastMessage.value = msg
    }
    fun clearToast() {
        _toastMessage.value = null
    }

    fun selectTab(tab: String) {
        _currentTab.value = tab
    }

    // Database flows
    val allTasks = repository.allTasks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allJournalEntries = repository.allJournalEntries.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allFocusSessions = repository.allFocusSessions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allDayProgress = repository.allDayProgress.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val settings = repository.settings
        .map { it ?: AppSetting() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppSetting()
        )

    // Active screen transient states (e.g., active task for Focus Screen)
    private val _selectedFocusTask = MutableStateFlow<Task?>(null)
    val selectedFocusTask: StateFlow<Task?> = _selectedFocusTask.asStateFlow()

    fun setSelectedFocusTask(task: Task?) {
        _selectedFocusTask.value = task
    }

    // Initialization checks
    init {
        viewModelScope.launch {
            // Check if 90 days are set; if database is empty, seed initial 90 empty days
            repository.allDayProgress.first().let { days ->
                if (days.isEmpty()) {
                    repository.resetAllData()
                }
            }
        }
    }

    // Dynamic metrics
    val totalXP: Flow<Int> = combine(allTasks, allFocusSessions, allDayProgress) { tasks, sessions, days ->
        val taskXp = tasks.filter { it.isCompleted }.sumOf { it.xpReward }
        val focusXp = sessions.sumOf { it.xpEarned }
        val dayXp = days.sumOf {
            when (it.status) {
                "BEAST_MODE" -> 300
                "COMPLETED" -> 150
                "LOW_DAY" -> 50
                else -> 0
            }
        }
        taskXp + focusXp + dayXp
    }

    val currentLevel: Flow<Int> = totalXP.map { xp ->
        1 + (xp / 1000)
    }

    val focusHours: Flow<Double> = allFocusSessions.map { sessions ->
        sessions.sumOf { it.durationMinutes } / 60.0
    }

    val beastDays: Flow<Int> = allDayProgress.map { days ->
        days.count { it.status == "BEAST_MODE" }
    }

    val completedDaysCount: Flow<Int> = allDayProgress.map { days ->
        days.count { it.status == "COMPLETED" || it.status == "BEAST_MODE" }
    }

    val currentStreak: Flow<Int> = allDayProgress.map { days ->
        if (days.isEmpty()) return@map 0
        // Find the streak counting back from today
        // For a clean formulation, let's identify how many consecutive completed days there are matching backwards
        val sortedDays = days.sortedBy { it.dayNumber }
        var maxCompletedDay = 0
        for (i in sortedDays.indices) {
            val d = sortedDays[i]
            if (d.status == "COMPLETED" || d.status == "BEAST_MODE" || d.status == "LOW_DAY") {
                maxCompletedDay = d.dayNumber
            }
        }
        if (maxCompletedDay == 0) return@map 0
        
        var count = 0
        var currentDayToCheck = maxCompletedDay
        while (currentDayToCheck > 0) {
            val dayObj = sortedDays.find { it.dayNumber == currentDayToCheck }
            if (dayObj != null && (dayObj.status == "COMPLETED" || dayObj.status == "BEAST_MODE" || dayObj.status == "LOW_DAY")) {
                count++
                currentDayToCheck--
            } else {
                break
            }
        }
        count
    }

    // CRUD - Tasks
    fun addTask(title: String, priority: String, category: String) {
        viewModelScope.launch {
            if (title.isBlank()) {
                showToast("Mission objective cannot be empty.")
                return@launch
            }
            val newTask = Task(title = title, priority = priority, category = category)
            repository.insertTask(newTask)
            showToast("New mission locked of category '$category'.")
        }
    }

    fun updateTaskCompletion(task: Task, completed: Boolean) {
        viewModelScope.launch {
            val updated = task.copy(
                isCompleted = completed,
                dateCompleted = if (completed) System.currentTimeMillis() else null
            )
            repository.updateTask(updated)
            if (completed) {
                showToast("Mission completed. +${task.xpReward} XP secured!")
            } else {
                showToast("Mission re-opened.")
            }
        }
    }

    fun editTask(task: Task, title: String, priority: String, category: String) {
        viewModelScope.launch {
            if (title.isBlank()) {
                showToast("Mission title cannot be empty.")
                return@launch
            }
            val updated = task.copy(title = title, priority = priority, category = category)
            repository.updateTask(updated)
            showToast("Mission parameter updated.")
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.deleteTask(task)
            showToast("Mission purged.")
        }
    }

    // CRUD - Journal
    fun addJournalEntry(
        date: String,
        shipped: String,
        blockers: String,
        improvements: String,
        lessons: String,
        mood: String,
        energy: Int,
        wins: String
    ) {
        viewModelScope.launch {
            val entry = JournalEntry(
                date = date,
                reflectionWhatShipped = shipped,
                reflectionBlocker = blockers,
                reflectionImprovement = improvements,
                lessonsLearned = lessons,
                mood = mood,
                energyLevel = energy,
                wins = wins
            )
            repository.insertJournalEntry(entry)
            showToast("Tactical record logged in archive.")
        }
    }

    fun editJournalEntry(entry: JournalEntry) {
        viewModelScope.launch {
            repository.updateJournalEntry(entry)
            showToast("Tactical log entry updated.")
        }
    }

    fun deleteJournalEntry(entry: JournalEntry) {
        viewModelScope.launch {
            repository.deleteJournalEntry(entry)
            showToast("Tactical log purged from mainframe.")
        }
    }

    // CRUD - Focus Session
    fun logFocusSession(missionName: String, durationMinutes: Int, xpEarned: Int) {
        viewModelScope.launch {
            val session = FocusSession(
                missionName = missionName,
                durationMinutes = durationMinutes,
                xpEarned = xpEarned
            )
            repository.insertFocusSession(session)
            showToast("Focus threshold unlocked. +$xpEarned XP generated!")
        }
    }

    // Day Progress Update
    fun updateDayStatus(dayNumber: Int, status: String) {
        viewModelScope.launch {
            val existing = allDayProgress.value.find { it.dayNumber == dayNumber }
            val phase = when (dayNumber) {
                in 1..22 -> "Foundation"
                in 23..45 -> "Product Completion"
                in 46..68 -> "Expansion"
                else -> "Placement Weaponization"
            }
            val day = DayProgress(dayNumber = dayNumber, status = status, phase = phase)
            repository.updateDayProgress(day)
            
            val feedback = when (status) {
                "BEAST_MODE" -> "BEAST MODE unlocked for Day $dayNumber! +300 XP."
                "COMPLETED" -> "Day $dayNumber status: Regular Execution. +150 XP."
                "LOW_DAY" -> "Low Day Survival logged for Day $dayNumber. Resistance compounds."
                else -> "Day $dayNumber state cleared to zero."
            }
            showToast(feedback)
        }
    }

    // Settings Updating
    fun updateQuotePreset(quotePreset: String) {
        viewModelScope.launch {
            val currentSettings = settings.value
            repository.saveSettings(currentSettings.copy(selectedQuotePreset = quotePreset))
            showToast("Quote configuration saved.")
        }
    }

    fun updateExamTargetDate(targetDate: String) {
        viewModelScope.launch {
            val currentSettings = settings.value
            repository.saveSettings(currentSettings.copy(examTargetDate = targetDate))
            showToast("Target deadline updated.")
        }
    }

    fun resetAllData() {
        viewModelScope.launch {
            repository.resetAllData()
            _selectedFocusTask.value = null
            showToast("Bhava 3.0 has been reset.")
        }
    }
}
