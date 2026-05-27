# Bhava 3.0
> **A Clean, Minimalist Focus, Streak, and Journal Tracking Ecosystem**  
> Engineered with Kotlin, Jetpack Compose, Material Design 3, and Room SQLite database.

Bhava 3.0 is a highly optimized, single-screen personal accountability system designed to help developers and students maintain consecutive high-intensity study blocks, record daily journal feedback, and chart a 90-day progress habits map.

---

## 📱 Visual Wireframe & Mockup Layouts

### 1. Daily Dashboard Screen Mockup
The main hub matches the **Cosmic Forest / Mint Green** dynamic color theme, using high-contrast typography, generous padding, and sleek Material cards.

```text
+-------------------------------------------------------------+
|  BHAVA 3.0                                     [Settings]  |
|  STREAK & PROGRESS MANAGER                     12:00 UTC    |
+-------------------------------------------------------------+
|  [YOUR STREAK]                                              |
|  🔥 7 Days Consecutive Habit Streak                         |
|  "Your momentum is building. Keep the fire burning!"       |
+-------------------------------------------------------------+
| [⚡ PROGRESS]         [⏱️ FOCUS HOURS]      [📈 DAILY BASES] |
|  450 XP Generated    |   4.2 Hrs Today     |   12 Days Logged|
+-------------------------------------------------------------+
|  ACTIVE PRIORITY: Write Dentist PWA API                     |
|  [||||||||||||........ 60%]  | [START TIMER]                |
+-------------------------------------------------------------+
|  TODAY'S PLAN (2 items remaining)               [VIEW ALL]  |
|  [ ] Design Database Schema          [High]  [Core Eng]   |
|  [ ] Solve 2 LeetCode questions      [Med]   [DSA]         |
+-------------------------------------------------------------+
|  [Dash]    [Plan]    [Focus]    [Journal]    [Streak]       |
+-------------------------------------------------------------+
```

### 2. Live Study Focus Timer Mockup
An elegant, distraction-free radial countdown design displaying real-time feedback and focus metrics.

```text
+-------------------------------------------------------------+
|  BHAVA 3.0 // FOCUS                        (STANDBY // IDLE)|
+-------------------------------------------------------------+
|  STUDY FOCUS BLOCK                                          |
|  "Solve 2 LeetCode questions"                               |
+-------------------------------------------------------------+
|                                                             |
|                         . - ~ ~ ~ - .                       |
|                     . '   : : : :   ' .                     |
|                   /     .-----------.   \                   |
|                  /     /   44:59     \   \                  |
|                 |     |  MINS LEFT   |    |                 |
|                  \     \             /   /                  |
|                   \     '-----------'   /                   |
|                     ' .             . '                     |
|                         ' - _ _ _ - '                       |
|                                                             |
|                      STUDY FOCUS TIMER                      |
|                                                             |
+-------------------------------------------------------------+
|  Presets:   [ 25m ]       [ 50m * ]       [ 90m ]           |
|  XP Reward potential: 500 XP                               |
+-------------------------------------------------------------+
|  [   CANCEL TIMER   ]               [   PAUSE TIMER   ]     |
+-------------------------------------------------------------+
|  [Dash]    [Plan]    [Focus]    [Journal]    [Streak]       |
+-------------------------------------------------------------+
```

### 3. Cumulative 90-Day Streak Grid Mockup
The visual engine maps accountability using three-tier color coding. Every checked square reinforces the daily loop.

```text
+-------------------------------------------------------------+
|  BHAVA 3.0 PROGRESS TRACKER                                 |
|  A 90-day progress habits map to build a consistent streak. |
+-------------------------------------------------------------+
|  COMPLETED DAYS BASELINE                                    |
|  Day 12 of 90 Completed                     13.3% COMPLETE  |
+-------------------------------------------------------------+
|  STREAK GRID                                                |
|  [■][■][■][■][■][■][■][▣][▨][▥][ ][ ][ ][ ][ ][ ][ ][ ][ ][ ]|
|  [ ][ ][ ][ ][ ][ ][ ][ ][ ][ ][ ][ ][ ][ ][ ][ ][ ][ ][ ][ ]|
|  [ ][ ][ ][ ][ ][ ][ ][ ][ ][ ][ ][ ][ ][ ][ ][ ][ ][ ][ ][ ]|
|  (Click a grid cell to cycle and update habit focus status)  |
|                                                             |
|  [■] LIGHT FOCUS   [▣] FULL FOCUS   [▨] HIGH FOCUS          |
+-------------------------------------------------------------+
|  90-DAY FOCUS PHASES                                        |
|  [✓] Phase 1: Foundation (Days 1-22): Core data structures. |
|  [ ] Phase 2: Apps Build (Days 23-45): Write modern apps.   |
|  [ ] Phase 3: Expansion (Days 46-68): System architecture.  |
|  [ ] Phase 4: Goals Achieved (Days 69-90): Mock interviews. |
+-------------------------------------------------------------+
```

---

## 🛠️ Complete Technical Specification

### 1. Architectural Blueprint
Bhava 3.0 implements the **MVVM (Model-View-ViewModel)** architectural pattern. It separates data persistence, business rules, and UI rendering cleanly to ensure reliability and maintainability:

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

*   **View Layer (Jetpack Compose)**: Implements Material Design 3 components, reacting dynamically to stream states emitted from the ViewModel. Includes comprehensive `enableEdgeToEdge()` configuration and clean status layouts.
*   **ViewModel Layer (`CompoundViewModel`)**: Controls application state using Kotlin `MutableStateFlow` and handles background operations inside safe `viewModelScope` coroutines.
*   **Repository Layer (`CompoundRepository`)**: Functions as the single source of truth, balancing persistent values stored in Room SQLite with local states.
*   **Local Storage Layer (Room DB)**: Maintains records for planned tasks, focus session logs, progress grid items, daily journal logs, and dynamic user configurations.

---

## ⚡ Core Feature Directory

### 📝 Task Planner (`Plan` Tab)
*   **Categorization Matrix**: Group tasks by specialized categories such as *DSA*, *Core Engineering*, *AI Learning*, *Cybersecurity*, *Dentist PWA*, or *Journal*.
*   **Priority Levels**: Label tasks by Priority (*Low*, *Medium*, *High*).
*   **Filtering**: Instantly filter on the-fly using active category tags.
*   **Dynamic Status**: Toggle task completion checks instantly in the database with cascading visual indicators showing completed, semi-transparent, or active cards.

### ⏱️ Custom Study Timer (`Focus` Tab)
*   **Concentric Rings Canvas**: Renders a visually striking radial progress interface utilizing clean mathematical canvas dashes and background arches.
*   **Duration Presets**: Quick preset buttons configured to standard focus blocks (25 mins, 50 mins, 90 mins).
*   **XP Rewards**: Earns scaled experience points (XP) based on study minutes completed. Standalone sessions automatically log as general Focus blocks.

### 📓 Daily Reflection Log (`Journal` Tab)
*   **Structured Fields**:
    *   Accomplished goals (*What did you accomplish today?*)
    *   Blockers & friction (*What blockers / friction did you face today?*)
    *   Optimizations (*What can you improve tomorrow?*)
    *   Markdown lessons learned archive.
    *   Dynamic wins comma-separated list.
*   **Energy Level Dial**: Sliding indicator scoring daily mental stamina on a scale from 1 to 5.
*   **Expandable History**: Chronological reverse-sorted history grid of entries complete with a slide-up inspection detail card.

### 📊 Streak Analytics (`Streak` Tab)
*   **Interactive grid**: Change study baseline density (None -> Light -> Full -> High Focus) dynamically by tap commands.
*   **Milestone Phases**: Keep track of structured 90-day progress metrics spanning 4 foundation modules.
*   **Subject Practice Progress**: Dynamic visual percentage bars reflecting overall course progress aligned with finished developer tasks.

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

---
*Note: The images on the free tier quota limit were temporarily bypassed during the current workspace generation turn. High fidelity SVG and text outlines are embedded above to preserve pixel-perfect UI positioning details.*
