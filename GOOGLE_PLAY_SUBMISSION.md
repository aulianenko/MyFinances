# Google Play Store Submission Guide

## Complete Checklist for Publishing My Finances

---

## Prerequisites

### 1. Google Play Developer Account

- [ ] Create account at https://play.google.com/console
- [ ] Pay one-time $25 registration fee
- [ ] Complete account verification
- [ ] Set up payment profile (for paid apps/in-app purchases)

### 2. App Signing

- [ ] Follow [KEYSTORE_SETUP.md](KEYSTORE_SETUP.md) to generate keystore
- [ ] Enroll in Google Play App Signing (recommended)
- [ ] Keep upload key secure

### 3. Build Signed Release

```bash
# Option 1: Command Line
./gradlew bundleRelease

# Option 2: Android Studio
Build → Generate Signed Bundle / APK → Android App Bundle
```

**Output:** `app/build/outputs/bundle/release/app-release.aab`

---

## Store Listing Assets

### Required Assets

#### 1. App Icon
- [x] Already exists at `app/src/main/res/mipmap-*/ic_launcher.png`
- Verify 512x512 high-res icon for Play Store
- Use Android Studio → Image Asset to generate if needed

#### 2. Feature Graphic
- [ ] Create 1024 x 500 px banner image
- Should showcase app name and key features
- Use Canva, Figma, or similar tool

#### 3. Screenshots (Required: At least 2)
**Phone Screenshots (Required):**
- [ ] Dashboard screen
- [ ] Account list screen
- [ ] Account detail with chart
- [ ] Add value screen
- [ ] Analytics screen
- [ ] Settings screen

**Tablet Screenshots (Optional but recommended):**
- [ ] Dashboard on tablet
- [ ] Multi-pane layouts if supported

**Screenshot Requirements:**
- JPEG or 24-bit PNG (no alpha)
- Min: 320px | Max: 3840px
- Max aspect ratio: 2:1
- Recommended: 1080 x 1920 px (portrait)

#### 4. Promotional Video (Optional)
- YouTube link to demo video
- 30-90 seconds recommended
- Show key features and user flow

---

## Store Listing Content

### App Title
```
My Finances - Portfolio Tracker
```
*Max 50 characters*

### Short Description
```
Track multiple financial accounts with multi-currency support, charts, and analytics. All data stored locally on your device.
```
*Max 80 characters*

### Full Description

```
My Finances - Personal Portfolio Tracker

Track your financial accounts across multiple currencies with beautiful charts, detailed analytics, and complete privacy.

🌟 KEY FEATURES

📊 Multi-Account Tracking
• Create unlimited financial accounts
• Support for 30+ world currencies
• Track stocks, crypto, savings, and investments

📈 Charts & Analytics
• Portfolio value trend charts
• Account distribution pie charts
• Performance metrics and volatility analysis
• Historical value tracking

💱 Currency Conversion
• Real-time currency conversion
• Portfolio totals in your base currency
• Built-in currency converter tool
• Support for USD, EUR, GBP, JPY, and more

📅 Historical Data
• Add values for past dates
• Complete value history for each account
• Track performance over time periods
• Export capabilities (coming soon)

🔒 Privacy First
• All data stored locally on your device
• No internet connection required
• No account registration
• No ads or tracking
• Open source

⚡ Modern Design
• Material 3 design
• Dark mode support
• Smooth animations
• Intuitive navigation

💪 POWERFUL ANALYTICS

• Portfolio growth tracking
• Best/worst performing accounts
• Time period filtering (3M, 6M, 1Y, All)
• Correlation analysis
• Volatility calculations

🎯 PERFECT FOR

• Individual investors
• Crypto portfolio tracking
• Multi-currency travelers
• Financial goal tracking
• Personal finance management

📱 BUILT WITH LATEST TECH

• Jetpack Compose UI
• Material 3 design
• Room database
• Vico charts library
• Kotlin coroutines

🔐 YOUR DATA, YOUR CONTROL

Unlike other finance apps:
✅ No data collection
✅ No cloud sync (your data stays on your device)
✅ No login required
✅ No permissions needed
✅ No third-party services
✅ Completely free

💼 USE CASES

• Track investment portfolios
• Monitor cryptocurrency holdings
• Manage multi-currency accounts
• Record real estate values
• Track precious metals
• Monitor business assets

📊 BULK UPDATES

Update all account values at once with shared timestamps - perfect for regular portfolio snapshots.

🌍 MULTI-CURRENCY SUPPORT

USD, EUR, GBP, JPY, CHF, CAD, AUD, CNY, INR, BRL, RUB, KRW, MXN, SGD, HKD, NOK, SEK, DKK, PLN, THB, IDR, CZK, ILS, ZAR, TRY, UAH, and more.

📈 COMING SOON

• Data export (CSV, JSON)
• Biometric authentication
• Cloud backup (optional)
• Budget tracking
• Multi-device sync

Made with ❤️ for privacy-conscious users who want complete control over their financial data.

Open source: https://github.com/aulianenko/MyFinances
```

*Max 4000 characters*

### Category
```
Finance
```

### Tags (Keywords)
```
finance, portfolio, tracker, multi-currency, crypto, stocks, investments, privacy, local, charts, analytics
```

### Content Rating
```
Everyone (No violence, no adult content)
```

### Privacy Policy URL
```
https://github.com/aulianenko/MyFinances/blob/main/PRIVACY_POLICY.md
```
*Required for all apps*

---

## Technical Details

### App Details

**Application ID:**
```
dev.aulianenko.myfinances
```

**Version Code:** `1`
**Version Name:** `1.0.0`

**Supported Devices:**
- Phone
- Tablet
- Android TV (not optimized)

**Minimum SDK:** 28 (Android 9.0 Pie)
**Target SDK:** 36 (Android 14.0)

### App Access

**Type:** Free
**Contains Ads:** No
**Contains In-App Purchases:** No

### Distribution

**Countries:** All (or select specific countries)
**Primary Language:** English
**Additional Languages:** (Add if translated)

---

## Data Safety Section

**Critical for Google Play approval**

### 1. Does your app collect or share user data?
```
No
```

### 2. Security practices
- [ ] Data is encrypted in transit: **Not applicable** (no network)
- [ ] Users can request data deletion: **Yes** (via app or uninstall)
- [ ] App uses HTTPS: **Not applicable**
- [ ] App handles financial info: **Yes, locally only**

### 3. Data types collected
```
None - all data is local only
```

### 4. Types of user data
- [ ] Location: **No**
- [ ] Personal info: **No**
- [ ] Financial info: **Yes, but local only**
- [ ] Messages: **No**
- [ ] Photos/Videos: **No**
- [ ] Audio files: **No**
- [ ] Files/Docs: **No**
- [ ] Calendar: **No**
- [ ] Contacts: **No**
- [ ] App activity: **No**
- [ ] Web browsing: **No**
- [ ] App info and performance: **No**
- [ ] Device/Other IDs: **No**

### 5. Financial Data Details
**What:** Account values, currency types
**Purpose:** Core app functionality
**Shared:** No
**Optional:** No (required for app function)
**Encrypted:** Local device encryption
**Deletable:** Yes

---

## Pre-Launch Checklist

### Testing
- [ ] Test on Android 9 (minimum SDK)
- [ ] Test on Android 14 (target SDK)
- [ ] Test on phone and tablet
- [ ] Test rotation handling
- [ ] Test with screen reader
- [ ] Test dark mode
- [ ] Test different languages/locales
- [ ] Test release APK (not debug)

### Build
- [ ] Release build assembles successfully
- [ ] ProGuard enabled and tested
- [ ] App signed with release key
- [ ] Version code incremented
- [ ] Version name updated

### Content
- [ ] All screenshots taken
- [ ] Feature graphic created
- [ ] Short description under 80 chars
- [ ] Full description compelling
- [ ] Privacy policy uploaded and linked
- [ ] Content rating completed

### Compliance
- [ ] Privacy policy accurate
- [ ] No misleading claims
- [ ] No prohibited content
- [ ] GDPR compliant (if EU distribution)
- [ ] No trademarked content

---

## Submission Steps

### 1. Create App on Play Console

1. Go to https://play.google.com/console
2. Click **Create app**
3. Fill in:
   - App name: "My Finances"
   - Default language: English
   - App or game: App
   - Free or paid: Free
4. Accept declarations
5. Click **Create app**

### 2. Complete Dashboard Tasks

**Set up your app:**
- [x] App access
- [x] Ads declaration
- [x] Content rating
- [x] Target audience
- [x] News app designation (No)
- [x] COVID-19 contact tracing (No)
- [x] Data safety
- [x] Government app (No)

**Store presence:**
- [x] App details
- [x] Main store listing
- [x] Store settings

**Production:**
- [x] Countries/regions
- [x] Create new release
- [x] Upload app bundle

### 3. Upload App Bundle

1. Go to **Production → Releases**
2. Click **Create new release**
3. Upload `app-release.aab`
4. Fill release details:
   - Release name: "1.0.0"
   - Release notes:

```
Initial release of My Finances

Features:
• Track multiple accounts with multi-currency support
• Portfolio charts and analytics
• Currency conversion tools
• Historical value tracking
• Complete local data privacy
• Material 3 design with dark mode
```

5. Click **Review release**

### 4. Review and Publish

1. Review all sections (green checkmarks)
2. Click **Submit for review**
3. Wait for Google review (1-7 days typically)

---

## After Publishing

### Monitor

- **Play Console:** Check crash reports, ratings, reviews
- **Version Code:** Increment for each update
- **Analytics:** Monitor installs, retention (optional)

### Updates

1. Increment `versionCode` and `versionName`
2. Build new signed bundle
3. Create new release in Play Console
4. Add release notes
5. Submit for review

### Respond to Reviews

- Reply to user feedback
- Address bugs and feature requests
- Maintain good rating

---

## Common Issues

### App Rejected

**Reason:** Privacy policy missing/incorrect
**Fix:** Ensure privacy policy URL is accessible and accurate

**Reason:** Misleading description
**Fix:** Remove any exaggerated claims

**Reason:** Permissions not explained
**Fix:** Add explanation for any requested permissions

### Build Issues

**Reason:** Minify crashes app
**Fix:** Check ProGuard rules, test release build thoroughly

**Reason:** Target SDK too old
**Fix:** Target latest SDK (currently 36)

---

## Resources

- **Play Console:** https://play.google.com/console
- **Policy Center:** https://play.google.com/about/developer-content-policy/
- **Developer Guides:** https://developer.android.com/distribute/best-practices/launch
- **Asset Guidelines:** https://support.google.com/googleplay/android-developer/answer/9866151

---

## Timeline

**Typical publishing timeline:**

1. **Day 0:** Create developer account ($25)
2. **Day 0-1:** Build assets (screenshots, graphics)
3. **Day 1:** Complete store listing
4. **Day 1:** Upload first release
5. **Day 1-7:** Google review process
6. **Day 7+:** App published!

**Update timeline:**
- Build and upload: 1 hour
- Review: 1-3 days
- Published: Updates go live after approval

---

## Success Metrics

**Track after launch:**
- Install count
- Crash-free rate (target: >99%)
- Average rating (target: >4.0)
- Retention rate
- Active devices

**Optimize:**
- Screenshots with high conversion
- Description with keywords
- Rating prompts in app (future)
- Feature graphic A/B testing

---

**Last Updated:** October 27, 2025
**App Version:** 1.0.0
