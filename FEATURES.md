# My Finances - Feature Documentation

## Overview

This document provides detailed information about all implemented features, their usage, and technical implementation details.

---

## Feature List

### ✅ Implemented Features

1. [Account Management](#1-account-management)
2. [Account Value Tracking](#2-account-value-tracking)
3. [Bulk Value Updates](#3-bulk-value-updates)
4. [Dashboard & Analytics](#4-dashboard--analytics)
5. [Navigation & UI](#5-navigation--ui)
6. [Multi-Currency Support](#6-multi-currency-support)
7. [Exchange Rate API Integration](#7-exchange-rate-api-integration)
8. [Notifications & Reminders](#8-notifications--reminders)

### 🚧 Planned Features

9. [Biometric Authentication](#9-biometric-authentication-planned)
10. [Export/Import](#10-exportimport-planned)
11. [Cloud Sync](#11-cloud-sync-planned)

---

## 1. Account Management

### Description
Create, view, edit, and delete financial accounts with multi-currency support.

### User Stories
- As a user, I want to create accounts for different financial assets
- As a user, I want to edit account details
- As a user, I want to delete accounts I no longer need
- As a user, I want to see all my accounts at a glance

### Features

#### 1.1 Create Account
**Screen:** `AddAccountScreen`
**Navigation:** Account List → FAB → Add Account

**Fields:**
- Account Name (required)
- Currency (required, dropdown with 26 currencies)

**Validation:**
- Name cannot be empty
- Currency must be selected from list

**Technical:**
- ViewModel: `AddAccountViewModel`
- Repository method: `insertAccount()`
- Auto-generates UUID
- Sets creation timestamp

#### 1.2 View Accounts
**Screen:** `AccountListScreen`
**Navigation:** Bottom Nav → Accounts

**Display:**
- Account name
- Current value (if exists) or "No value recorded"
- Currency code and full name
- Edit and Delete buttons

**Features:**
- Reactive updates (Flow-based)
- Click to view details
- Pull latest value for each account

**Technical:**
- ViewModel: `AccountListViewModel`
- Combines Account + latest AccountValue
- Empty state handling

#### 1.3 Edit Account
**Screen:** `EditAccountScreen`
**Navigation:** Account List → Edit button

**Editable:**
- Account name
- Currency

**Features:**
- Pre-populated with current values
- Updates `updatedAt` timestamp
- Validation same as create

**Technical:**
- ViewModel: `EditAccountViewModel`
- Loads account by ID
- Updates existing entity

#### 1.4 Delete Account
**Location:** Account List item
**Action:** Delete icon button

**Behavior:**
- Deletes account immediately
- Cascade deletes all associated values
- No confirmation dialog (consider adding)

**Technical:**
- Room cascade delete via ForeignKey
- Reactive UI update

---

## 2. Account Value Tracking

### Description
Record and track account values over time with optional notes.

### User Stories
- As a user, I want to record the current value of an account
- As a user, I want to add notes to value updates
- As a user, I want to see historical values
- As a user, I want to delete incorrect value entries

### Features

#### 2.1 Add Account Value
**Screen:** `AddAccountValueScreen`
**Navigation:** Account Detail → FAB

**Fields:**
- Current Value (required, decimal)
- Note (optional, multiline)

**Validation:**
- Value cannot be empty
- Must be valid decimal number
- Cannot be negative

**Features:**
- Shows account name and currency
- Displays currency symbol in label
- Timestamp auto-generated

**Technical:**
- ViewModel: `AddAccountValueViewModel`
- Validates numeric input
- Stores with current timestamp

#### 2.2 View Value History
**Screen:** `AccountDetailScreen`
**Navigation:** Account List → Click account

**Display:**
- Current value (large, highlighted)
- Historical values (chronological list)
- Each entry shows:
  - Value with currency symbol
  - Date and time
  - Optional note
  - Delete button

**Features:**
- Real-time updates
- Empty state when no values
- Formatted currency display
- Formatted timestamps

**Technical:**
- ViewModel: `AccountDetailViewModel`
- Combines account + values
- Sorts by timestamp descending

#### 2.3 Delete Value Entry
**Location:** Account Detail screen
**Action:** Delete icon on value item

**Behavior:**
- Removes single value entry
- Updates current value display
- Immediate reactive update

---

## 3. Bulk Value Updates

### Description
Update values for multiple accounts simultaneously with a shared timestamp.

### User Stories
- As a user, I want to update all my accounts at once
- As a user, I want to skip accounts I don't want to update
- As a user, I want to add a single note for all updates

### Features

#### 3.1 Bulk Update Screen
**Screen:** `BulkUpdateScreen`
**Navigation:** Dashboard → FAB

**Display:**
- Input card for each account
- Account name and currency
- Value input field
- Shared note field (bottom)

**Features:**
- Cards highlight when value entered
- Skip accounts by leaving empty
- Validates all inputs before saving
- All updates share timestamp
- Optional shared note

**Validation:**
- At least one value required
- Each value must be valid number
- No negative values
- Shows errors per account

**Technical:**
- ViewModel: `BulkUpdateViewModel`
- State: `Map<accountId, value>`
- Batch insert with single timestamp

#### 3.2 Card Highlighting
**Behavior:**
- Default: surface color
- With value: primaryContainer color
- Provides visual feedback

**Technical:**
- State tracking: `hasValue` flag
- Conditional card background

---

## 4. Dashboard & Analytics

### Description
Portfolio overview with statistics across different time periods.

### User Stories
- As a user, I want to see my portfolio at a glance
- As a user, I want to track performance over time
- As a user, I want to analyze different time periods

### Features

#### 4.1 Dashboard Overview
**Screen:** `DashboardScreen`
**Navigation:** Bottom Nav → Dashboard (default)

**Display:**
- Total accounts count (card)
- Time period selector
- Per-account statistics (list)

**Period Options:**
- 3 Months
- 6 Months
- 1 Year
- All Time (Max)

**Technical:**
- ViewModel: `DashboardViewModel`
- UseCase: `CalculateStatisticsUseCase`
- Reactive to period changes

#### 4.2 Account Statistics Card
**Display per account:**
- Account name
- Current value (large, bold)
- Value change (with sign)
- Percentage change (with sign)
- Number of updates in period
- Color coding: green (positive) / red (negative)

**Click Behavior:**
- Navigate to Account Detail

**Technical:**
- Calculates: current - first in period
- Percentage: ((current - first) / first) * 100
- Handles missing data gracefully

#### 4.3 Statistics Calculation
**Formula:**
```
First Value = Oldest value in selected period
Current Value = Latest value overall
Change = Current - First
% Change = (Change / First) * 100
```

**Edge Cases:**
- No values: shows 0
- Only one value: no change shown
- First value is 0: percentage not calculated

**Time Period Filtering:**
```kotlin
fun getStartTimestamp(): Long = when (period) {
    THREE_MONTHS -> now - 3 months
    SIX_MONTHS -> now - 6 months
    ONE_YEAR -> now - 1 year
    MAX -> 0 (all time)
}
```

---

## 5. Navigation & UI

### Description
Bottom navigation with Material 3 design and dark mode support.

### Features

#### 5.1 Bottom Navigation
**Tabs:**
1. **Dashboard** (Home icon)
   - Portfolio overview
   - Default screen
2. **Accounts** (List icon)
   - Account management
3. **Settings** (Settings icon)
   - App configuration (placeholder)

**Behavior:**
- State preservation per tab
- Single-top navigation
- Back stack per section

**Technical:**
- NavigationBar with NavigationBarItem
- NavController with saveState/restoreState
- Visibility: shown only on main screens

#### 5.2 Top App Bar
**Component:** `AppTopBar`

**Features:**
- Title display
- Optional back navigation
- Optional action buttons
- Consistent Material 3 styling

**Usage:**
```kotlin
AppTopBar(
    title = "Screen Title",
    canNavigateBack = true,
    onNavigateBack = { },
    actions = { IconButton(...) }
)
```

#### 5.3 Theme System
**Support:**
- Light mode
- Dark mode (system default)
- Dynamic colors (Android 12+)

**Implementation:**
- Material 3 color system
- Theme.kt with MaterialTheme
- System theme detection

---

## 6. Multi-Currency Support

### Description
Support for 26 major world currencies with proper symbols and names.

### Supported Currencies
```
USD ($) - US Dollar
EUR (€) - Euro
GBP (£) - British Pound
JPY (¥) - Japanese Yen
CHF (CHF) - Swiss Franc
CAD (C$) - Canadian Dollar
AUD (A$) - Australian Dollar
CNY (¥) - Chinese Yuan
INR (₹) - Indian Rupee
BRL (R$) - Brazilian Real
RUB (₽) - Russian Ruble
KRW (₩) - South Korean Won
MXN ($) - Mexican Peso
SGD (S$) - Singapore Dollar
HKD (HK$) - Hong Kong Dollar
NOK (kr) - Norwegian Krone
SEK (kr) - Swedish Krona
DKK (kr) - Danish Krone
PLN (zł) - Polish Zloty
THB (฿) - Thai Baht
IDR (Rp) - Indonesian Rupiah
CZK (Kč) - Czech Koruna
ILS (₪) - Israeli Shekel
ZAR (R) - South African Rand
TRY (₺) - Turkish Lira
UAH (₴) - Ukrainian Hryvnia
```

### Features
- Dropdown selection in account creation/editing
- Symbol display in value fields
- Full name in account details
- No automatic conversion (each account independent)

### Technical Implementation
```kotlin
data class Currency(
    val code: String,   // "USD"
    val symbol: String, // "$"
    val name: String    // "US Dollar"
)

object CurrencyProvider {
    val currencies: List<Currency>
    fun getCurrencyByCode(code: String): Currency?
}
```

---

## 7. Exchange Rate API Integration

### Description
Automatic and manual updates of exchange rates from the Frankfurter API (European Central Bank) to ensure accurate multi-currency conversions.

### User Stories
- As a user, I want exchange rates to update automatically so I don't have to manage them
- As a user, I want to manually refresh rates when needed
- As a user, I want to see when rates were last updated
- As a user, I want accurate currency conversions in my portfolio

### Features

#### 7.1 Automatic Daily Updates
**Implementation:** `ExchangeRateWorker` (WorkManager)

**Behavior:**
- Runs once per day automatically
- Fetches latest rates from Frankfurter API
- Updates all 30+ supported currencies
- Retries on failure
- Runs in background, no user interaction

**Technical:**
- WorkManager periodic work request
- Scheduled on app startup
- ExistingPeriodicWorkPolicy.KEEP (doesn't duplicate)
- API endpoint: `https://api.frankfurter.dev/v1/latest?base=USD`

#### 7.2 Manual Refresh
**Screen:** Settings → Exchange Rates card
**Action:** "Refresh Exchange Rates" button

**Features:**
- Loading indicator during refresh
- Success message with count of rates updated
- Error message if API call fails
- Displays last updated timestamp
- Messages auto-dismiss (3s for success, 5s for errors)

**Technical:**
- ViewModel: `SettingsViewModel.refreshExchangeRates()`
- UseCase: `CurrencyConversionUseCase.updateExchangeRatesFromApi()`
- Repository: `ExchangeRateRepository.updateExchangeRatesFromApi()`

#### 7.3 Exchange Rate Display
**Location:** Settings screen

**Shows:**
- Last updated date and time
- Formatted as: "MMM dd, yyyy HH:mm"
- Example: "Oct 29, 2025 14:30"
- Shows "Never" if no rates exist

#### 7.4 API Integration
**API:** Frankfurter (https://frankfurter.dev)
**Base URL:** `https://api.frankfurter.dev/v1/`
**Endpoint:** `/latest?base=USD`

**Features:**
- No authentication required
- Free for all usage levels
- Daily updates from ECB (around 16:00 CET)
- No rate limits for reasonable use
- Returns rates for 30+ currencies

**Response Format:**
```json
{
  "base": "USD",
  "date": "2025-10-29",
  "rates": {
    "EUR": 0.85,
    "GBP": 0.75,
    "JPY": 149.50,
    ...
  }
}
```

**Technical Stack:**
- Retrofit 2.11.0 for API calls
- OkHttp 4.12.0 for HTTP client
- Moshi 1.15.1 for JSON parsing
- NetworkModule (Hilt) for DI

#### 7.5 Rate Storage
**Database:** Room (ExchangeRate table)

**Fields:**
- `id` (String) - UUID
- `currencyCode` (String) - Unique index
- `rateToUSD` (Double) - Exchange rate
- `lastUpdated` (Long) - Timestamp

**Conversion:**
- API returns: USD → Currency rate
- We store: Currency → USD rate (inverted)
- Reason: Easier conversion calculations

#### 7.6 Error Handling
**Network Errors:**
- No internet connection
- API timeout (30s)
- API unavailable

**Fallback:**
- Uses last cached rates
- Shows error message to user
- WorkManager retries failed updates

**User Feedback:**
- Success: Green card with count
- Error: Red card with error message
- Auto-dismiss after timeout

---

## 8. Notifications & Reminders

### Description
Configurable reminder notifications to prompt users to update their portfolio values regularly.

### User Stories
- As a user, I want reminders to update my portfolio
- As a user, I want to choose how often I get reminders
- As a user, I want to enable/disable notifications
- As a user, I want to know which accounts need updating

### Features

#### 8.1 Notification Settings
**Screen:** Settings → Notifications card

**Options:**
- Enable/Disable notifications (toggle)
- Reminder frequency selection
- Permission handling (Android 13+)

**Frequencies:**
- Every 3 days
- Weekly (7 days)
- Bi-weekly (14 days)
- Monthly (30 days)

**Technical:**
- DataStore preferences
- Permission: POST_NOTIFICATIONS (Android 13+)
- Permission launcher in Settings screen

#### 8.2 Reminder Worker
**Implementation:** `ReminderWorker` (WorkManager + Hilt)

**Behavior:**
- Checks all accounts for updates
- Compares last update vs. threshold
- Counts accounts needing updates
- Shows notification if any accounts outdated

**Logic:**
```kotlin
threshold = now - frequency_days
for each account:
  if last_update < threshold:
    accountsNeedingUpdate++
```

**Technical:**
- Periodic WorkManager task
- Frequency matches user preference
- Runs in background
- Comprehensive logging

#### 8.3 Notification Display
**Title:** "Portfolio Update Reminder"
**Content:** "N account(s) need updating"
**Icon:** App icon
**Channel:** "Reminders" (importance: DEFAULT)

**Features:**
- Deep link to app (when tapped)
- Dismissible
- Sound/vibration based on system settings

**Technical:**
- NotificationHelper creates notifications
- NotificationScheduler manages WorkManager
- Channel created on app startup

#### 8.4 Permission Handling
**Android 13+ (API 33+):**
- Requires POST_NOTIFICATIONS permission
- Shows permission dialog when enabling
- Falls back to disabled if denied

**Android 12 and below:**
- No permission required
- Works automatically

**Implementation:**
- ActivityResultContracts.RequestPermission
- Permission state stored in preferences

#### 8.5 Scheduling
**Initial Schedule:**
- Set on app startup (if enabled)
- Restored from saved preferences

**Updates:**
- When user changes frequency
- When user enables/disables
- Cancels old work, creates new

**Technical:**
```kotlin
WorkManager.enqueueUniquePeriodicWork(
    name = "portfolio_reminder_work",
    policy = ExistingPeriodicWorkPolicy.UPDATE,
    request = PeriodicWorkRequest(frequency)
)
```

---

## 9. Biometric Authentication (Planned)

### Planned Features

#### 9.1 App Lock
- Biometric authentication on launch
- Fingerprint/Face unlock
- Fallback to PIN/pattern

#### 9.2 Setup Flow
- Enable/disable in settings
- Configure fallback method

---

## 10. Export/Import (Planned)

### Planned Features

#### 10.1 Data Export
- CSV format for accounts and values
- JSON format for complete backup
- Include all historical data
- Share or save to device

#### 10.2 Data Import
- Import from CSV/JSON
- Merge with existing data
- Validation and error handling

#### 10.3 Scheduled Backups
- Automatic periodic exports
- Cloud storage integration (Google Drive, etc.)

---

## 11. Cloud Sync (Planned)

### Planned Features

#### 11.1 Multi-Device Sync
- Real-time synchronization
- Conflict resolution
- Backend service integration

#### 11.2 Account Management
- Sign in with Google/Email
- Encrypted cloud storage
- Selective sync options

---

## Technical Implementation Details

### Reactive Data Flow

```
Database (Room)
    ↓ (Flow)
Repository
    ↓ (Flow)
ViewModel (StateFlow)
    ↓ (collectAsState)
UI (Composable)
```

### Error Handling

**Current Approach:**
- Validation in ViewModels
- Error messages in UI state
- Snackbar for user feedback

**Pattern:**
```kotlin
data class UiState(
    val data: Data?,
    val isLoading: Boolean,
    val errorMessage: String?
)
```

### Input Validation

**Numeric Fields:**
```kotlin
val value = input.toDoubleOrNull() ?: return error
if (value < 0) return error
```

**Required Fields:**
```kotlin
if (input.isBlank()) return error
```

### State Management

**ViewModel Pattern:**
```kotlin
private val _uiState = MutableStateFlow(UiState())
val uiState: StateFlow<UiState> = _uiState.asStateFlow()

fun updateState() {
    _uiState.update { it.copy(field = newValue) }
}
```

---

## User Experience

### Loading States
- `LoadingIndicator` for async operations
- Skeleton screens (future enhancement)

### Empty States
- `EmptyState` component
- Contextual messages
- Clear call-to-action

### Error States
- Snackbar for temporary errors
- Error text in forms
- Validation messages

### Success States
- Auto-navigation on success
- Silent updates for saves
- Optimistic UI updates

---

## Performance Considerations

### Database Queries
- Indexed foreign keys
- Flow-based reactive queries
- Efficient joins avoided (separate queries combined in ViewModel)

### UI Rendering
- Lazy loading (LazyColumn)
- State hoisting
- Minimal recomposition

### Memory Management
- ViewModel lifecycle
- Flow collection with lifecycle
- Database singleton

---

## Accessibility

### Current Support
- Material 3 accessibility built-in
- Semantic content descriptions
- Scalable text

### Future Enhancements
- Screen reader testing
- High contrast mode
- Larger touch targets

---

## Data Persistence

### Local Storage Only
- Room SQLite database
- No cloud sync (yet)
- Data stays on device

### Data Lifecycle
- Accounts persist until deleted
- Values cascade delete with account
- No automatic cleanup

---

## Future Roadmap

### High Priority
1. Biometric authentication
2. Export/Import (CSV, JSON)
3. Budget tracking

### Medium Priority
1. Tags/categories for accounts
2. Goal setting
3. Custom date ranges for analytics

### Low Priority
1. Multi-device cloud sync
2. Transaction tracking (income vs expenses)
3. Tax reporting features

---

**Last Updated:** October 29, 2025
**App Version:** 1.0
**Features Implemented:** 8/11
