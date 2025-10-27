# My Finances - AI Assistant Project Summary

> **Quick reference for AI assistants working with this codebase**

## Project Overview

**My Finances** is a native Android app for tracking personal finances across multiple accounts with different currencies. Built with modern Android development practices using Kotlin, Jetpack Compose, and Room Database.

**Key Info:**
- **Language:** Kotlin 2.0.21
- **UI:** Jetpack Compose + Material 3
- **Architecture:** MVVM with Hilt DI
- **Database:** Room (SQLite) with Flow
- **Min SDK:** 28 | **Target SDK:** 36

---

## Tech Stack Quick Reference

```kotlin
// UI & Presentation
- Jetpack Compose (Material 3 design)
- Hilt (Dependency Injection)
- StateFlow (State management)
- Navigation Compose

// Data & Domain
- Room Database (with Flow)
- DataStore (Preferences)
- Kotlin Coroutines & Flow

// Charts & Visualization
- Vico 2.0.0-alpha.28 (Charts library)
```

---

## Project Structure

```
app/src/main/java/dev/aulianenko/myfinances/
├── data/           # Data layer (Database)
│   ├── dao/        # Room DAOs (Flow-based queries)
│   ├── entity/     # Database entities (Account, AccountValue, ExchangeRate)
│   ├── database/   # AppDatabase (Room singleton)
│   └── repository/ # Data repositories (AccountRepository, ExchangeRateRepository, UserPreferencesRepository)
├── di/             # Hilt modules (DatabaseModule, RepositoryModule)
├── domain/         # Business logic
│   ├── model/      # Domain models (Statistics, Analytics)
│   ├── usecase/    # Use cases (CalculateStatisticsUseCase, CurrencyConversionUseCase, AnalyticsUseCase)
│   └── Currency.kt # Currency definitions (30+ currencies)
└── ui/             # Presentation layer
    ├── components/ # Reusable components (Charts, Inputs, Buttons)
    ├── navigation/ # NavGraph, Screen routes
    ├── screens/    # Feature screens (ViewModels + Composables)
    │   ├── account/      # Account CRUD
    │   ├── accountvalue/ # Value tracking
    │   ├── analytics/    # Performance analytics
    │   ├── dashboard/    # Portfolio overview
    │   └── settings/     # App settings
    └── theme/      # Material 3 theming
```

---

## Core Patterns & Conventions

### MVVM Pattern
```kotlin
// 1. UI State
data class FeatureUiState(
    val data: Data? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

// 2. ViewModel (with Hilt)
@HiltViewModel
class FeatureViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {
    private val _uiState = MutableStateFlow(FeatureUiState())
    val uiState: StateFlow<FeatureUiState> = _uiState.asStateFlow()
}

// 3. Composable Screen
@Composable
fun FeatureScreen(
    viewModel: FeatureViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    // UI implementation
}
```

### Repository Pattern (Flow-based)
```kotlin
class AccountRepository @Inject constructor(
    private val accountDao: AccountDao,
    private val accountValueDao: AccountValueDao
) {
    fun getAllAccounts(): Flow<List<Account>> =
        accountDao.getAllAccounts()

    suspend fun insertAccount(account: Account) =
        accountDao.insertAccount(account)
}
```

### Navigation
```kotlin
// Route definition
sealed class Screen(val route: String) {
    data object Dashboard : Screen("dashboard")
    data object AccountDetail : Screen("account_detail/{accountId}") {
        fun createRoute(accountId: String) = "account_detail/$accountId"
    }
}

// Navigation
navController.navigate(Screen.AccountDetail.createRoute(accountId))
```

---

## Database Schema

```sql
-- Version 2 (current)

Account {
    id: String (UUID PK)
    name: String
    currency: String
    createdAt: Long
    updatedAt: Long
}

AccountValue {
    id: String (UUID PK)
    accountId: String (FK → Account)
    value: Double
    timestamp: Long        -- User-selectable date
    note: String?
}

ExchangeRate {
    id: String (UUID PK)
    currencyCode: String (UNIQUE)
    rateToUSD: Double
    lastUpdated: Long
}

-- Relationships:
-- Account → AccountValue (1:N, cascade delete)
```

---

## Key Features

1. **Account Management** - CRUD with multi-currency
2. **Value Tracking** - Historical values with custom dates
3. **Charts** - Line charts (trends) + Pie charts (distribution)
4. **Analytics** - Performance metrics, volatility, correlations
5. **Multi-Currency** - 30+ currencies with conversion
6. **Settings** - Base currency preference + converter utility

---

## Common Tasks

### Adding a New Screen
```bash
1. Create ViewModel with @HiltViewModel
2. Create UiState data class
3. Create Composable screen
4. Add route to Screen.kt
5. Add to NavGraph.kt
6. Wire navigation in calling screen
```

### Adding Database Entity
```bash
1. Create @Entity class in data/entity/
2. Create @Dao interface in data/dao/
3. Add to AppDatabase entities list
4. Increment database version
5. Add migration (or use fallbackToDestructiveMigration for dev)
6. Update Repository
```

### Adding Hilt Dependency
```kotlin
// In appropriate module (DatabaseModule, RepositoryModule)
@Provides
@Singleton
fun provideFeature(dependency: Dependency): Feature {
    return FeatureImpl(dependency)
}
```

---

## Build & Run

```bash
# Build project
./gradlew build

# Run on device/emulator
./gradlew installDebug

# Clean build
./gradlew clean build
```

---

## Important Notes for AI Assistants

### When Modifying Code:

1. **Always use Hilt @Inject** for dependencies (ViewModels, Repositories, UseCases)
2. **Use Flow** for reactive data streams from Repository to UI
3. **StateFlow** for ViewModel state management
4. **Follow naming conventions:**
   - Screens: `FeatureScreen.kt`
   - ViewModels: `FeatureViewModel.kt`
   - State: `FeatureUiState`
5. **Composables are stateless** - state lives in ViewModel
6. **Use Material 3 components** - Not Material 2
7. **Icons**: Use `Icons.Default.*` - many Material icons don't exist
8. **Database changes**: Increment version, handle migration

### Common Gotchas:

- ⚠️ Many Material Icons don't exist - verify before using
- ⚠️ Use `@OptIn(ExperimentalMaterial3Api::class)` for DatePicker
- ⚠️ `menuAnchor()` is deprecated - warnings are acceptable
- ⚠️ Always use `hiltViewModel()` not manual creation
- ⚠️ Database: Prefer `.fallbackToDestructiveMigration()` in dev
- ⚠️ Charts: Vico uses specific data structures for LineChart/PieChart

### Commit Message Format:
```
Story X.Y: Brief description

- Detailed change 1
- Detailed change 2
- Detailed change 3

🤖 Generated with [Claude Code](https://claude.com/claude-code)

Co-Authored-By: Claude <noreply@anthropic.com>
```

---

## Dependencies (Key Libraries)

```toml
# UI
compose-bom = "2024.12.01"
material3 = (from BOM)

# Architecture
hilt = "2.52"
lifecycle = "2.8.7"
navigation-compose = "2.8.5"

# Database
room = "2.6.1"
datastore = "1.1.1"

# Charts
vico = "2.0.0-alpha.28"
```

---

## Testing Approach

**Current:** Manual testing on emulator/device
**Future:** Unit tests for ViewModels, Repository tests with in-memory DB

---

## Development Status

**Version:** 1.0 (October 2025)
**Database Version:** 2
**Total Features:** 15+ implemented
**Total Commits:** 20+

**Recently Implemented (Latest Session):**
- Story 8.1: Scrollable screens
- Story 8.2: Portfolio trend chart on dashboard
- Story 8.3: Date picker for historical values

---

## Quick Reference Links

- [README.md](README.md) - Full project overview
- [ARCHITECTURE.md](ARCHITECTURE.md) - Architecture details
- [FEATURES.md](FEATURES.md) - Complete feature documentation
- [DEVELOPMENT.md](DEVELOPMENT.md) - Development workflows

---

**Last Updated:** October 27, 2025
**For:** AI Assistants (Claude Code, GitHub Copilot, etc.)
**Maintained By:** Andrii Ulianenko
