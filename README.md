# 📝 Float Notes — Android Floating Window App

A native Android app that displays sticky notes as **true system overlay windows** that float above all other apps.

---

## ✨ Features

| Feature | Details |
|---|---|
| 🪟 True floating windows | Uses `TYPE_APPLICATION_OVERLAY` — floats above all apps |
| ➕ Draggable bubble launcher | Purple gradient bubble, drag anywhere on screen |
| 🖱 Drag notes freely | Drag by the header bar to any position |
| 👁 Transparency control | Per-note opacity slider (20%–100%) via ⚙ menu |
| 🔤 Text size control | Global text size slider (10sp–30sp) via ⚙ menu |
| ↔ Note width control | Resize note width (150dp–360dp) via ⚙ menu |
| 🎨 6 color themes | Each new note gets a unique color accent |
| 🔔 Foreground service | Keeps notes alive while you use other apps |
| × Close individual notes | Dismiss any note independently |
| 🔁 Persistent service | Notes survive app switching |

---

## 📋 Requirements

- **Android Studio** Hedgehog (2023.1.1) or newer
- **Android SDK** API 26+ (Android 8.0 Oreo minimum)
- **Target device/emulator**: Android 8.0+

---

## 🚀 Build & Install

### Option A — Android Studio (Recommended)

1. **Open project**: File → Open → select the `FloatNotes` folder
2. **Sync Gradle**: Click "Sync Now" when prompted
3. **Connect device**: Enable USB debugging on your Android phone
4. **Run**: Click the ▶ Run button or press `Shift+F10`

### Option B — Command Line

```bash
cd FloatNotes
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

## 📱 First Launch

1. Open **Float Notes** from your app drawer
2. Tap **"Launch Floating Notes"**
3. The app will open Android's **"Display over other apps"** settings
4. Find **Float Notes** and toggle it **ON**
5. Press back — the purple bubble appears on screen!

### Using the app

- **Tap the bubble** (+) → creates a new floating note
- **Drag bubble** → move the launcher anywhere
- **Drag note header** → reposition the note
- **Type in note body** → tap the note to open keyboard
- **Tap ⚙** → open settings panel for that note:
  - **Transparency** slider — applies to ALL notes
  - **Text Size** slider — applies to ALL notes  
  - **Note Width** slider — applies to THAT note only
- **Tap ×** → close/delete that note
- **Notification** → tap "Stop" to shut down the service

---

## 🏗 Project Structure

```
FloatNotes/
├── app/src/main/
│   ├── AndroidManifest.xml          # Permissions & components
│   └── java/com/floatnotes/
│       ├── MainActivity.java         # Permission request + launcher UI
│       ├── FloatingNoteService.java  # Core overlay service ⭐
│       └── NoteEditorActivity.java   # Reserved for future use
│   └── res/
│       ├── layout/activity_main.xml  # Main screen layout
│       ├── drawable/                 # Button & card backgrounds
│       └── values/                   # Strings, colors, styles
├── build.gradle                      # Project config
└── settings.gradle
```

---

## 🔑 Key Android APIs Used

```java
// Overlay window type (draws above all apps)
WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY

// Required permission
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />

// Runtime permission check
Settings.canDrawOverlays(context)

// Foreground service (keeps running when app is in background)
startForegroundService(intent)

// Window transparency
params.alpha = 0.85f; // 0.0 (invisible) to 1.0 (opaque)
windowManager.updateViewLayout(view, params);
```

---

## 🛠 Customization

### Add more note colors
In `FloatingNoteService.java`, edit the `noteColors` and `noteAccents` arrays.

### Change default opacity/text size
```java
private float globalAlpha = 0.92f;    // Change default transparency
private float globalTextSize = 15f;   // Change default text size (sp)
```

### Make notes persist across reboots
Add a `BroadcastReceiver` for `BOOT_COMPLETED` and save note content to SharedPreferences.

---

## ⚠️ Known Limitations

- Notes are **not saved** when the service is stopped (add SharedPreferences persistence to fix)
- Keyboard may overlap notes on some devices (works best with notes in upper half of screen)
- Some manufacturer overlays (MIUI, OneUI) may require additional battery optimization exemptions

---

## 📄 License

MIT — free to use and modify.
