# Keystore Setup Guide

## Generate Release Keystore

### Step 1: Generate Keystore File

Run this command in your project root directory:

```bash
keytool -genkey -v -keystore myfinances-release-key.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias myfinances-key
```

**You will be prompted for:**
- Keystore password (choose a strong password)
- Key password (can be same as keystore password)
- Your name
- Organizational unit
- Organization name
- City/Locality
- State/Province
- Country code (e.g., US, UK, UA)

**IMPORTANT:** Save these passwords securely! You cannot recover them.

### Step 2: Create keystore.properties File

Create a file named `keystore.properties` in the project root:

```properties
storeFile=myfinances-release-key.jks
storePassword=YOUR_KEYSTORE_PASSWORD
keyAlias=myfinances-key
keyPassword=YOUR_KEY_PASSWORD
```

**Example:**
```properties
storeFile=myfinances-release-key.jks
storePassword=MySecurePassword123
keyAlias=myfinances-key
keyPassword=MySecurePassword123
```

### Step 3: Secure Your Keystore

**Add to .gitignore:**
```bash
# Add these lines to .gitignore
*.jks
*.keystore
keystore.properties
```

**NEVER commit:**
- ❌ `myfinances-release-key.jks`
- ❌ `keystore.properties`
- ❌ Any file containing passwords

**Backup securely:**
- ✅ Store keystore file in secure cloud storage (encrypted)
- ✅ Store passwords in password manager
- ✅ Keep offline backup on encrypted drive

### Step 4: Verify Setup

Build a signed release APK:

```bash
./gradlew assembleRelease
```

The signed APK will be at:
```
app/build/outputs/apk/release/app-release.apk
```

Verify the signature:
```bash
jarsigner -verify -verbose -certs app/build/outputs/apk/release/app-release.apk
```

---

## Alternative: Using Android Studio

### Method 1: Build → Generate Signed Bundle/APK

1. **Build → Generate Signed Bundle / APK**
2. Choose **Android App Bundle** (recommended) or **APK**
3. Click **Create new...**
4. Fill in the form:
   - Key store path: `myfinances-release-key.jks`
   - Password: your keystore password
   - Key alias: `myfinances-key`
   - Key password: your key password
5. Click **OK** and **Next**
6. Choose **release** build variant
7. Select signature versions (V1 and V2)
8. Click **Finish**

### Method 2: Build Variants

1. Open **Build Variants** panel (View → Tool Windows → Build Variants)
2. Select **release** variant
3. Click **Build → Build Bundle(s) / APK(s) → Build APK(s)**

---

## Google Play App Signing

**Recommended:** Use Google Play App Signing

**Benefits:**
- Google manages your app signing key
- You only manage the upload key
- Automatic security improvements
- Key rotation support

**Setup:**
1. Enroll in Google Play Console
2. Upload your initial release
3. Opt into Google Play App Signing
4. Google will generate and manage the app signing key
5. You keep your upload key for future releases

**Documentation:**
https://support.google.com/googleplay/android-developer/answer/9842756

---

## Keystore Security Checklist

- [ ] Keystore file backed up securely
- [ ] Passwords stored in password manager
- [ ] Keystore file NOT in version control
- [ ] keystore.properties NOT in version control
- [ ] .gitignore updated
- [ ] Release APK builds successfully
- [ ] Considered Google Play App Signing

---

## Troubleshooting

### "keystore.properties not found"

**Solution:** Create the file in project root with correct properties.

### "Keystore was tampered with, or password was incorrect"

**Solution:** Check your password. Make sure it's the keystore password, not the key password.

### "Cannot recover key"

**Solution:** If you lost your keystore or password, you must:
- Generate a new keystore
- Create a new app listing on Google Play
- Cannot update existing app

---

## Important Notes

⚠️ **Lose your keystore = Cannot update your app on Google Play**

- Keep multiple secure backups
- Use a password manager
- Consider Google Play App Signing
- Document your setup process

✅ **Best Practice:**
Use Google Play App Signing and only manage your upload key. This provides the best security and recovery options.
