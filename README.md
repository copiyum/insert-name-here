# Grove

A personal finance tracker for Android. Grove helps you keep an eye on what you owe, what you've spent, and whether you're on track for the month — without the overwhelm of a spreadsheet or the lock-in of a bank app.

---

## What it does

Grove gives you a four-panel view of your money:

- **Dashboard** — this month's burn rate, recent transactions, and a progress arc that tells you honestly how the rest of the month is looking
- **History** — a scrollable, filterable log of every expense with category breakdown
- **Bills** — upcoming recurring charges with due dates and amounts
- **Reports** — month-over-month charts so you can spot the trends that budgets alone won't show you

It's a single-user, single-device app. No accounts, no sync, no ads. Your data stays on your phone.

---

## The app

Grove is a native Android app written in Kotlin with Jetpack Compose. The UI layer is built entirely from a custom design system in `com.grove.app.designsystem`, with typed tokens for color, typography, spacing, and motion — no third-party component library. Business logic and derived state live in a repository layer that sits behind a `ViewModel`, keeping Compose free of any non-UI concerns. The data layer is currently backed by in-memory seed data; a `BudgetRepository` interface is in place for swapping in persistent storage (DataStore / Room) without touching any UI code.

---

## Architecture overview

```
com.grove.app
├── MainActivity.kt                 # thin shell: sets up theme and navigation host
├── designsystem/
│   ├── theme/                      # Color, Typography, Tokens, Theme
│   ├── component/                  # reusable UI primitives (charts, inputs, chips, rows, etc.)
│   ├── catalog/                    # CategoryVisuals — single source of truth for category icons/colors/labels
│   └── format/                     # Currency and number formatting utilities
├── data/
│   ├── model/                      # raw domain objects (User, Category, Expense, Bill)
│   ├── BudgetState.kt              # immutable state container
│   ├── BudgetCalculator.kt         # pure functions: tone, totals, safe-to-spend
│   └── BudgetRepository.kt          # interface with in-memory seed implementation
└── feature/
    ├── home/                       # navigation host, bottom bar, FAB, global sheets/toast
    ├── dashboard/
    ├── history/
    ├── bills/
    ├── budget/
    ├── reports/
    ├── settings/
    ├── addexpense/
    └── onboarding/
```

The feature packages own their own screen composable and ViewModel. The `home` feature is the navigation host and holds the single source of truth for global app state. Features depend only on the design system and the data layer — no feature may reach into another feature's internals.

---

## Build requirements

| Requirement | Version |
|---|---|
| Android SDK | API 35 |
| Kotlin | 2.0.21 |
| Gradle | 8.7+ |
| Java | 21 |

Run the project with:

```sh
./gradlew installDebug
```

Or open it directly in Android Studio (the project uses the Gradle wrapper — no additional setup needed).

---

## Key libraries

| Library | Purpose |
|---|---|
| Jetpack Compose BOM 2024.10 | UI framework |
| Material 3 | Design system base |
| Navigation Compose 2.8.5 | Screen navigation with backstack |
| Lifecycle ViewModel Compose 2.8.3 | State holder per screen |
| Lifecycle Runtime Compose 2.8.3 | collectAsStateWithLifecycle |
| DataStore Preferences 1.1.1 | Persistent settings (dark mode, budget) |
| kotlinx-collections-immutable 0.3.7 | Stable, skippable list types in Compose |

---

### Grove is actively in development.