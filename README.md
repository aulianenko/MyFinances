# My Finances 📊

A modern Android app for tracking personal finances across multiple accounts with different currencies. Built with Jetpack Compose, Room Database, and Material 3 design.

![Android](https://img.shields.io/badge/Platform-Android-green.svg)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple.svg)
![Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-blue.svg)
![Material3](https://img.shields.io/badge/Design-Material%203-orange.svg)

## Features

### ✅ Implemented

- **Account Management** - Create, edit, delete accounts with multi-currency support
- **Value Tracking** - Record account values with timestamps and notes
- **Bulk Updates** - Update multiple accounts simultaneously
- **Dashboard Analytics** - Portfolio overview with statistics across different time periods
- **Historical Data** - View complete value history for each account
- **Multi-Currency** - Support for 26 major world currencies
- **Dark Mode** - Full Material 3 theming with dark mode support
- **Bottom Navigation** - Intuitive navigation between main sections

### 🚧 Planned

- **Charts & Visualizations** - Line charts and pie charts for data visualization
- **Biometric Authentication** - Secure app with fingerprint/face unlock
- **Settings & Preferences** - Customizable app behavior and theme toggle

## Screenshots

_Coming soon..._

## Technical Stack

- **Language:** Kotlin 2.0.21
- **UI Framework:** Jetpack Compose with Material 3
- **Architecture:** MVVM (Model-View-ViewModel)
- **Database:** Room 2.6.1
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
├── domain/
│   ├── model/            # Domain models
│   ├── usecase/          # Business use cases
│   └── Currency.kt       # Currency definitions
└── ui/
    ├── components/       # Reusable UI components
    ├── navigation/       # Navigation setup
    ├── screens/          # Feature screens
    │   ├── account/      # Account management
    │   ├── accountvalue/ # Value tracking
    │   └── dashboard/    # Analytics dashboard
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
- `timestamp` (Long) - Value timestamp
- `note` (String?) - Optional note

### Relationships
- One Account → Many AccountValues
- Cascade delete on Account removal

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

- **1.0** (Current)
  - Account management
  - Value tracking
  - Bulk updates
  - Dashboard analytics
  - Multi-currency support

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- Built with [Jetpack Compose](https://developer.android.com/jetpack/compose)
- Icons from [Material Icons](https://fonts.google.com/icons)
- Architecture inspired by [Android Architecture Components](https://developer.android.com/topic/architecture)

## Contact

**Developer:** Andrii Ulianenko
**Project Link:** [GitHub Repository](#)

---

**Note:** This is an active development project. Features and documentation are continuously updated.