# Claude Code Instructions - MyFinances

This document provides essential context and guidelines for AI assistants (like Claude Code) working on the MyFinances Android project.

---

## Project Overview

**MyFinances** is a modern Android portfolio tracking application built with Jetpack Compose and Kotlin. The app allows users to track financial accounts across multiple currencies with historical value tracking, analytics, and live exchange rate updates.

**Current Version:** 1.0 (October 2025)
**Status:** Production-ready, actively maintained

### Key Capabilities
- Multi-account portfolio tracking (30+ currencies)
- Historical value tracking with custom dates
- Dashboard analytics with time-based filtering
- Live exchange rates from Frankfurter API
- Automatic daily rate updates (WorkManager)
- Configurable reminder notifications
- Material 3 design with dark mode

---

## Architecture

### Pattern: Simple MVVM

The project uses **Simple MVVM** (no additional Clean Architecture layers) with clear separation:

```
UI Layer (Compose)
    ↓ StateFlow
Domain Layer (Use Cases)
    ↓ Flow
Data Layer (Repository → Room/API)
```

**Key Principle:** Simplicity over complexity. Use Cases handle business logic, ViewModels manage UI state, Repositories handle data sources.

### Technology Stack

- **Language:** Kotlin 2.0.21
- **UI:** Jetpack Compose + Material 3
- **DI:** Hilt (Dagger)
- **Database:** Room 2.6.1
- **Networking:** Retrofit 2.11.0 + OkHttp 4.12.0 + Moshi 1.15.1
- **Background:** WorkManager 2.9.1
- **Charts:** Vico 2.0.0-alpha.28
- **State:** DataStore 1.1.1 (preferences)

### Project Structure

```
app/src/main/java/dev/aulianenko/myfinances/
├── data/
│   ├── api/              # API services (Retrofir)
│   │   ├── model/        # API response models
│   │   └── FrankfurterApiService.kt
│   ├── dao/              # Room DAOs
│   ├── database/         # AppDatabase
│   ├── entity/           # Room entities
│   └── repository/       # Data repositories
├── di/                   # Hilt modules
│   ├── DatabaseModule.kt
│   ├── NetworkModule.kt
│   └── RepositoryModule.kt
├── domain/
│   ├── model/            # Domain models (not entities)
│   ├── usecase/          # Business logic
│   └── Currency.kt
├── notification/         # WorkManager workers
│   ├── ExchangeRateWorker.kt
│   ├── ReminderWorker.kt
│   └── NotificationScheduler.kt
└── ui/
    ├── components/       # Reusable composables
    ├── navigation/       # NavGraph, Screen, BottomNavItem
    ├── screens/          # Feature screens (each has Screen + ViewModel)
    │   ├── account/
    │   ├── accountvalue/
    │   ├── dashboard/
    │   └── settings/
    └── theme/            # Material 3 theme
```

---

## Key Patterns & Conventions

### 1. ViewModel Pattern

**Always follow this structure:**

```kotlin
data class FeatureUiState(
    val data: Data? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

@HiltViewModel
class FeatureViewModel @Inject constructor(
    private val useCase: UseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeatureUiState())
    val uiState: StateFlow<FeatureUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            useCase.getData().collect { data ->
                _uiState.update { it.copy(data = data, isLoading = false) }
            }
        }
    }
}
```

**Important:**
- Use `MutableStateFlow` privately, expose `StateFlow` publicly
- Use `.update { }` to modify state (thread-safe)
- Collect Flows in `viewModelScope.launch`
- Handle loading and error states

### 2. Repository Pattern

```kotlin
class FeatureRepository @Inject constructor(
    private val dao: FeatureDao,
    private val apiService: ApiService? = null
) {
    fun getData(): Flow<List<Data>> = dao.getData()

    suspend fun insert(data: Data) = dao.insert(data)

    // API integration example
    suspend fun updateFromApi(): Result<Int> {
        return try {
            val response = apiService.fetch()
            dao.insertAll(response.toEntities())
            Result.success(response.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

**Important:**
- Repositories expose Flows for reactive data
- Suspend functions for one-time operations
- Use `Result<T>` for operations that can fail
- Keep repositories simple - no business logic

### 3. Use Case Pattern

```kotlin
class FeatureUseCase @Inject constructor(
    private val repository: Repository
) {
    fun getData(): Flow<Data> = repository.getData()

    suspend fun performAction(): Result<Unit> {
        // Business logic here
        return repository.doSomething()
    }
}
```

**When to create Use Cases:**
- Complex business logic
- Combining multiple repositories
- Data transformation/calculations
- When logic would clutter ViewModel

**When to skip Use Cases:**
- Simple CRUD operations
- Direct repository pass-through

### 4. Composable Screens

```kotlin
@Composable
fun FeatureScreen(
    modifier: Modifier = Modifier,
    viewModel: FeatureViewModel = hiltViewModel(),
    onNavigate: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { AppTopBar(title = "Title") }
    ) { paddingValues ->
        if (uiState.isLoading) {
            LoadingIndicator()
        } else {
            // Content
        }
    }
}
```

**Important:**
- Use `hiltViewModel()` for ViewModel injection
- Collect state with `collectAsState()`
- Always pass `Modifier` parameter
- Use `Scaffold` for Material 3 structure
- Handle loading/error states

### 5. Database Entities

```kotlin
@Entity(tableName = "table_name")
data class EntityName(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "column_name")
    val field: String,

    val createdAt: Long = System.currentTimeMillis()
)
```

**Important:**
- Use UUID strings for primary keys
- Add created/updated timestamps
- Use `@ColumnInfo` for custom column names
- Keep entities simple - no business logic

### 6. DAOs

```kotlin
@Dao
interface FeatureDao {
    @Query("SELECT * FROM table_name")
    fun getAll(): Flow<List<Entity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: Entity)

    @Delete
    suspend fun delete(entity: Entity)
}
```

**Important:**
- Return `Flow` for reactive queries
- Use `suspend` for insert/update/delete
- Use `OnConflictStrategy.REPLACE` for upserts

---

## Database Schema

### Current Tables

**Account**
- `id` (String, PK) - UUID
- `name` (String) - Account name
- `currency` (String) - Currency code (USD, EUR, etc.)
- `createdAt` (Long) - Timestamp
- `updatedAt` (Long) - Timestamp

**AccountValue**
- `id` (String, PK) - UUID
- `accountId` (String, FK → Account) - Cascade delete
- `value` (Double) - Account value
- `timestamp` (Long) - User-selectable timestamp
- `note` (String?) - Optional note

**ExchangeRate**
- `id` (String, PK) - UUID
- `currencyCode` (String, UNIQUE) - Currency code
- `rateToUSD` (Double) - Exchange rate to USD
- `lastUpdated` (Long) - Timestamp

**Database Version:** 2

---

## Common Development Tasks

### Adding a New Feature Screen

1. Create package in `ui/screens/featurename/`
2. Create files:
   - `FeatureScreen.kt` - Composable UI
   - `FeatureViewModel.kt` - State management
3. Add to `navigation/Screen.kt`:
   ```kotlin
   data object Feature : Screen("feature")
   ```
4. Add route to `NavGraph.kt`
5. If needed: create Use Case in `domain/usecase/`

### Adding a New API Endpoint

1. Add response model in `data/api/model/`
2. Add method to service in `data/api/XxxApiService.kt`
3. Update repository to use new endpoint
4. Add Use Case method if needed
5. Update ViewModel to call Use Case

### Adding a Database Table

1. Create entity in `data/entity/`
2. Create DAO in `data/dao/`
3. Update `AppDatabase.kt`:
   - Add to `entities` array
   - Increment `version`
   - Add migration if needed
4. Provide DAO in `DatabaseModule.kt`
5. Create repository in `data/repository/`

### Adding Background Work

1. Create Worker in `notification/`:
   ```kotlin
   @HiltWorker
   class FeatureWorker @AssistedInject constructor(
       @Assisted context: Context,
       @Assisted params: WorkerParameters,
       private val useCase: UseCase
   ) : CoroutineWorker(context, params) {
       override suspend fun doWork(): Result {
           // Work here
           return Result.success()
       }
   }
   ```
2. Schedule in `NotificationScheduler.kt`
3. Call scheduler from `MyFinancesApplication.kt`

---

## Testing Guidelines

### ViewModel Tests

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class FeatureViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var useCase: UseCase
    private lateinit var viewModel: FeatureViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        useCase = mockk()
        every { useCase.getData() } returns flowOf(testData)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test should verify behavior`() = runTest {
        viewModel = FeatureViewModel(useCase)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(expected, state.data)
        }
    }
}
```

**Important:**
- Mock all dependencies with MockK
- Use `runTest` for coroutine tests
- Use `turbine` for Flow testing
- Always test loading and error states

### Running Tests

```bash
# All tests
./gradlew test

# Specific test class
./gradlew test --tests FeatureViewModelTest

# Debug build tests only
./gradlew testDebugUnitTest
```

---

## Commit Conventions

### Format

```
Brief description (imperative mood)

Detailed explanation:
- Change 1
- Change 2
- Change 3

🤖 Generated with [Claude Code](https://claude.com/claude-code)

Co-Authored-By: Claude <noreply@anthropic.com>
```

### Examples

**Good:**
```
Add exchange rate refresh UI to Settings screen

Changes:
- Add refreshExchangeRates() method to SettingsViewModel
- Add Exchange Rates card to Settings screen
- Display last updated timestamp
- Show success/error messages with auto-dismiss
```

**Bad:**
```
Updated stuff
```

### Commit Guidelines

- One logical change per commit
- Descriptive first line (50 chars or less)
- Detailed explanation in body
- Reference issue numbers if applicable
- Always add Claude Code attribution (if using AI)

---

## API Integration: Frankfurter

### Base URL
`https://api.frankfurter.dev/v1/`

### Key Endpoint
`GET /latest?base=USD`

**Response:**
```json
{
  "base": "USD",
  "date": "2025-10-29",
  "rates": {
    "EUR": 0.85,
    "GBP": 0.75,
    ...
  }
}
```

**Features:**
- No authentication required
- Free, no rate limits
- Daily updates from ECB (~16:00 CET)
- 30+ currencies supported

**Important:**
- API returns USD → Currency rates
- We store Currency → USD rates (inverted)
- Use `1.0 / apiRate` when storing

---

## Important Files Reference

### Configuration
- `build.gradle.kts` - Dependencies and build config
- `gradle/libs.versions.toml` - Version catalog
- `AndroidManifest.xml` - Permissions and app config

### Core Application
- `MyFinancesApplication.kt` - App startup, DI, workers
- `MainActivity.kt` - Single activity host
- `ui/navigation/NavGraph.kt` - Navigation setup

### Data Layer
- `data/database/AppDatabase.kt` - Room database
- `data/repository/` - All repositories
- `data/api/FrankfurterApiService.kt` - API client

### Domain Layer
- `domain/Currency.kt` - 30+ currency definitions
- `domain/usecase/` - Business logic

### Key ViewModels
- `DashboardViewModel` - Portfolio analytics
- `SettingsViewModel` - App preferences
- `AccountListViewModel` - Account management

---

## Common Pitfalls & Solutions

### 1. State Updates Not Working

❌ **Wrong:**
```kotlin
_uiState.value.copy(field = newValue) // Doesn't emit
```

✅ **Correct:**
```kotlin
_uiState.update { it.copy(field = newValue) }
```

### 2. Flow Not Collecting

❌ **Wrong:**
```kotlin
init {
    repository.getData() // Not collected
}
```

✅ **Correct:**
```kotlin
init {
    viewModelScope.launch {
        repository.getData().collect { data ->
            _uiState.update { it.copy(data = data) }
        }
    }
}
```

### 3. Hilt Injection in Tests

❌ **Wrong:**
```kotlin
val viewModel = FeatureViewModel(useCase)
// Missing dependencies
```

✅ **Correct:**
```kotlin
val viewModel = FeatureViewModel(
    useCase1,
    useCase2,
    repository
)
// All dependencies provided
```

### 4. Room Entities in UI

❌ **Wrong:**
```kotlin
// Using entity directly in UI
@Composable
fun Screen(account: Account)
```

✅ **Correct:**
```kotlin
// Use domain models or UI state
data class AccountUiModel(
    val name: String,
    val value: Double,
    val formattedValue: String
)
```

---

## External Resources

- **Frankfurter API Docs:** https://frankfurter.dev
- **Vico Charts:** https://github.com/patrykandpatrick/vico
- **Material 3 Guidelines:** https://m3.material.io
- **Compose Docs:** https://developer.android.com/jetpack/compose
- **Hilt Guide:** https://dagger.dev/hilt/

---

## Notes for Claude Code

### When Adding Features

1. **Follow existing patterns** - Review similar features first
2. **Update tests** - Don't break existing tests
3. **Update docs** - Update README.md and FEATURES.md
4. **Small commits** - One logical change per commit
5. **Build verification** - Always run `./gradlew build` before committing

### When Fixing Bugs

1. **Understand the bug** - Read error messages carefully
2. **Check related code** - Bug might be in caller, not callee
3. **Add tests** - Prevent regression
4. **Minimal changes** - Fix only what's broken

### When Refactoring

1. **Don't break tests** - Green → Refactor → Green
2. **Incremental changes** - Small, reviewable commits
3. **Preserve behavior** - No feature changes during refactor
4. **Update docs** - If public API changes

---

**Last Updated:** October 29, 2025
**For Questions:** See README.md, ARCHITECTURE.md, FEATURES.md, or DEVELOPMENT.md
