 # 📱 Task Manager & Productivity App

A feature-rich Android task management and productivity application developed using **Kotlin and Android Studio**. The application helps users organize daily tasks, monitor productivity, maintain focus through a dedicated timer, and track achievements and statistics.

This project was developed as an academic Android application project and provided hands-on experience with Android development, UI design, application architecture, data persistence, background services, notifications, and user-focused functionality.

---

## ✨ Features

### 📝 Task Management

- Create and manage daily tasks
- Add new tasks with relevant information
- Set task names and priorities
- Organize tasks by categories
- Mark tasks as completed
- Edit and delete existing tasks
- Track task creation and completion information

### ⏱️ Focus Timer

- Dedicated focus/productivity timer
- Countdown functionality
- Start and pause focus sessions
- Foreground service for timer operation
- Notification support during focus sessions
- Helps users maintain focused working periods

### 📊 Productivity Dashboard

- View productivity statistics
- Monitor completed tasks
- Track task-related progress
- Visual representation of productivity data

### 🏆 Achievements

- Achievement system based on user activity
- Tracks productivity milestones
- Provides motivational feedback as users complete tasks and goals

### ⚙️ Settings

- Application settings and preferences
- Theme-related functionality
- Sound and notification-related controls
- User experience customization

### 🔔 Reminders & Notifications

- Task reminder functionality
- Notification support
- Scheduled task reminders

### 📱 Home Screen Widget

- Android home screen widget support
- Quick access to task-related information

---

# 📸 Application Screenshots

## 🏠 Home Screen

![Home Screen](./home-screen.png)

The home screen provides the main task management interface where users can view and manage their daily tasks.

---

## ➕ Add Task

![Add Task Screen](add-task-screen.png)

The Add Task screen allows users to create new tasks and provide the required task information before adding them to their task list.

---

## 📊 Dashboard

![Dashboard](dashboard-screen.png)

The dashboard provides an overview of productivity and task-related statistics, helping users monitor their progress.

---

## ⏱️ Focus Timer

![Timer Screen](timer-screen.png)

The focus timer helps users maintain concentration during dedicated productivity sessions.

---

## 🏆 Achievements

![Achievements Screen](achievements-screen.png)

The achievements section tracks user progress and productivity milestones.

---

## ⚙️ Settings

![Settings Screen](settings-screen.png)

The settings screen provides controls for customizing application behavior and preferences.

---

# 🛠️ Technologies Used

## Programming Language

- **Kotlin**

## Development Environment

- **Android Studio**
- **Gradle**
- **Kotlin DSL**

## Android Technologies

- Android SDK
- AndroidX
- View Binding
- Fragments
- Navigation Component
- ViewModel
- LiveData
- Coroutines
- Foreground Services
- Broadcast Receivers
- Android Notifications
- App Widgets

## Libraries

- AndroidX Core KTX
- AndroidX AppCompat
- Material Components
- ConstraintLayout
- Navigation Component
- Lifecycle ViewModel
- Lifecycle LiveData
- Gson
- Lottie
- Konfetti
- MPAndroidChart
- AndroidX Splash Screen

---

# 🏗️ Application Architecture

The application uses Android architecture components to separate the user interface from application logic.

```text
UI / Fragments
      ↓
ViewModel
      ↓
Application Logic
      ↓
Local Data Persistence
