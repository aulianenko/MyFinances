# My Finances 📊

A modern Android app for tracking personal finances across multiple accounts with different currencies. Built with Jetpack Compose, Room Database, and Material 3 design.

![Android](https://img.shields.io/badge/Platform-Android-green.svg)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple.svg)
![Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-blue.svg)
![Material3](https://img.shields.io/badge/Design-Material%203-orange.svg)

## Features

### ✅ Implemented

- **Account Management** - Create, edit, delete accounts with multi-currency support
- **Value Tracking** - Record account values with custom dates and notes
- **Historical Dates** - Add values for past dates with date picker
- **Bulk Updates** - Update multiple accounts simultaneously with shared date
- **Dashboard Analytics** - Portfolio overview with statistics and trends
- **Charts & Visualizations** - Portfolio value trend and distribution charts
- **Historical Data** - View complete value history for each account with charts
- **Multi-Currency** - Support for 30+ world currencies with exchange rates
- **Currency Conversion** - Portfolio totals in base currency with real-time conversion
- **Currency Converter** - Utility to convert between any currencies in Settings
- **Settings Screen** - Base currency preference and currency converter
- **Advanced Analytics** - Performance metrics, volatility, and correlations
- **Scrollable Screens** - All screens properly scroll for any content size
- **Dark Mode** - Full Material 3 theming with dark mode support
- **Bottom Navigation** - Intuitive navigation between main sections

### 🚧 Planned

- **Biometric Authentication** - Secure app with fingerprint/face unlock
- **Export/Import** - Backup and restore data in CSV/JSON formats
- **Cloud Sync** - Multi-device synchronization
- **Budget Tracking** - Set and track budgets across accounts

## Screenshots

_Coming soon..._

## Technical Stack

- **Language:** Kotlin 2.0.21
- **UI Framework:** Jetpack Compose with Material 3
- **Architecture:** MVVM (Model-View-ViewModel)
- **Dependency Injection:** Hilt (Dagger)
- **Database:** Room 2.6.1
- **Charts:** Vico 2.0.0-alpha.28
- **Preferences:** DataStore 1.1.1
- **Navigation:** Navigation Compose 2.8.5
- **Minimum SDK:** 28 (Android 9.0)
- **Target SDK:** 36 (Android 14.0)

## Architecture

The app follows **Simple MVVM** architecture pattern:

```
┌─────────────────────────────────────────────┐
│              UI Layer (Compose)              │
│  - Screens                                   │
│  - ViewModels                                │
│  - Navigation                                │
└──────────────────┬──────────────────────────┘
                   │ StateFlow
┌──────────────────▼──────────────────────────┐
│           Domain Layer                       │
│  - Use Cases                                 │
│  - Business Logic                            │
│  - Domain Models                             │
└──────────────────┬──────────────────────────┘
                   │ Flow
┌──────────────────▼──────────────────────────┐
│            Data Layer                        │
│  - Repository                                │
│  - Room Database                             │
│  - DAOs                                      │
│  - Entities                                  │
└──────────────────────────────────────────────┘
```

### Key Design Decisions

1. **Simple MVVM** - No additional Clean Architecture layers for faster development
2. **Jetpack Compose** - Modern declarative UI framework
3. **Room Database** - Type-safe local persistence with Flow support
4. **StateFlow** - Reactive state management in ViewModels
5. **Bottom Navigation** - Material 3 navigation pattern

## Project Structure

```
app/src/main/java/dev/aulianenko/myfinances/
├── data/
│   ├── dao/              # Database Access Objects
│   ├── database/         # Room database configuration
│   ├── entity/           # Database entities
│   └── repository/       # Data repositories
├── di/                   # Hilt dependency injection modules
├── domain/
│   ├── model/            # Domain models
│   ├── usecase/          # Business use cases
│   └── Currency.kt       # Currency definitions
└── ui/
    ├── components/       # Reusable UI components (charts, inputs, etc.)
    ├── navigation/       # Navigation setup
    ├── screens/          # Feature screens
    │   ├── account/      # Account management
    │   ├── accountvalue/ # Value tracking
    │   ├── analytics/    # Advanced analytics
    │   ├── dashboard/    # Analytics dashboard
    │   └── settings/     # App settings
    └── theme/            # Material 3 theming
```

## Getting Started

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 11 or higher
- Android SDK with API 36

### Installation

1. Clone the repository
```bash
git clone <repository-url>
cd MyFinances
```

2. Open the project in Android Studio

3. Sync Gradle and build the project
```bash
./gradlew build
```

4. Run on device or emulator
```bash
./gradlew installDebug
```

## Development

### Building

```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease

# Run tests
./gradlew test
```

### Code Style

The project follows Kotlin coding conventions and uses:
- Kotlin Coroutines for async operations
- Flow for reactive streams
- Jetpack Compose for UI
- Material 3 components

### Testing

```bash
# Unit tests
./gradlew test

# Instrumented tests
./gradlew connectedAndroidTest
```

## Documentation

- **[ARCHITECTURE.md](ARCHITECTURE.md)** - Detailed architecture and design decisions
- **[FEATURES.md](FEATURES.md)** - Complete feature documentation
- **[DEVELOPMENT.md](DEVELOPMENT.md)** - Development guide and workflows

## Database Schema

### Entities

**Account**
- `id` (String) - UUID primary key
- `name` (String) - Account name
- `currency` (String) - Currency code (USD, EUR, etc.)
- `createdAt` (Long) - Creation timestamp
- `updatedAt` (Long) - Last update timestamp

**AccountValue**
- `id` (String) - UUID primary key
- `accountId` (String) - Foreign key to Account
- `value` (Double) - Account value
- `timestamp` (Long) - Value timestamp (user-selectable)
- `note` (String?) - Optional note

**ExchangeRate**
- `id` (String) - UUID primary key
- `currencyCode` (String) - Currency code (unique index)
- `rateToUSD` (Double) - Exchange rate to USD
- `lastUpdated` (Long) - Last update timestamp

### Relationships
- One Account → Many AccountValues
- Cascade delete on Account removal
- Exchange rates independent, default values initialized on first launch

## Supported Currencies

USD, EUR, GBP, JPY, CHF, CAD, AUD, CNY, INR, BRL, RUB, KRW, MXN, SGD, HKD, NOK, SEK, DKK, PLN, THB, IDR, CZK, ILS, ZAR, TRY, UAH

## Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Story X.Y: Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Commit Convention

Follow the format:
```
Story X.Y: Brief description

- Detailed change 1
- Detailed change 2
```

## Version History

- **1.0** (Current - October 2025)
  - Account management with CRUD operations
  - Value tracking with custom dates
  - Bulk updates with shared timestamps
  - Dashboard analytics with time period filtering
  - Charts & visualizations (line charts, pie charts)
  - Multi-currency support (30+ currencies)
  - Currency conversion with exchange rates
  - Currency converter utility in Settings
  - Advanced analytics (performance, volatility, correlations)
  - Base currency preference with DataStore
  - Hilt dependency injection
  - Scrollable screens for all content
  - Material 3 design with dark mode

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- Built with [Jetpack Compose](https://developer.android.com/jetpack/compose)
- Charts powered by [Vico](https://github.com/patrykandpatrick/vico)
- Dependency injection with [Hilt](https://dagger.dev/hilt/)
- Icons from [Material Icons](https://fonts.google.com/icons)
- Architecture inspired by [Android Architecture Components](https://developer.android.com/topic/architecture)

## Contact

**Developer:** Andrii Ulianenko
**Project Link:** [GitHub Repository](#)

---

**Note:** This is an active development project. Features and documentation are continuously updated.