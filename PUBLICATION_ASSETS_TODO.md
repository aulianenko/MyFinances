# Publication Assets TODO

This file tracks the remaining assets needed for Google Play Store publication.

## ✅ Completed

- [x] Release build configuration (ProGuard/R8 enabled)
- [x] Signing configuration setup
- [x] Privacy Policy document
- [x] Version configuration (1.0.0)
- [x] Clean manifest (no extra permissions)
- [x] Comprehensive ProGuard rules
- [x] Build successful (APK: 15MB, AAB: 10MB)

## 📋 Remaining Tasks

### 1. App Icon ⚠️

**Current Status:** Default Android Studio icon
**Action Required:** Create custom app icon

**Requirements:**
- Base icon: 512x512 px (high-res for Play Store)
- Adaptive icon layers:
  - Foreground: 108x108 dp (safe zone: 66x66 dp)
  - Background: 108x108 dp
- Formats: PNG (24-bit, no transparency for some)

**Tools:**
- Android Studio → New → Image Asset
- Figma / Adobe XD / Sketch
- Icon generators (e.g., appicon.co)

**Design Suggestions for My Finances:**
- Use chart/graph iconography
- Include currency symbol ($, €) subtly
- Use Material 3 color scheme (primary color)
- Keep it simple and recognizable at small sizes

**Files to Create:**
```
app/src/main/res/
├── mipmap-hdpi/ic_launcher.png (72x72)
├── mipmap-mdpi/ic_launcher.png (48x48)
├── mipmap-xhdpi/ic_launcher.png (96x96)
├── mipmap-xxhdpi/ic_launcher.png (144x144)
├── mipmap-xxxhdpi/ic_launcher.png (192x192)
└── mipmap-anydpi-v26/ic_launcher.xml (adaptive icon config)
```

**High-res for Play Store:**
- File: `512x512_icon.png`
- Location: Keep in `assets/` folder for upload

---

### 2. Feature Graphic 🎨

**Status:** Not created
**Required:** Yes (mandatory for Play Store)

**Requirements:**
- Dimensions: 1024 x 500 px
- Format: JPEG or 24-bit PNG (no transparency)
- File size: < 1MB
- Content: App name + key features visualization

**Design Elements:**
- App name: "My Finances"
- Tagline: "Track Your Portfolio" or "Multi-Currency Portfolio Tracker"
- Visual: Chart mockup, currency symbols, phone screenshot
- Colors: Match Material 3 theme
- Font: Roboto (Material Design standard)

**Tools:**
- Canva (templates available)
- Figma
- Adobe Photoshop
- GIMP (free)

**Template Ideas:**
```
┌─────────────────────────────────────────┐
│  [App Icon]  MY FINANCES                │
│                                         │
│  Track Your Portfolio with Confidence   │
│  [Phone screenshot showing charts]      │
│  📊 Charts • 💱 Multi-Currency • 📈 Analytics │
└─────────────────────────────────────────┘
```

**File to Create:**
- `feature_graphic.png` (1024x500)
- Location: Keep in `assets/` folder

---

### 3. Screenshots 📱

**Status:** Not created
**Required:** Minimum 2, recommended 4-8

**Requirements:**
- Phone screenshots:
  - Format: JPEG or 24-bit PNG
  - Min: 320px | Max: 3840px
  - Max aspect ratio: 2:1
  - Recommended: 1080 x 2400 px (portrait)
  - Minimum required: 2 screenshots
  - Recommended: 4-8 screenshots

**Screenshots to Take:**

1. **Dashboard Screen** ⭐ (Must have)
   - Show portfolio overview
   - Display charts
   - Show statistics

2. **Account List** ⭐ (Must have)
   - Multiple accounts visible
   - Different currencies shown
   - Current values displayed

3. **Account Detail with Chart** (Recommended)
   - Line chart visible
   - Value history shown
   - Analytics data

4. **Add Value Screen** (Recommended)
   - Show date picker
   - Input fields visible
   - Clean UI

5. **Analytics Screen** (Recommended)
   - Performance metrics
   - Best/worst performers
   - Statistics

6. **Settings with Currency Converter** (Optional)
   - Base currency selection
   - Converter tool
   - Settings options

7. **Bulk Update Screen** (Optional)
   - Multiple account inputs
   - Demonstrates efficiency

8. **Dark Mode Example** (Optional)
   - Show dark theme
   - Any screen in dark mode

**How to Take Screenshots:**

**Method 1: Android Studio Emulator**
```bash
1. Start emulator with desired screen size
2. Navigate to each screen
3. Click camera icon in emulator toolbar
4. Saves to: /Users/andrii/.android/avd/[device]/screenshots/
```

**Method 2: Physical Device**
```bash
1. Enable Developer Options
2. Connect via ADB
3. Navigate to screen
4. Run: adb exec-out screencap -p > screenshot.png
```

**Method 3: Device Screenshot + ADB Pull**
```bash
1. Take screenshot on device (Power + Volume Down)
2. adb pull /sdcard/Pictures/Screenshots/ ./screenshots/
```

**Post-Processing:**
- Resize to 1080 x 2400 px
- Add optional frame (phone bezel)
- Ensure no personal data visible
- Compress for faster upload

**Files to Create:**
```
assets/screenshots/
├── 01_dashboard.png
├── 02_accounts.png
├── 03_account_detail.png
├── 04_add_value.png
├── 05_analytics.png
├── 06_settings.png
├── 07_bulk_update.png
└── 08_dark_mode.png
```

---

### 4. Promotional Video (Optional) 🎥

**Status:** Not created
**Required:** No (but recommended)

**Requirements:**
- YouTube link
- Duration: 30-90 seconds
- Shows key features
- High quality (720p minimum)

**Content Ideas:**
- Quick walkthrough of main features
- Creating account
- Adding values
- Viewing charts
- Using currency converter

**Tools:**
- Screen recording (ADB or OBS)
- Video editing (iMovie, DaVinci Resolve, Kdenlive)
- Background music (YouTube Audio Library)

**Not required for initial launch** - can add later

---

## Next Steps

### Before Submission:

1. **Create App Icon** (30 min - 2 hours)
   - Design in Figma/Canva
   - Generate adaptive icon
   - Test on device

2. **Create Feature Graphic** (1-2 hours)
   - Design in Canva using template
   - Include app name and tagline
   - Add visual elements

3. **Take Screenshots** (30 min - 1 hour)
   - Run app on emulator (Pixel 6 or similar)
   - Navigate to each key screen
   - Take clean screenshots
   - Optional: Add phone frames

4. **Review All Assets**
   - [ ] Icon looks good at all sizes
   - [ ] Feature graphic is compelling
   - [ ] Screenshots show key features
   - [ ] All images meet size requirements

5. **Follow GOOGLE_PLAY_SUBMISSION.md**
   - Upload all assets
   - Complete store listing
   - Submit for review

---

## Quick Asset Checklist

Before uploading to Play Console:

### Required ✅
- [ ] App icon (512x512 high-res)
- [ ] Feature graphic (1024x500)
- [ ] At least 2 phone screenshots
- [ ] App title (≤50 chars)
- [ ] Short description (≤80 chars)
- [ ] Full description (≤4000 chars)
- [ ] Privacy policy URL
- [ ] Signed APK/AAB
- [ ] Content rating completed
- [ ] Data safety form completed

### Recommended ⭐
- [ ] 4-8 phone screenshots
- [ ] Tablet screenshots
- [ ] Feature graphic with app branding
- [ ] Custom adaptive icon
- [ ] Well-written descriptions

### Optional 🎁
- [ ] Promotional video
- [ ] Translated listings
- [ ] Tablet-optimized screenshots

---

## Asset Templates & Resources

**Icon Generators:**
- https://icon.kitchen/ (free adaptive icons)
- https://appicon.co/ (iOS + Android)
- https://romannurik.github.io/AndroidAssetStudio/

**Design Tools:**
- Canva (free tier): https://www.canva.com/
- Figma (free): https://www.figma.com/
- GIMP (free): https://www.gimp.org/

**Feature Graphic Templates:**
- Canva → "App Feature Graphic" templates
- Figma Community → Android feature graphic templates

**Screenshot Framing:**
- https://mockuphone.com/ (device frames)
- https://screenshots.pro/ (app store screenshots)

**Stock Images/Icons:**
- Material Icons: https://fonts.google.com/icons
- Unsplash: https://unsplash.com/ (if needed)

---

## Estimated Time to Complete

- **App Icon:** 1-3 hours (including iterations)
- **Feature Graphic:** 1-2 hours
- **Screenshots:** 1 hour
- **Total:** 3-6 hours

**Can start publishing after completing these assets!**

---

## Need Help?

**For icon design:**
- Use Android Studio's Image Asset Studio
- Browse Material Design guidelines
- Look at similar finance apps for inspiration

**For screenshots:**
- Use Pixel 6 emulator (common resolution)
- Ensure clean data (no "Test Account")
- Show realistic but not actual financial data

**For feature graphic:**
- Keep text large and readable
- Use high contrast
- Include app name prominently
- Less is more - don't overcrowd

---

**Last Updated:** October 27, 2025
**Status:** Ready for asset creation
