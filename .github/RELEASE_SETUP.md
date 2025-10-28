# GitHub Actions Release Setup

This document explains how to set up GitHub Actions secrets for automated release builds.

## Required Secrets

To enable automatic signing of release builds, you need to configure the following GitHub secrets:

### 1. KEYSTORE_BASE64

Your Android keystore file encoded in Base64.

**How to create:**
```bash
# Navigate to your project directory
cd /path/to/MyFinances

# Encode your keystore file
base64 -i path/to/your/keystore.jks | pbcopy  # macOS (copies to clipboard)
# OR
base64 -i path/to/your/keystore.jks > keystore.txt  # Save to file
```

### 2. KEYSTORE_PASSWORD

The password for your keystore file.

### 3. KEY_ALIAS

The alias of the key in your keystore.

### 4. KEY_PASSWORD

The password for the specific key.

## Adding Secrets to GitHub

1. Go to your GitHub repository
2. Navigate to **Settings** → **Secrets and variables** → **Actions**
3. Click **New repository secret**
4. Add each of the four secrets listed above

## Workflows

### CI Workflow (ci.yml)
- **Triggers:** Push to main, Pull requests
- **Actions:**
  - Runs all unit tests
  - Builds debug APK
  - Uploads test results and APK as artifacts

### Release Workflow (release.yml)
- **Triggers:**
  - When a GitHub release is created
  - Manual workflow dispatch (with version input)
- **Actions:**
  - Runs release unit tests
  - Builds signed release APK (if secrets configured)
  - Builds signed release AAB (if secrets configured)
  - Uploads APK and AAB as artifacts (90 days retention)
  - Attaches APK and AAB to GitHub release

## Creating a Release

### Option 1: GitHub UI
1. Go to **Releases** → **Draft a new release**
2. Create a new tag (e.g., `v1.0.0`)
3. Fill in release title and description
4. Click **Publish release**
5. GitHub Actions will automatically build and attach APK/AAB

### Option 2: Manual Workflow Dispatch
1. Go to **Actions** → **Release Build**
2. Click **Run workflow**
3. Enter version name (e.g., `1.0.0`)
4. Click **Run workflow**
5. Download artifacts from the workflow run

## Without Signing Secrets

If you haven't configured signing secrets:
- The workflow will still run
- APK/AAB will be built but **unsigned**
- You'll need to sign them manually before distribution

## Verifying Setup

After configuring secrets, test the setup:

1. Create a test release:
   ```bash
   git tag v0.0.1-test
   git push origin v0.0.1-test
   ```

2. Create a GitHub release from this tag

3. Check Actions tab for the workflow run

4. Verify signed APK/AAB are attached to the release

## Security Notes

- **Never commit keystore files or passwords to the repository**
- GitHub secrets are encrypted and only accessible to GitHub Actions
- The keystore is temporarily decoded during build and cleaned up after
- Use different keystores for debug and release builds

## Troubleshooting

### Build fails with "Keystore not found"
- Verify `KEYSTORE_BASE64` secret is correctly set
- Check that Base64 encoding was done correctly

### Build fails with "Invalid keystore format"
- Ensure you encoded the correct keystore file
- Try re-encoding the keystore

### APK/AAB not signed
- Verify all four secrets are set correctly
- Check workflow logs for signing errors
- Ensure keystore password and key password are correct

## Updating Keystore

If you need to update the keystore:

1. Generate new keystore (see `KEYSTORE_SETUP.md`)
2. Re-encode it to Base64
3. Update `KEYSTORE_BASE64` secret
4. Update passwords/alias if changed

## References

- [Android App Signing](https://developer.android.com/studio/publish/app-signing)
- [GitHub Actions Secrets](https://docs.github.com/en/actions/security-guides/encrypted-secrets)
- [KEYSTORE_SETUP.md](../KEYSTORE_SETUP.md) - How to generate a keystore
