package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val priority: String, // "P0", "P1", "P2"
    val category: String, // "Dentist PWA", "DSA", "Core Engineering", "AI Learning", "Cybersecurity", "Journal"
    val isCompleted: Boolean = false,
    val xpReward: Int = 200, // standard task completion gives 200 XP
    val dateCreated: Long = System.currentTimeMillis(),
    val dateCompleted: Long? = null
)

@Entity(tableName = "journal_entries")
data class JournalEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String, // e.g., "Tuesday, Oct 24th" or "24-05-2026"
    val reflectionWhatShipped: String = "",
    val reflectionBlocker: String = "",
    val reflectionImprovement: String = "",
    val lessonsLearned: String = "",
    val mood: String = "Neutral", // "Focus", "Calm", "Tired", "Stressed", "Beast"
    val energyLevel: Int = 3, // 1 to 5
    val wins: String = "", // raw string or comma separated
    val dateCreated: Long = System.currentTimeMillis()
)

@Entity(tableName = "focus_sessions")
data class FocusSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val missionName: String,
    val durationMinutes: Int,
    val xpEarned: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val category: String = "General"
)

@Entity(tableName = "day_progress")
data class DayProgress(
    @PrimaryKey val dayNumber: Int, // 1 to 90
    val status: String = "UNATTEMPTED", // "UNATTEMPTED", "LOW_DAY", "COMPLETED", "BEAST_MODE"
    val phase: String = "Foundation", // "Foundation", "Product Completion", "Expansion", "Placement Weaponization"
    val updatedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "app_settings")
data class AppSetting(
    @PrimaryKey val id: String = "default",
    val quotesEnabled: Boolean = true,
    val selectedQuotePreset: String = "Execution creates confidence.",
    val isDarkMode: Boolean = true,
    val examTargetDate: String = "2026-09-01",
    val notificationEnabled: Boolean = false,
    val earnedXp: Int = 0
)

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY CASE priority WHEN 'P0' THEN 1 WHEN 'P1' THEN 2 ELSE 3 END, dateCreated DESC")
    fun getAllTasks(): Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("DELETE FROM tasks")
    suspend fun deleteAllTasks()
}

@Dao
interface JournalDao {
    @Query("SELECT * FROM journal_entries ORDER BY dateCreated DESC")
    fun getAllEntries(): Flow<List<JournalEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: JournalEntry)

    @Update
    suspend fun updateEntry(entry: JournalEntry)

    @Delete
    suspend fun deleteEntry(entry: JournalEntry)

    @Query("DELETE FROM journal_entries")
    suspend fun deleteAllEntries()
}

@Dao
interface FocusSessionDao {
    @Query("SELECT * FROM focus_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<FocusSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: FocusSession)

    @Query("DELETE FROM focus_sessions")
    suspend fun deleteAllSessions()
}

@Dao
interface DayProgressDao {
    @Query("SELECT * FROM day_progress ORDER BY dayNumber ASC")
    fun getAllDays(): Flow<List<DayProgress>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDay(day: DayProgress)

    @Query("DELETE FROM day_progress")
    suspend fun deleteAllDays()
}

@Dao
interface AppSettingsDao {
    @Query("SELECT * FROM app_settings WHERE id = 'default'")
    fun getSettings(): Flow<AppSetting?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(setting: AppSetting)
}

@Database(
    entities = [Task::class, JournalEntry::class, FocusSession::class, DayProgress::class, AppSetting::class],
    version = 1,
    exportSchema = false
)
abstract class CompoundDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun journalDao(): JournalDao
    abstract fun focusSessionDao(): FocusSessionDao
    abstract fun dayProgressDao(): DayProgressDao
    abstract fun settingsDao(): AppSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: CompoundDatabase? = null

        fun getDatabase(context: Context): CompoundDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CompoundDatabase::class.java,
                    "compound_os_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
