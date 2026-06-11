# Grove

A personal finance tracker for Android that shows you one number -- what's safe to spend today -- so the rest can fade into the background.

```
./gradlew installDebug
```

---

## Features

| Feature | Description |
|---|---|
| Safe-to-spend today | A daily allowance that adjusts in real-time as you spend. Overshoot one day and the next morning recalculates from what's left. |
| Dashboard | Monthly burn rate, progress arc, today's allowance, recent transactions |
| History | Filterable, swipeable expense log grouped by day |
| Bills | Track recurring charges with status pills and swipe-to-delete |
| Reports | Month-over-month bars, daily pace chart, donut breakdown by category |
| Budget editing | Set a monthly budget with stepper controls; per-category caps |
| Currency support | 15 currencies with consistent symbol display everywhere |
| Dark mode | Toggle in settings with per-session override |

---

## Architecture

```
com.grove.app
├── designsystem/
│   ├── theme/
│   ├── component/
│   ├── catalog/
│   └── format/
├── data/
│   ├── model/
│   ├── db/
│   │   ├── entity/
│   │   ├── dao/
│   │   └── seed/
│   ├── BudgetState.kt
│   └── BudgetStateReactor.kt
└── feature/
    ├── home/
    ├── dashboard/
    ├── history/
    ├── bills/
    ├── reports/
    ├── budget/
    ├── settings/
    ├── addexpense/
    └── onboarding/
```

---

## Build requirements

| Requirement | Version |
|---|---|
| Android SDK | 35 |
| Kotlin | 2.0.21 |
| Gradle | 8.7+ |
| Java | 21 |

Open in Android Studio or run:

```sh
./gradlew installDebug
```

---

## Key libraries

| Library | Purpose |
|---|---|
| Jetpack Compose BOM 2024.10 | UI framework |
| Navigation Compose 2.8.5 | Screen routing |
| Room 2.6.1 | SQLite persistence (15 entities, 10 DAOs) |
| DataStore Preferences 1.1.1 | Dark mode override |
| kotlinx-collections-immutable 0.3.7 | Stable persistentListOf for Compose |
| Lifecycle ViewModel Compose 2.8.3 | Screen state management |
