# My Finances - Architecture Documentation

## Table of Contents
1. [Project Overview](#project-overview)
2. [Architecture Pattern](#architecture-pattern)
3. [Project Structure](#project-structure)
4. [Key Design Decisions](#key-design-decisions)
5. [Data Layer](#data-layer)
6. [Domain Layer](#domain-layer)
7. [UI Layer](#ui-layer)
8. [Navigation](#navigation)
9. [Technology Stack](#technology-stack)
10. [Development Workflow](#development-workflow)

---

## Project Overview

**My Finances** is a native Android application for tracking personal finances across multiple accounts with different currencies. The app enables users to:
- Manage multiple financial accounts
- Track account values over time
- View portfolio statistics and performance
- Update account values individually or in bulk
- Analyze trends across different time periods

**Target SDK:** Android 14 (API 36)
**Minimum SDK:** Android 9 (API 28)
**Language:** Kotlin
**UI Framework:** Jetpack Compose

---

## Architecture Pattern

### MVVM (Model-View-ViewModel)

The app follows the **Simple MVVM** pattern without additional architectural layers like Clean Architecture. This decision provides:

**Benefits:**
- Clear separation of concerns
- Easier to understand and maintain
- Faster development for the app's scope
- Sufficient for current requirements
- Easy to test business logic

**Structure:**
```
Model (Data Layer) ← Repository ← ViewModel ← View (UI Layer)
```

### Reactive Programming

The app uses **Kotlin Flow** for reactive data streams:
- Real-time UI updates when data changes
- Declarative data transformations
- Lifecycle-aware data collection
- Efficient resource management

---

## Project Structure

```
app/src/main/java/dev/aulianenko/myfinances/
│
├── data/                          # Data Layer
│   ├── dao/                       # Database Access Objects
│   │   ├── AccountDao.kt
│   │   └── AccountValueDao.kt
│   ├── database/                  # Database configuration
│   │   └── AppDatabase.kt
│   ├── entity/                    # Room entities (database tables)
│   │   ├── Account.kt
│   │   └── AccountValue.kt
│   └── repository/                # Data repositories
│       └── AccountRepository.kt
│
├── domain/                        # Business Logic Layer
│   ├── model/                     # Domain models
│   │   ├── AccountStatistics.kt
│   │   └── TimePeriod.kt
│   ├── usecase/                   # Business use cases
│   │   └── CalculateStatisticsUseCase.kt
│   └── Currency.kt                # Currency definitions
│
└── ui/                           # Presentation Layer
    ├── components/               # Reusable UI components
    │   ├── AppTopBar.kt
    │   ├── EmptyState.kt
    │   ├── InputField.kt
    │   ├── LoadingIndicator.kt
    │   └── PrimaryButton.kt
    ├── navigation/               # Navigation configuration
    │   ├── BottomNavItem.kt
    │   ├── NavGraph.kt
    │   ├── PlaceholderScreen.kt
    │   └── Screen.kt
    ├── screens/                  # Feature screens
    │   ├── account/              # Account management screens
    │   │   ├── AccountDetailScreen.kt
    │   │   ├── AccountDetailViewModel.kt
    │   │   ├── AccountListScreen.kt
    │   │   ├── AccountListViewModel.kt
    │   │   ├── AddAccountScreen.kt
    │   │   ├── AddAccountViewModel.kt
    │   │   ├── EditAccountScreen.kt
    │   │   └── EditAccountViewModel.kt
    │   ├── accountvalue/         # Value update screens
    │   │   ├── AddAccountValueScreen.kt
    │   │   ├── AddAccountValueViewModel.kt
    │   │   ├── BulkUpdateScreen.kt
    │   │   └── BulkUpdateViewModel.kt
    │   └── dashboard/            # Dashboard screens
    │       ├── DashboardScreen.kt
    │       └── DashboardViewModel.kt
    └── theme/                    # App theming
        ├── Color.kt
        ├── Theme.kt
        └── Type.kt
```

---

## Key Design Decisions

### 1. **Simple MVVM Over Clean Architecture**

**Decision:** Use Simple MVVM without UseCase layer for most features

**Rationale:**
- App scope is well-defined and manageable
- Reduces boilerplate and complexity
- Faster feature development
- Still maintainable and testable
- UseCase introduced only for complex operations (CalculateStatisticsUseCase)

**Trade-offs:**
- ViewModels might grow larger for complex features
- Less abstraction between layers
- Acceptable for current app size

---

### 2. **Jetpack Compose for UI**

**Decision:** Use Compose instead of XML Views

**Rationale:**
- Modern, declarative UI development
- Less boilerplate code
- Built-in state management
- Better preview support
- Future-proof technology

**Benefits Observed:**
- Faster UI development
- Easier to create reusable components
- Better integration with ViewModels
- Simplified navigation

---

### 3. **Room Database for Persistence**

**Decision:** Use Room over alternatives (Realm, SQLDelight)

**Rationale:**
- Official Android solution
- Excellent Kotlin support
- Compile-time query verification
- Flow integration for reactive queries
- Well-documented and maintained

**Implementation Details:**
- Entity-based schema
- Foreign key relationships with cascade delete
- Flow-based reactive queries
- Singleton database pattern

---

### 4. **UUID for Entity IDs**

**Decision:** Use String UUIDs instead of auto-increment integers

**Rationale:**
- Globally unique identifiers
- Better for potential sync features
- No collision risks
- Client-side generation

**Trade-offs:**
- Slightly larger database size
- Acceptable for app scale

---

### 5. **Multi-Currency Support**

**Decision:** Store currency as String code (USD, EUR, etc.)

**Rationale:**
- Simple implementation
- No currency conversion needed initially
- Each account tracks values in its own currency
- CurrencyProvider handles display logic

**Current Limitation:**
- No automatic currency conversion
- No exchange rate tracking
- Future enhancement opportunity

---

### 6. **Timestamp-Based Value Tracking**

**Decision:** Store timestamp with each AccountValue

**Rationale:**
- Enables historical analysis
- Supports time-period filtering
- Bulk updates share single timestamp
- Chronological ordering

**Implementation:**
```kotlin
data class AccountValue(
    val id: String,
    val accountId: String,
    val value: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val note: String? = null
)
```

---

### 7. **Bottom Navigation Pattern**

**Decision:** Bottom navigation for main sections, top navigation for details

**Rationale:**
- Mobile-first navigation pattern
- Easy thumb access
- Clear section separation
- Material Design best practice

**Sections:**
- Dashboard (analytics)
- Accounts (management)
- Settings (configuration)

---

### 8. **State Management with StateFlow**

**Decision:** Use StateFlow in ViewModels

**Rationale:**
- Type-safe state container
- Lifecycle-aware
- Single source of truth
- Easy to test

**Pattern:**
```kotlin
private val _uiState = MutableStateFlow(UiState())
val uiState: StateFlow<UiState> = _uiState.asStateFlow()
```

---

## Data Layer

### Database Schema

**Entities:**

1. **Account**
   - `id`: String (UUID)
   - `name`: String
   - `currency`: String
   - `createdAt`: Long
   - `updatedAt`: Long

2. **AccountValue**
   - `id`: String (UUID)
   - `accountId`: String (Foreign Key)
   - `value`: Double
   - `timestamp`: Long
   - `note`: String?

**Relationships:**
- One Account → Many AccountValues
- Cascade delete: Deleting account removes all values

### Repository Pattern

**AccountRepository** acts as single source of truth:
- Encapsulates DAOs
- Provides Flow-based APIs
- Handles data transformations
- No caching layer (Room handles this)

**Key Methods:**
```kotlin
fun getAllAccounts(): Flow<List<Account>>
fun getAccountById(id: String): Flow<Account?>
fun getLatestAccountValue(accountId: String): Flow<AccountValue?>
fun getAccountValuesInPeriod(accountId: String, start: Long, end: Long): Flow<List<AccountValue>>
```

---

## Domain Layer

### Use Cases

**CalculateStatisticsUseCase:**
- Combines account and value data
- Calculates performance metrics
- Filters by time period
- Returns PortfolioStatistics

**Why separate UseCase here:**
- Complex business logic
- Multiple data sources
- Reusable across screens
- Easier to test

### Domain Models

**AccountStatistics:**
```kotlin
data class AccountStatistics(
    val accountId: String,
    val accountName: String,
    val currency: String,
    val currentValue: Double,
    val firstValue: Double?,
    val valueChange: Double?,
    val percentageChange: Double?,
    val valueCount: Int,
    val period: TimePeriod
)
```

**TimePeriod:**
- THREE_MONTHS
- SIX_MONTHS
- ONE_YEAR
- MAX (all time)

---

## UI Layer

### Composable Architecture

**Screen Structure:**
```
Screen (Composable)
  ├── ViewModel (state management)
  ├── UI State (data class)
  └── UI Components (reusable)
```

### Reusable Components

1. **AppTopBar** - Consistent top navigation
2. **InputField** - Form inputs with validation
3. **PrimaryButton** - Standard action buttons
4. **LoadingIndicator** - Loading states
5. **EmptyState** - Empty data placeholders

### Screen Patterns

**Typical Screen Implementation:**
```kotlin
@Composable
fun FeatureScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel = remember { FeatureViewModel(repository) }
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { AppTopBar(...) }
    ) { padding ->
        when {
            uiState.isLoading -> LoadingIndicator()
            uiState.isEmpty -> EmptyState(...)
            else -> Content(...)
        }
    }
}
```

### State Hoisting

- UI state lives in ViewModel
- Events passed as lambdas
- No business logic in Composables
- Unidirectional data flow

---

## Navigation

### Navigation Architecture

**Type:** Compose Navigation with NavController

**Route Definition:**
```kotlin
sealed class Screen(val route: String) {
    data object Dashboard : Screen("dashboard")
    data object AccountDetail : Screen("account_detail/{accountId}") {
        fun createRoute(accountId: String) = "account_detail/$accountId"
    }
}
```

### Navigation Patterns

1. **Bottom Navigation** - Main sections (Dashboard, Accounts, Settings)
2. **Stack Navigation** - Detail screens with back button
3. **State Preservation** - Bottom nav saves/restores state

### Navigation Graph

- `startDestination`: Dashboard
- Bottom bar shows on: Dashboard, AccountList, Settings
- Detail screens hide bottom bar

---

## Technology Stack

### Core Libraries

| Library | Version | Purpose |
|---------|---------|---------|
| Kotlin | 2.0.21 | Programming language |
| Compose BOM | 2024.12.01 | UI framework |
| Room | 2.6.1 | Local database |
| Navigation Compose | 2.8.5 | Navigation |
| Lifecycle | 2.8.7 | Android lifecycle |
| Material 3 | - | Design system |

### Build Configuration

- **Gradle**: Kotlin DSL
- **Version Catalog**: Centralized dependency management
- **KSP**: Annotation processing for Room
- **Min SDK**: 28 (Android 9)
- **Target SDK**: 36 (Android 14)
- **Compile SDK**: 36

---

## Development Workflow

### Git Commit Strategy

**Pattern:** One commit per user story

**Format:**
```
Story X.Y: Brief description

- Bullet point 1
- Bullet point 2
- Bullet point 3
```

**Benefits:**
- Clear feature history
- Easy to review
- Revertable features
- Good documentation

### Feature Development Flow

1. Define user story
2. Create necessary data models
3. Implement ViewModel
4. Build UI with Compose
5. Wire navigation
6. Test feature
7. Commit with descriptive message

### Code Organization

**By Feature:**
- Related ViewModels and Screens together
- Shared components in `ui/components`
- Domain logic in `domain` package

**Naming Conventions:**
- `*Screen.kt` - Composable screens
- `*ViewModel.kt` - ViewModels
- `*UiState` - UI state data classes
- `*Repository.kt` - Data repositories

---

## Future Considerations

### Potential Enhancements

1. **Dependency Injection**
   - Consider Hilt/Koin for larger codebase
   - Currently using manual dependency creation

2. **Testing**
   - Unit tests for ViewModels
   - UI tests for critical flows
   - Repository tests with in-memory database

3. **Data Sync**
   - Cloud backup
   - Multi-device sync
   - Conflict resolution

4. **Currency Conversion**
   - Exchange rate API integration
   - Automatic conversion
   - Total portfolio value in base currency

5. **Export/Import**
   - CSV export
   - JSON backup
   - Data migration

---

## Lessons Learned

### What Worked Well

1. **Simple MVVM** - Right level of abstraction
2. **Compose** - Faster UI development
3. **Flow** - Reactive updates simplified
4. **Feature commits** - Easy to track progress
5. **Bottom navigation** - Intuitive UX

### Trade-offs Accepted

1. **No DI framework** - Acceptable for current size
2. **No Clean Architecture** - Simpler codebase
3. **No currency conversion** - Future feature
4. **Manual ViewModel creation** - Works for now
5. **No offline-first** - Not required initially

### Technical Debt

1. Deprecation warning: `menuAnchor()` in dropdown
2. No comprehensive test coverage
3. No error analytics/logging
4. Limited input validation in some screens

---

## Conclusion

The My Finances app demonstrates a pragmatic approach to Android development, balancing modern best practices with development speed. The simple MVVM architecture provides sufficient structure without over-engineering, while Jetpack Compose and Room enable rapid feature development with type safety and reactive updates.

The architecture supports the current feature set well and has room to grow with additional patterns (DI, testing, advanced features) as needed.

---

**Last Updated:** October 26, 2025
**Version:** 1.0
**Commits:** 17
