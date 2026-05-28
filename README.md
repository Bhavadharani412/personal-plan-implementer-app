# Bhava 3.0
> **A Clean, Minimalist Focus, Streak, and Journal Tracking Ecosystem**  
> Engineered with Kotlin, Jetpack Compose, Material Design 3, and Room SQLite database.

Bhava 3.0 is a highly optimized, distraction-free personal accountability system designed to help developers and students maintain consecutive high-intensity study blocks, record daily journal feedback, track time spent on distinct subject categories, and progress through a sequential 90-day habits map.

---

## 📱 Visual Wireframe & Mockup Layouts

### 1. Daily Dashboard Screen Mockup
The main hub operates in a stunning, high-contrast **Deep Burgundy (#550C18) / Burnt Orange (#D86A33)** theme, emphasizing pristine layout spacing, legible typography, and elegant cards.

```text
+-------------------------------------------------------------+
|  BHAVA 3.0                                     [Settings]  |
|  STREAK & PROGRESS MANAGER                     12:00 UTC    |
|  Deep Burgundy & Burnt Orange High-Contrast Theme           |
+-------------------------------------------------------------+
|  [YOUR ACTIVE STREAK]                                       |
|  🔥 7 Days Consecutive Habit Streak                         |
|  "Your momentum is building. Keep the fire burning!"       |
+-------------------------------------------------------------+
| [⚡ PROGRESS]         [⏱️ FOCUS HOURS]      [🏆 LEVEL BASES] |
| 1450 XP Secured      |   4.2 Hrs Today     |   Level 02 Active|
+-------------------------------------------------------------+
|  ACTIVE PRIORITY: Write Dentist PWA API                     |
|  [||||||||||||........ 60%]  | [START TIMER]                |
+-------------------------------------------------------------+
|  TODAY'S PLAN (2 items remaining)               [VIEW ALL]  |
|  [ ] Design Database Schema          [High]  [Core Eng]   |
|  [ ] Solve 2 LeetCode questions      [Med]   [DSA]         |
+-------------------------------------------------------------+
|  [Dash]    [Plan]    [Focus]    [Journal]    [Stats]        |
+-------------------------------------------------------------+
```

### 2. Live Study Focus Timer Mockup
An elegant, distraction-free study cockpit. The radial countdown runs **persisted in a ViewModel-scoped background coroutine**, meaning you can switch between tabs without ever resetting or losing your progress! Includes custom preset buttons and a freeform dial-in box.

```text
+-------------------------------------------------------------+
|  BHAVA 3.0 // FOCUS                    (TIMER ACTIVE)       |
+-------------------------------------------------------------+
|  CURRENT STUDY FOCUS TASK                                   |
|  "Solve 2 LeetCode questions" (Subject: DSA)                |
+-------------------------------------------------------------+
|                                                             |
|                         . - ~ ~ ~ - .                       |
|                     . '   : : : :   ' .                     |
|                   /     .-----------.   \                   |
|                  /     /   44:59     \   \                  |
|                 |     | FOCUS ACTIVE  |    |                 |
|                  \     \             /   /                  |
|                   \     '-----------'   /                   |
|                     ' .             . '                     |
|                         ' - _ _ _ - '                       |
|                                                             |
|                   BACKGROUND PERSISTED CORE                 |
|                                                             |
+-------------------------------------------------------------+
|  Presets:   [ 25m ]  [ 45m ]  [ 67m ]  [ 120m ]             |
|  Or Dial Custom Minutes: [ 55    ]  [ SET ]                 |
|                                                             |
|  XP Reward potential: 450 XP                                |
+-------------------------------------------------------------+
|  [   CANCEL TIMER   ]               [   PAUSE TIMER   ]     |
+-------------------------------------------------------------+
|  [Dash]    [Plan]    [Focus]    [Journal]    [Stats]        |
+-------------------------------------------------------------+
```

### 3. Cumulative Analytics & Category Breakdown Mockup
Allows students to track how much time they have spent on individual subjects, and shows sequential progression on the 90-day progress tracker grid.

```text
+-------------------------------------------------------------+
|  BHAVA 3.0 PROGRESS TRACKER                                 |
|  Chronological sequential daily habit tracker.               |
+-------------------------------------------------------------+
|  COMPLETED DAYS BASELINE                                    |
|  Day 12 of 90 Completed                     13.3% COMPLETE  |
+-------------------------------------------------------------+
|  STREAK GRID                                                |
|  [■][■][■][■][■][■][■][■][■][■][■][■][ ][ ][ ][ ][ ][ ][ ][ ]|
|  [ ][ ][ ][ ][ ][ ][ ][ ][ ][ ][ ][ ][ ][ ][ ][ ][ ][ ][ ][ ]|
|  [ ][ ][ ][ ][ ][ ][ ][ ][ ][ ][ ][ ][ ][ ][ ][ ][ ][ ][ ][ ]|
|  (Task completions and deep focus timers auto-tick the list!) |
+-------------------------------------------------------------+
|  CATEGORY FOCUS BREAKDOWN                                    |
|  DSA              : ■■■■■■■■■■■■■■■■■■ 3.5 Hrs (54%)        |
|  Dentist PWA      : ■■■■■■■ 1.2 Hrs (18%)                   |
|  Core Engineering : ■■■■■ 0.8 Hrs (12%)                     |
|  AI Learning      : ■■■ 0.5 Hrs (8%)                        |
|  Cybersecurity    : ■■ 0.4 Hrs (6%)                         |
|  Journal          : ■ 0.2 Hrs (2%)                          |
+-------------------------------------------------------------+
|  [Dash]    [Plan]    [Focus]    [Journal]    [Stats]        |
+-------------------------------------------------------------+
```

---

## 🛠️ Complete Technical Specification

### 1. Architectural Blueprint
Bhava 3.0 implements a strict **MVVM (Model-View-ViewModel)** architectural pattern. It separates data persistence, business rules, and UI rendering cleanly to ensure reliability and maintainability:

```text
                  +--------------------------+
                  |  Jetpack Compose Layer   |
                  |  (CompoundUI / Theme)    |
                  +-------------+------------+
                                | Observes state flow
                                V
                  +--------------------------+
                  |    CompoundViewModel     |
                  +-------------+------------+
                                | Calls Repository APIs
                                V
                  +--------------------------+
                  |    CompoundRepository    |
                  +------+--------------+----+
                         |              |
         Reads & writes |              | Logs focus periods
                        V              V
                  +------------+ +------------+
                  |  Room DAO  | | SQLite DB  |
                  +------------+ +------------+
```

*   **View Layer (Jetpack Compose)**: Implements custom Material Design 3 components styled using the updated burgundy color theme.
*   **ViewModel Layer (`CompoundViewModel`)**: Manages the persistent study countdown within `viewModelScope` to maintain state across tab transitions.
*   **Repository Layer (`CompoundRepository`)**: Functions as the single source of truth, managing interactions with the database.
*   **Local Storage Layer (Room DB with Migration Support)**: Fully persistent storage for completed items, journal logs, and custom setup options, complete with dynamic schema migration safety.

---

## ⚡ Core Feature Directory

### 📝 Task Planner (`Plan` Tab)
*   **Categorization Matrix**: Group tasks by specialized categories such as *DSA*, *Core Engineering*, *AI Learning*, *Cybersecurity*, *Dentist PWA*, or *Journal*.
*   **Priority Levels**: Label tasks by Priority (*Low*, *Medium*, *High*).
*   **Automatic Streak Linkage**: Completing a task automatically increments and check-marks the next consecutive day on the 90-day habits grid.

### ⏱️ Persistent Study Timer (`Focus` Tab)
*   **ViewModel-Scoped Timer**: Runs perfectly in the background even when users browse other screens.
*   **Vibrant Presets**: Clean control buttons set for exactly **`25m`**, **`45m`**, **`67m`**, and **`120m`** pomodoros.
*   **Custom Dial Input**: Outline text box allowing freeform minutes specification (1 to 480 mins).
*   **XP Balance Protection**: User XP is saved permanently in settings, eliminating negative feedback when completing tasks and later deleting/archiving them.

### 📓 Daily Reflection Log (`Journal` Tab)
*   **Structured Fields**:
    *   Accomplished goals (*What did you accomplish today?*)
    *   Blockers & friction (*What blockers / friction did you face today?*)
    *   Optimizations (*What can you improve tomorrow?*)
    *   Wins and mood-o-meter dials.
*   **Persistent XP Reward**: Logs secure +100 XP to account setting balance upon submission.

### 📊 Streak Analytics & Focus Hours (`Stats` Tab)
*   **Sequential Streak Advancement**: Prompts consecutive habit indicators to move forward in perfect historical order on any logged focus block, journal entry, or task check.
*   **Category-wise Breakdown**: Visualizes focus hours partitioned across individual modules (DSA, Dentist PWA, AI Learning, etc.) in a beautifully rendered live bar scale.

---

## 🚀 Build and Run Instructions

This app is compiled using **Gradle (Kotlin DSL)** and modern standard tool chains.

### Prerequisites
*   JDK 17 or higher
*   Android SDK Platform 34

### Key Compilation Commands
To compile and assemble the debug release:
```bash
gradle :app:assembleDebug
```

To execute the unit and Robolectric tests:
```bash
gradle :app:testDebugUnitTest
```
