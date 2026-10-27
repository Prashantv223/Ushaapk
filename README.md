# CLR - Smart File Manager & AI File Converter

[![CLR-apk](https://github.com/Prashantv223/Clr/actions/workflows/CLR-apk.yml/badge.svg)](https://github.com/Prashantv223/Clr/actions/workflows/CLR-apk.yml)
[![Build Android APK](https://github.com/Prashantv223/Clr/actions/workflows/build-apk.yml/badge.svg)](https://github.com/Prashantv223/Clr/actions/workflows/build-apk.yml)

**CLR** is an advanced Android application built with Jetpack Compose and Material 3, combining comprehensive file management, offline & cloud capabilities, and AI-powered conversions.

---

## 🌟 Key Features

1. **AI File Converter**:
   - Converts **Images** (JPG, PNG, WebP) to PDF, PNG, JPG, and compressed WebP formats.
   - Converts **Documents & Text** into A4 PDFs and Markdown.
   - Extracts **Audio from Video** into 320kbps MP3 and M4A voice tracks.
   - **AI Superpowers**: Optical Character Recognition (OCR), Executive Summaries, and Audio/Video Transcripts powered by Gemini AI.

2. **File Manager**:
   - Full operations: Move, Copy, Delete, Rename, Compress to ZIP, and Extract ZIP.
   - In-app media player (Audio with waveforms, Video player, Fullscreen Image viewer, APK Inspector, and Document text editor).
   - Instant file search with category filters (Images, Videos, Audio, Documents, APKs, Archives, Downloads).

3. **Private Encrypted Vault**:
   - Protected behind PIN and Biometric (Fingerprint) authentication.
   - Secure storage for hidden photos, videos, confidential documents, and encrypted secret notes.
   - Decoy mode: Optional "Fake Crash" security trigger after 3 failed PIN attempts.

4. **GPS Tracker & Route History**:
   - Live GPS telemetry (latitude, longitude, altitude, speed).
   - Interactive vector canvas map with radar pulse and route line rendering.
   - Record and replay route histories with distance (km) and elevation tracking.

5. **Hindi Calendar & Regional Festival Planner**:
   - Complete Vikram Samvat 2083 Panchang (Tithi, Paksha, Nakshatra, Rahu Kaal, Abhijit Muhurat).
   - 12 Hindi Maas selector with seasonal information.
   - Regional festival tracking and custom event planner with reminder notifications.

6. **Cloud Sync & Social Feeds**:
   - Encrypted cloud backup status (15 GB quota) with animated sync progress.
   - Auto-sync for social media feeds (WhatsApp, Telegram, Instagram, Cloud Drive).

7. **Voice Assistant & Gemini Text-to-Speech (TTS)**:
   - Hands-free voice navigation.
   - Speaks aloud file details, Panchang, and notes using `gemini-3.8-flash-tts` with native system TTS fallback.

8. **Custom Themes & Push Notification Center**:
   - Themes: Slate Cyan, Emerald Vault, Midnight Blue, Sunset Amber, and AMOLED Dark.
   - Customizable notification priority (High, Default, Low) and channel toggles.

---

## 🚀 GitHub Actions CI/CD Workflow (`Prashantv223/Clr`)

The repository includes a ready-to-run GitHub Actions workflow located at `.github/workflows/build-apk.yml`.

### Triggers:
- **Push to `main` or `master`**: Automatically compiles the code, executes tests, builds `app-debug.apk`, and uploads it as an artifact.
- **Pull Requests**: Validates unit and Robolectric tests and compiles the APK.
- **Manual Trigger (`workflow_dispatch`)**: Go to the **Actions** tab in GitHub, select **Build Android APK**, choose the build type (`debug`, `release`, or `both`), and click **Run workflow**.
- **Tags (`v*`)**: Pushing a version tag (e.g., `git tag v1.0.0 && git push origin v1.0.0`) builds both the APK and Android App Bundle (`.aab`) and automatically attaches them to a new GitHub Release.

### Local Build Instructions:
```bash
# Clone the repository
git clone https://github.com/Prashantv223/Clr.git
cd Clr

# Build Debug APK
./gradlew assembleDebug

# Output APK path:
# app/build/outputs/apk/debug/app-debug.apk
```
