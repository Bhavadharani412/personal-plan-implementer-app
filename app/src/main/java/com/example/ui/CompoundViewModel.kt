package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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

    // Category-wise Hour tracking flow
    val categoryHours: Flow<Map<String, Double>> = allFocusSessions.map { sessions ->
        val map = mutableMapOf<String, Double>()
        val defaultCategories = listOf("Dentist PWA", "DSA", "Core Engineering", "AI Learning", "Cybersecurity", "Journal")
        defaultCategories.forEach { map[it] = 0.0 }
        
        sessions.forEach { session ->
            val cat = if (session.category.isNotBlank()) session.category else "DSA"
            val hrs = session.durationMinutes / 60.0
            map[cat] = (map[cat] ?: 0.0) + hrs
        }
        map
    }

    // Indestructible permanent XP integration
    val totalXP: Flow<Int> = settings.map { it.earnedXp }

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
        val sortedDays = days.sortedBy { it.dayNumber }
        var count = 0
        for (day in sortedDays) {
            if (day.status == "COMPLETED" || day.status == "BEAST_MODE" || day.status == "LOW_DAY") {
                count++
            } else {
                break
            }
        }
        count
    }

    // Background study timer engine
    private val _isTimerActive = MutableStateFlow(false)
    val isTimerActive: StateFlow<Boolean> = _isTimerActive.asStateFlow()

    private val _countdownSeconds = MutableStateFlow(50 * 60)
    val countdownSeconds: StateFlow<Int> = _countdownSeconds.asStateFlow()

    private val _selectedSessionMinutes = MutableStateFlow(50)
    val selectedSessionMinutes: StateFlow<Int> = _selectedSessionMinutes.asStateFlow()

    private val _selectedTimerCategory = MutableStateFlow("DSA")
    val selectedTimerCategory: StateFlow<String> = _selectedTimerCategory.asStateFlow()

    private var timerJob: Job? = null

    fun startTimer() {
        if (_isTimerActive.value) return
        _isTimerActive.value = true
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_isTimerActive.value && _countdownSeconds.value > 0) {
                delay(1000)
                _countdownSeconds.value -= 1
            }
            if (_isTimerActive.value && _countdownSeconds.value == 0) {
                _isTimerActive.value = false
                val selectedMin = _selectedSessionMinutes.value
                val xpReward = selectedMin * 10
                val activeTask = _selectedFocusTask.value
                val categoryName = activeTask?.category ?: _selectedTimerCategory.value

                logFocusSession(
                    missionName = activeTask?.title ?: "Focus block",
                    durationMinutes = selectedMin,
                    xpEarned = xpReward,
                    category = categoryName
                )

                if (activeTask != null) {
                    updateTaskCompletion(activeTask, true)
                }

                advanceStreakOnActivity()
                _countdownSeconds.value = selectedMin * 60
            }
        }
    }

    fun pauseTimer() {
        _isTimerActive.value = false
        timerJob?.cancel()
        timerJob = null
    }

    fun cancelTimer() {
        _isTimerActive.value = false
        timerJob?.cancel()
        timerJob = null
        _countdownSeconds.value = _selectedSessionMinutes.value * 60
    }

    fun setSessionMinutes(minutes: Int) {
        if (!_isTimerActive.value) {
            _selectedSessionMinutes.value = minutes
            _countdownSeconds.value = minutes * 60
        }
    }

    fun rollSmartSessionMinutes(task: Task?) {
        if (_isTimerActive.value) return
        
        val calendar = java.util.Calendar.getInstance()
        val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
        
        val minMinutes: Int
        val maxMinutes: Int
        val reason: String

        if (task != null) {
            when (task.priority.uppercase()) {
                "HIGH" -> {
                    minMinutes = 65
                    maxMinutes = 110
                    reason = "High Priority task demands high-intensity study"
                }
                "MEDIUM" -> {
                    minMinutes = 40
                    maxMinutes = 75
                    reason = "Medium Priority task recommends balanced study blocks"
                }
                else -> {
                    minMinutes = 15
                    maxMinutes = 35
                    reason = "Low Priority task warrants a quick, sharp burst"
                }
            }
        } else {
            when (hour) {
                in 5..11 -> {
                    minMinutes = 45
                    maxMinutes = 90
                    reason = "Morning focus peak favors deeper study runs"
                }
                in 12..17 -> {
                    minMinutes = 30
                    maxMinutes = 70
                    reason = "Midday cognitive cycle favors standard blocks"
                }
                else -> {
                    minMinutes = 20
                    maxMinutes = 55
                    reason = "Evening recovery phase favors tactical sprint speeds"
                }
            }
        }

        // Generate dynamic organic number (like 84, 67, 35, 23, 90)
        val rolled = (minMinutes..maxMinutes).random()
        setSessionMinutes(rolled)
        showToast("🎲 Dynamic hour rolled: ${rolled}m! $reason.")
    }

    fun setSelectedTimerCategory(category: String) {
        _selectedTimerCategory.value = category
    }

    // Initialization: Seed database and retroactively calculate base XP if needed
    init {
        viewModelScope.launch {
            // Check if 90 days are set; if database is empty, seed initial 90 empty days
            val days = repository.allDayProgress.first()
            if (days.isEmpty()) {
                repository.resetAllData()
            }

            // Sync base XP on initial startup to protect existing history
            val currentSettings = settings.first()
            if (currentSettings.earnedXp == 0) {
                val tasks = repository.allTasks.first()
                val sessions = repository.allFocusSessions.first()
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
                val totalBase = taskXp + focusXp + dayXp
                if (totalBase > 0) {
                    repository.saveSettings(currentSettings.copy(earnedXp = totalBase))
                }
            }
        }
    }

    fun addPermanentXp(amount: Int) {
        viewModelScope.launch {
            val currentSettings = settings.value
            val newXp = currentSettings.earnedXp + amount
            repository.saveSettings(currentSettings.copy(earnedXp = newXp))
        }
    }

    fun advanceStreakOnActivity() {
        viewModelScope.launch {
            val days = repository.allDayProgress.first()
            if (days.isEmpty()) return@launch
            val sorted = days.sortedBy { it.dayNumber }
            val firstUnattempted = sorted.find { it.status == "UNATTEMPTED" }
            if (firstUnattempted != null) {
                val updated = firstUnattempted.copy(
                    status = "COMPLETED",
                    updatedTimestamp = System.currentTimeMillis()
                )
                repository.updateDayProgress(updated)
                addPermanentXp(150)
                showToast("Missions aligned! Progress Day ${firstUnattempted.dayNumber} of 90 checkmarked.")
            }
        }
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
            if (task.isCompleted == completed) return@launch
            val updated = task.copy(
                isCompleted = completed,
                dateCompleted = if (completed) System.currentTimeMillis() else null
            )
            repository.updateTask(updated)
            if (completed) {
                addPermanentXp(task.xpReward)
                showToast("Mission completed! +${task.xpReward} XP secured.")
                advanceStreakOnActivity()
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
            addPermanentXp(100) // Journal adds 100 XP
            showToast("Tactical record logged in archive. +100 XP.")
            advanceStreakOnActivity()
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

    // Logging focus sessions directly
    fun logFocusSession(missionName: String, durationMinutes: Int, xpEarned: Int, category: String = "General") {
        viewModelScope.launch {
            val session = FocusSession(
                missionName = missionName,
                durationMinutes = durationMinutes,
                xpEarned = xpEarned,
                category = category
            )
            repository.insertFocusSession(session)
            addPermanentXp(xpEarned)
            showToast("Focus threshold unlocked. +$xpEarned XP generated!")
        }
    }

    // Day Progress Update
    fun updateDayStatus(dayNumber: Int, status: String) {
        viewModelScope.launch {
            val existing = allDayProgress.value.find { it.dayNumber == dayNumber }
            val existingStatus = existing?.status ?: "UNATTEMPTED"
            if (existingStatus == status) return@launch

            val phase = when (dayNumber) {
                in 1..22 -> "Foundation"
                in 23..45 -> "Product Completion"
                in 46..68 -> "Expansion"
                else -> "Placement Weaponization"
            }
            val day = DayProgress(dayNumber = dayNumber, status = status, phase = phase)
            repository.updateDayProgress(day)

            val xpGain = when (status) {
                "BEAST_MODE" -> 300
                "COMPLETED" -> 150
                "LOW_DAY" -> 50
                else -> 0
            }
            if (xpGain > 0) {
                addPermanentXp(xpGain)
            }

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
            _isTimerActive.value = false
            timerJob?.cancel()
            timerJob = null
            _countdownSeconds.value = 50 * 60
            _selectedSessionMinutes.value = 50
            showToast("Bhava 3.0 has been reset.")
        }
    }
}
