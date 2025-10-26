# My Finances - Development Guide

## Getting Started

### Prerequisites
- **Android Studio:** Hedgehog (2023.1.1) or later
- **JDK:** 11 or later
- **Gradle:** 8.11.2
- **Kotlin:** 2.0.21
- **Min SDK:** 28 (Android 9.0)
- **Target SDK:** 36 (Android 14.0)

### Project Setup

1. **Clone the repository:**
```bash
git clone <repository-url>
cd MyFinances
```

2. **Open in Android Studio:**
   - File → Open → Select project directory
   - Wait for Gradle sync

3. **Build the project:**
```bash
./gradlew build
```

4. **Run on device/emulator:**
   - Connect device or start emulator
   - Click Run (Shift + F10)

---

## Project Configuration

### Gradle Structure

**Root `build.gradle.kts`:**
```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
}
```

**App `build.gradle.kts`:**
```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}
```

**Version Catalog (`gradle/libs.versions.toml`):**
```toml
[versions]
kotlin = "2.0.21"
composeBom = "2024.12.01"
room = "2.6.1"
navigation = "2.8.5"

[libraries]
# All dependencies centralized here

[plugins]
android-application = { ... }
kotlin-android = { ... }
kotlin-compose = { ... }
ksp = { ... }
```

### Build Configuration

**Namespace:** `dev.aulianenko.myfinances`
**Application ID:** `dev.aulianenko.myfinances`

**Build Types:**
- **Debug:** Default, no minification
- **Release:** ProGuard enabled (not configured yet)

**Compile Options:**
- Source: Java 11
- Target: Java 11
- JVM Target: 11

---

## Development Workflow

### Feature Development Process

1. **Create User Story**
   - Define clear requirements
   - Identify acceptance criteria
   - Plan data model changes

2. **Implement Data Layer**
   ```kotlin
   // 1. Create/update entity
   @Entity(tableName = "table_name")
   data class Entity(...)

   // 2. Create/update DAO
   @Dao
   interface EntityDao { ... }

   // 3. Update database version if needed
   @Database(version = X)

   // 4. Update/create repository
   class EntityRepository(private val dao: EntityDao)
   ```

3. **Implement Domain Layer (if needed)**
   ```kotlin
   // Create domain models
   data class DomainModel(...)

   // Create use case for complex logic
   class FeatureUseCase(private val repository: Repository)
   ```

4. **Implement Presentation Layer**
   ```kotlin
   // 1. Create UI State
   data class FeatureUiState(...)

   // 2. Create ViewModel
   class FeatureViewModel(private val repository: Repository) : ViewModel()

   // 3. Create Screen
   @Composable
   fun FeatureScreen(...)
   ```

5. **Wire Navigation**
   ```kotlin
   // 1. Add to Screen.kt
   data object Feature : Screen("feature_route")

   // 2. Add to NavGraph.kt
   composable(Screen.Feature.route) {
       FeatureScreen(...)
   }
   ```

6. **Test Feature**
   - Manual testing on device/emulator
   - Verify all edge cases
   - Check error handling

7. **Commit**
   ```bash
   git add .
   git commit -m "Story X.Y: Feature description

   - Change 1
   - Change 2
   - Change 3"
   ```

### Code Style

**File Naming:**
- Screens: `FeatureScreen.kt`
- ViewModels: `FeatureViewModel.kt`
- Repositories: `FeatureRepository.kt`
- DAOs: `FeatureDao.kt`
- Entities: `Feature.kt`

**Package Organization:**
```
feature/
  ├── FeatureScreen.kt
  ├── FeatureViewModel.kt
  └── FeatureUiState.kt (often in ViewModel file)
```

**Composable Naming:**
```kotlin
// Screen-level: PascalCase with "Screen"
@Composable
fun FeatureScreen() { }

// Component-level: PascalCase
@Composable
fun FeatureCard() { }

// Internal: PascalCase
@Composable
private fun FeatureItem() { }
```

**State Classes:**
```kotlin
data class FeatureUiState(
    val data: Data? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
```

---

## Database Management

### Schema Migrations

**Current Version:** 1

**When to Migrate:**
1. Add new table
2. Add/remove/modify columns
3. Change relationships

**Migration Process:**
```kotlin
@Database(
    entities = [Entity1::class, Entity2::class],
    version = 2,  // Increment
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    // Add migration
    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // SQL migration statements
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(...)
                .addMigrations(MIGRATION_1_2)
                .build()
        }
    }
}
```

**Current Approach:**
- `.fallbackToDestructiveMigration()` - Wipes data on schema changes
- Acceptable for development
- **TODO:** Implement proper migrations for production

### Inspecting Database

**Using Android Studio:**
1. Run app on emulator/device
2. View → Tool Windows → App Inspection
3. Select Database Inspector
4. View tables and data

**Using ADB:**
```bash
adb shell
cd /data/data/dev.aulianenko.myfinances/databases/
sqlite3 myfinances_database

.tables
.schema accounts
SELECT * FROM accounts;
```

---

## Debugging

### Logging

**Current:** No structured logging
**Recommendation:** Add Timber

```kotlin
// Add to build.gradle
implementation("com.jakewharton.timber:timber:5.0.1")

// Initialize in Application
class MyFinancesApp : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}

// Usage
Timber.d("Debug message")
Timber.e("Error message")
```

### Common Issues

#### 1. **Build Fails - Room Compiler**
**Error:** `error: Cannot find symbol class...Dao`

**Solution:**
```bash
./gradlew clean build
# Or in Android Studio:
Build → Clean Project
Build → Rebuild Project
```

#### 2. **Navigation Not Working**
**Check:**
- Route strings match exactly
- Arguments passed correctly
- NavController in scope

**Debug:**
```kotlin
// Log navigation events
navController.addOnDestinationChangedListener { _, destination, _ ->
    Timber.d("Navigated to: ${destination.route}")
}
```

#### 3. **UI Not Updating**
**Check:**
- Flow collection in Composable
- StateFlow properly exposed
- `collectAsState()` used

**Debug:**
```kotlin
// Log state changes
init {
    viewModelScope.launch {
        _uiState.collect { state ->
            Timber.d("State updated: $state")
        }
    }
}
```

#### 4. **Database Empty on App Restart**
**Possible Causes:**
- Using in-memory database (wrong)
- Database path incorrect
- Context is test context

**Verify:**
```kotlin
Room.databaseBuilder(
    context.applicationContext,  // Use applicationContext
    AppDatabase::class.java,
    "myfinances_database"  // Named database (not in-memory)
)
```

---

## Testing

### Unit Testing (TODO)

**Framework:** JUnit + MockK

**Example:**
```kotlin
class AccountRepositoryTest {
    private lateinit var accountDao: AccountDao
    private lateinit var repository: AccountRepository

    @Before
    fun setup() {
        accountDao = mockk()
        repository = AccountRepository(accountDao, mockk())
    }

    @Test
    fun `getAllAccounts returns flow of accounts`() = runTest {
        // Given
        val accounts = listOf(Account(...))
        every { accountDao.getAllAccounts() } returns flowOf(accounts)

        // When
        val result = repository.getAllAccounts().first()

        // Then
        assertEquals(accounts, result)
    }
}
```

### UI Testing (TODO)

**Framework:** Compose Testing + Espresso

**Example:**
```kotlin
class AccountListScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun emptyStateShown_whenNoAccounts() {
        composeTestRule.setContent {
            AccountListScreen(
                onNavigateToAddAccount = {},
                onNavigateToAccountDetail = {}
            )
        }

        composeTestRule
            .onNodeWithText("No Accounts Yet")
            .assertIsDisplayed()
    }
}
```

### Database Testing (TODO)

**Framework:** Room In-Memory Database

**Example:**
```kotlin
@RunWith(AndroidJUnit4::class)
class AccountDaoTest {
    private lateinit var database: AppDatabase
    private lateinit var accountDao: AccountDao

    @Before
    fun createDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).build()
        accountDao = database.accountDao()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun insertAndRetrieveAccount() = runBlocking {
        val account = Account(name = "Test", currency = "USD")
        accountDao.insertAccount(account)

        val accounts = accountDao.getAllAccounts().first()
        assertEquals(1, accounts.size)
        assertEquals("Test", accounts[0].name)
    }
}
```

---

## Performance Optimization

### Current Optimizations

1. **Lazy Loading**
   - LazyColumn for lists
   - Load only visible items

2. **Flow-based Queries**
   - Reactive updates
   - No manual refresh needed

3. **Database Indexing**
   - Foreign keys indexed
   - Primary keys indexed

4. **State Hoisting**
   - Minimal recomposition
   - Stable data structures

### Potential Improvements

1. **Pagination**
   - Paging 3 for large lists
   - Not needed yet

2. **Image Optimization**
   - Coil for images (when added)
   - Proper sizing

3. **Background Work**
   - WorkManager for scheduled tasks
   - Not needed yet

---

## Build Variants

### Current Configuration

**Debug:**
- Default build type
- Debugging enabled
- No code shrinking
- Logs enabled

**Release:**
- ProGuard rules defined but minimal
- Code shrinking ready
- Logs disabled (TODO)

### Adding Build Flavors (Future)

```kotlin
android {
    flavorDimensions += "environment"
    productFlavors {
        create("dev") {
            dimension = "environment"
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
        }
        create("prod") {
            dimension = "environment"
        }
    }
}
```

---

## Dependency Management

### Adding New Dependencies

1. **Add to version catalog:**
```toml
[versions]
new-lib = "1.0.0"

[libraries]
new-lib = { group = "com.example", name = "lib", version.ref = "new-lib" }
```

2. **Add to app/build.gradle.kts:**
```kotlin
dependencies {
    implementation(libs.new.lib)
}
```

3. **Sync Gradle**

### Updating Dependencies

```bash
# Check for updates
./gradlew dependencyUpdates

# Update version catalog
# Then sync Gradle
```

---

## Git Workflow

### Branch Strategy

**Current:** Direct commits to `main`
**Recommended for Team:**
```
main (production)
  ↑
develop (integration)
  ↑
feature/story-x-y (features)
```

### Commit Messages

**Format:**
```
Story X.Y: Brief description

- Detailed change 1
- Detailed change 2
- Detailed change 3
```

**Examples:**
```
Story 2.1: Create AccountRepository and domain models

- Implement AccountRepository with account and account value operations
- Add Currency domain model with 26 major world currencies
- Create CurrencyProvider for currency lookup and listing
```

### Reverting Features

**Revert a commit:**
```bash
git revert <commit-hash>
```

**Reset to commit (careful):**
```bash
git reset --hard <commit-hash>
```

---

## Continuous Integration (TODO)

### GitHub Actions Example

```yaml
name: Android CI

on: [push, pull_request]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 11
        uses: actions/setup-java@v3
        with:
          java-version: '11'
      - name: Build with Gradle
        run: ./gradlew build
      - name: Run tests
        run: ./gradlew test
```

---

## Release Process (TODO)

### Steps

1. **Update version:**
```kotlin
// app/build.gradle.kts
defaultConfig {
    versionCode = 2  // Increment
    versionName = "1.1"  // Update
}
```

2. **Build release:**
```bash
./gradlew assembleRelease
```

3. **Sign APK:**
   - Configure signing in build.gradle
   - Use keystore

4. **Test release build:**
```bash
./gradlew installRelease
```

5. **Generate changelog:**
```bash
git log --oneline v1.0..HEAD
```

6. **Tag release:**
```bash
git tag -a v1.1 -m "Version 1.1"
git push origin v1.1
```

---

## Troubleshooting

### Gradle Sync Issues

```bash
# Clear Gradle cache
rm -rf ~/.gradle/caches/

# Invalidate caches in Android Studio
File → Invalidate Caches / Restart
```

### ADB Issues

```bash
# Restart ADB
adb kill-server
adb start-server

# List devices
adb devices
```

### Emulator Issues

```bash
# List emulators
emulator -list-avds

# Start emulator
emulator -avd <name>

# Cold boot
emulator -avd <name> -no-snapshot-load
```

---

## Resources

### Documentation
- [Kotlin Docs](https://kotlinlang.org/docs/)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Room Database](https://developer.android.com/training/data-storage/room)
- [Material 3](https://m3.material.io/)

### Code Examples
- [Compose Samples](https://github.com/android/compose-samples)
- [Architecture Samples](https://github.com/android/architecture-samples)

### Tools
- [Android Studio](https://developer.android.com/studio)
- [Gradle](https://gradle.org/)
- [KSP](https://kotlinlang.org/docs/ksp-overview.html)

---

## Contributing

### Code Review Checklist

- [ ] Follows MVVM pattern
- [ ] Proper error handling
- [ ] Input validation
- [ ] No hardcoded strings (use strings.xml)
- [ ] Composables are pure functions
- [ ] State properly hoisted
- [ ] Navigation properly wired
- [ ] Commit message descriptive

### Pull Request Template (Future)

```markdown
## Story X.Y: Title

### Description
Brief description of changes

### Changes
- Change 1
- Change 2

### Testing
- [ ] Manual testing done
- [ ] Unit tests added/updated
- [ ] UI tests added/updated

### Screenshots
(if UI changes)
```

---

**Last Updated:** October 26, 2025
**Project Version:** 1.0
