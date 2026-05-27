package com.example.data

import kotlinx.coroutines.flow.Flow

class CompoundRepository(
    private val taskDao: TaskDao,
    private val journalDao: JournalDao,
    private val focusSessionDao: FocusSessionDao,
    private val dayProgressDao: DayProgressDao,
    private val settingsDao: AppSettingsDao
) {
    val allTasks: Flow<List<Task>> = taskDao.getAllTasks()
    val allJournalEntries: Flow<List<JournalEntry>> = journalDao.getAllEntries()
    val allFocusSessions: Flow<List<FocusSession>> = focusSessionDao.getAllSessions()
    val allDayProgress: Flow<List<DayProgress>> = dayProgressDao.getAllDays()
    val settings: Flow<AppSetting?> = settingsDao.getSettings()

    suspend fun insertTask(task: Task) = taskDao.insertTask(task)
    suspend fun updateTask(task: Task) = taskDao.updateTask(task)
    suspend fun deleteTask(task: Task) = taskDao.deleteTask(task)

    suspend fun insertJournalEntry(entry: JournalEntry) = journalDao.insertEntry(entry)
    suspend fun updateJournalEntry(entry: JournalEntry) = journalDao.updateEntry(entry)
    suspend fun deleteJournalEntry(entry: JournalEntry) = journalDao.deleteEntry(entry)

    suspend fun insertFocusSession(session: FocusSession) = focusSessionDao.insertSession(session)

    suspend fun updateDayProgress(day: DayProgress) = dayProgressDao.insertOrUpdateDay(day)

    suspend fun saveSettings(setting: AppSetting) = settingsDao.saveSettings(setting)

    suspend fun resetAllData() {
        taskDao.deleteAllTasks()
        journalDao.deleteAllEntries()
        focusSessionDao.deleteAllSessions()
        dayProgressDao.deleteAllDays()
        // Initialize 90 empty days
        for (i in 1..90) {
            val phase = when (i) {
                in 1..22 -> "Foundation"
                in 23..45 -> "Product Completion"
                in 46..68 -> "Expansion"
                else -> "Placement Weaponization"
            }
            dayProgressDao.insertOrUpdateDay(DayProgress(dayNumber = i, status = "UNATTEMPTED", phase = phase))
        }
        saveSettings(AppSetting())
    }
}
