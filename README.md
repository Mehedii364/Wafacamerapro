# Wafa Camera Pro

> **Professional Camera + Smart Scanner + Photo Studio + Privacy Suite**  
> *Developed by Mehedi364* • *Package: `site.wafazone.camerapro`*

---

## 🌟 Overview

**Wafa Camera Pro** is an offline-first native Android application built with **Jetpack Compose**, **CameraX**, **Camera2 Interop**, and **Google ML Kit**. It combines photography, document scanning, studio color grading, image forensics, and privacy protection into a unified suite.

No cloud account or remote backend is required. All media, documents, and processing remain strictly on the device.

---

## 📸 Key Features

### 1. Camera Engine (CameraX + Camera2)
- **High-Definition Photography:** Lens switching (Rear/Front), auto/manual exposure compensation, pinch-to-zoom, grid overlays (Rule of Thirds, Golden Ratio, Cross), capture timer (3s, 5s, 10s), and real-time capture feedback.
- **Video Recording:** CameraX `VideoCapture` with start/pause/resume/stop controls, live duration timer, and audio recording toggle.
- **Torch & Flash:** Full support for Auto, On, Off, and continuous Torch flashlight modes.
- **Physical Volume Button Shutter:** Support for triggering photo capture via device volume keys.

### 2. Smart Document Scanner
- **Document Enhancement Filters:** Original, Grayscale, High-Contrast B&W, and Crisp Document enhancement.
- **On-Device OCR:** Real Google ML Kit Text Recognition with one-tap clipboard copy.
- **Multipage PDF Generation:** Converts scanned document pages directly to standard A4 PDF files using Android's native `PdfDocument` engine and saves to the device Documents directory.

### 3. Photo Studio (Editor)
- **Non-Destructive Adjustments:** Real-time ColorMatrix filters for Brightness, Contrast, Saturation, Warmth/Tint, Sepia, and Monochrome.
- **Transforms:** 90° rotation, Horizontal flip, and Vertical flip.
- **Direct Export:** Saves output as high-quality JPEG to the Android Gallery.

### 4. Watermark Studio
- **Developer Presets:** Includes "Developed by Mehedi364", "Wafa Camera Pro", and custom signatures.
- **Dynamic Stamps:** Configurable timestamp, text color, opacity, pill background, and multi-corner positioning (Bottom-Right, Bottom-Left, Top-Right, Center).

### 5. Collage & Contact Sheet Maker
- **Multiple Layouts:** 2x2 Grid, Split Horizontal, Split Vertical, Triple Hero, and Contact Sheet.
- **Styling:** Configurable border spacing and background colors (Obsidian Black, Studio White, Midnight Navy).

### 6. Format Converter & Compressor
- **Format Conversion:** Converts between JPEG, PNG, and WebP (lossy and lossless).
- **Compression Metrics:** Live before-and-after byte size calculations and percentage reduction metrics:  
  `((originalBytes - outputBytes) / originalBytes) * 100`

### 7. Duplicate Cleaner
- **Exact Duplicates:** Byte-level SHA-256 hash detection.
- **Visually Similar Photos:** Perceptual difference hashing (dHash) comparing 64-bit image fingerprints with Hamming distance metrics.
- **Safe Cleanup:** Visual comparison preview before deletion.

### 8. QR & Barcode Scanner
- On-device ML Kit barcode reading (QR Code, EAN-13, Code 128, UPC-A, Data Matrix).
- Local scan history backed by Room database.

### 9. Hardware Diagnostics
- Queries real `CameraManager` and `CameraCharacteristics` to evaluate Sensor Hardware Level (LEVEL_3, FULL, LIMITED, LEGACY), Max Megapixel resolution, Zoom ratios, Exposure steps, OIS / EIS stabilization, and RAW capture capabilities.

---

## 🛠️ Tech Stack & Architecture

- **Language:** Kotlin 2.x
- **UI Framework:** Jetpack Compose + Material 3 (Edge-to-Edge)
- **Camera:** CameraX (`camera-camera2`, `camera-lifecycle`, `camera-view`, `camera-video`)
- **Database:** AndroidX Room (`androidx.room`)
- **Preferences:** AndroidX DataStore
- **Machine Learning:** Google ML Kit (Text Recognition & Barcode Scanning)
- **Image Pipeline:** Android Canvas, ColorMatrix, Matrix, and Coil
- **Build System:** Gradle (Kotlin DSL)

---

## 🚀 Building Locally

### Prerequisites
- Android Studio Ladybug / Meerkat (or newer)
- JDK 17
- Android SDK 35/36 installed (minSdk: 26)

### Command Line Build
```bash
# Make gradlew executable
chmod +x ./gradlew

# Build debug APK
./gradlew assembleDebug

# Output APK path:
# app/build/outputs/apk/debug/app-debug.apk
```

---

## ⚙️ Automated GitHub Actions CI/CD

This repository includes an automated workflow (`.github/workflows/android-build.yml`) that:
1. Sets up JDK 17 and Android SDK environment.
2. Runs `./gradlew assembleDebug`.
3. Verifies APK integrity (`unzip -t` and size check).
4. Verifies SHA-256 checksums.
5. Saves the APK in `APK_DOWNLOAD/app-debug.apk` and uploads it as a workflow artifact.

---

## 🔒 Privacy Guarantee

- **Zero Cloud Uploads:** All camera preview feeds, photos, and documents remain strictly local to the device.
- **Zero Telemetry:** No tracking libraries, analytics SDKs, or cloud backends.
- **Minimal Permissions:** Uses Android's modern photo picker for importing and scoped storage for saving.

---

## 👤 Credits

- **Developer:** Mehedi364
- **Application:** Wafa Camera Pro
- **Package:** `site.wafazone.camerapro`
