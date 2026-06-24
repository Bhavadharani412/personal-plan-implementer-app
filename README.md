# SkillOS

SkillOS is a time-first personal operating system for students, self-learners, freelancers, and career-focused professionals.

The app helps users organize learning plans, subjects, topics, goals, and study sessions while tracking actual time spent learning and building skills.

## Features

### Learning Management

* Create long-term learning plans
* Organize skills into categories
* Create subjects under categories
* Break subjects into actionable topics
* Track topic completion progress

### Focus Session Timer

* Start learning sessions for any topic
* Pause and resume active sessions
* Persistent timer state
* Session duration tracking

### Goal Tracking

* Set learning goals
* Track target study minutes
* Monitor progress across subjects

### Analytics Dashboard

* View study activity
* Track learning consistency
* Measure time invested in skill development

### Offline First

* Local Room database storage
* No internet connection required
* Fast and reliable data access

---

## Tech Stack

### Android

* Kotlin
* Jetpack Compose
* Material 3

### Architecture

* MVVM Architecture
* Repository Pattern
* StateFlow
* Coroutines

### Data Layer

* Room Database
* DAO Pattern
* Reactive Flows

---

## Database Entities

### Plan

Represents a learning roadmap with start and end dates.

### Category

Top-level skill grouping.

Examples:

* Programming
* Freelancing
* Career Preparation
* Communication

### Subject

A focused learning area inside a category.

Examples:

* Android Development
* Data Structures
* UI/UX Design

### Topic

Individual learning units inside a subject.

Examples:

* Room Database
* Dependency Injection
* Compose Navigation

### Session

Stores completed focus sessions and learning time.

### Goal

Tracks target learning minutes.

### Resource

Stores learning references such as:

* Articles
* Documentation
* GitHub repositories
* Videos
* PDFs

### Note

Topic-specific notes and knowledge capture.

---

## Project Structure

```text
app/
├── data/
│   ├── dao/
│   ├── database/
│   ├── entity/
│   └── repository/
├── ui/
│   ├── screens/
│   ├── theme/
│   └── viewmodel/
└── MainActivity.kt
```

---

## Getting Started

### Prerequisites

* Android Studio Hedgehog or newer
* Android SDK
* JDK 17

### Installation

```bash
git clone https://github.com/Bhavadharani412/personal-plan-implementer-app.git
cd personal-plan-implementer-app
```

Open the project in Android Studio and allow Gradle sync to complete.

### Run

1. Connect an Android device or start an emulator.
2. Build the project.
3. Run the application.

---

## Current Capabilities

* Learning plan management
* Category management
* Subject management
* Topic management
* Goal management
* Session timer
* Room database persistence
* Dashboard analytics

---

## Future Roadmap

* Cloud sync
* Backup and restore
* AI learning assistant
* Smart recommendations
* Productivity insights
* Calendar integration
* Streak tracking
* Habit system
* Export reports
* Cross-device synchronization

---

## Vision

SkillOS aims to become a personal operating system for skill development by helping users intentionally invest time, track progress, and build career-ready expertise through structured learning.
