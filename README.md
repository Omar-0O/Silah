# 🌸 Silah (صِلَةِ) — Kinship & Family Ties Android Application

> **«مَنْ سَرَّهُ أَنْ يُبْسَطَ لَهُ فِي رِزْقِهِ، وَأَنْ يُنْسَأَ لَهُ فِي أَثَرِهِ، فَلْيَصِلْ رَحِمَهُ»**  
> *"Whoever would like his provision to be expanded and his lifespan extended, let him maintain his family ties."* — Prophet Muhammad ﷺ

**Silah (صِلَةِ)** is a modern, privacy-focused, **Local-First Native Android Application** built to help users preserve, organize, and nurture family relationships and kinship ties. Featuring automatic call log synchronization, intelligent degree-based reminders, dynamic home screen widgets, and offline-first JSON backup/restore capabilities, Silah seamlessly integrates into daily life to keep family connections strong.

---

## ✨ Key Features

### 1. 📞 Automatic Call Log Synchronization
- **Automatic Tracking**: Automatically detects incoming, outgoing, and missed calls from saved relatives via `CallLogManager`.
- **Zero Effort**: Updates communication records instantly in the background without needing manual logging.

### 2. 📲 Smart Contact Import & Reminder Customization
- **Interactive Importer**: Search phone contacts and import relatives with a single tap.
- **Custom Reminder Frequencies**: Set tailored reminder intervals for each relative (Daily, Every 3 Days, Weekly, Bi-weekly, Monthly).
- **Auto-Suggested Relationship Degrees**: Smart relationship degree classification (*Parents*, *Siblings*, *Uncles & Aunts*, *Other Relatives*).

### 3. ⏱️ Arabic Relative Time & Countdown Timers
- **Humanized Arabic Relative Time**: Precise Arabic time formatting (`"منذ 15 دقيقة ⏱️"`, `"منذ ساعتين ⏱️"`, `"منذ 3 ساعات ⏱️"`, `"أمس"`, `"منذ 4 أيام"`).
- **Color-Coded Status Tracking**:
  - 🟢 **Connected**: All good (`"بخير"`)
  - 🟡 **Due Tomorrow**: Final reminder day (`"غداً اخر موعد"`)
  - 🟠 **Due Today**: Contact due today (`"حان موعد الاتصال اليوم"`)
  - 🔴 **Overdue**: Overdue count by days (`"تأخرت ع أيام"`)

### 4. 🌿 Live Home Screen Smart Widget
- **Urgency Scoring Algorithm**: Automatically ranks relatives on your home screen widget based on urgency and contact intervals.
- **Auto-Refresh**: Instantly updates when calls are synced, logs are recorded, or relatives are added/edited/deleted.
- **One-Tap Quick Actions**: Call or message directly from your device home screen.

### 5. 🔔 Degree-Aware Smart Notifications
- **Context-Aware Arabic Copy**:
  - Uncles/Aunts: *"بقالك فترة مش بتطمن على خالك ✨"* / *"بقالك فترة مش بتطمن على عمتك ✨"*
  - Parents: *"بقالك فترة مش بتطمن على والدتك 💚"* / *"بقالك فترة مش بتطمن على والدك 💚"*
  - Siblings: *"بقالك فترة مش بتطمن على أختك 🌸"* / *"بقالك فترة مش بتطمن على أخوك 🌸"*
  - Other Relatives: *"بقالك فترة مش بتطمن على عمر 🌿"*
- **WorkManager Powered**: Reliable, battery-efficient periodic background execution.

### 6. 🔒 100% Local-First Architecture & SAF Backup
- **Complete Privacy**: Zero external server dependencies; all data resides locally in Room Database.
- **JSON Backup & Restore**: Storage Access Framework (SAF) integration to export/import `silah_backup.json` to Google Drive or local storage effortlessly.

### 7. 🎨 Soft-UI Design System & Dynamic Arabic Typography
- **Custom Canvas Graphics**: Geometrically crafted Kinship Knot (`KinshipKnotIcon.kt`).
- **Dynamic Fonts Engine**: Real-time font switching between premium Arabic typefaces:
  - 🌟 **Thamanyah OS** (Cultural & Modern)
  - 🖋️ **Cairo** (Classic & Balanced)
  - 🍃 **Almarai** (Soft & Elegant)
  - 📐 **Tajawal** (Geometric & Readable)

---

## 🛠️ Technology Stack & Architecture

- **Language**: 100% Kotlin (Coroutines, StateFlow, SharedFlow)
- **UI Framework**: Jetpack Compose (Material Design 3, Soft-UI Palette)
- **Database**: Room Database (Local-First SQLite ORM)
- **Background Processing**: Android WorkManager
- **Widget**: AppWidgetProvider & RemoteViews
- **Storage**: Storage Access Framework (SAF)
- **Architecture Pattern**: MVVM (Model-View-ViewModel) + Repository Pattern

---

## 📦 Project Directory Structure

```text
com.example/
├── MainActivity.kt               # Entry point & SAF launchers for Backup/Restore
├── data/
│   ├── AppDatabase.kt           # Room Database setup
│   ├── BackupManager.kt          # JSON Export & Import logic
│   ├── CallLogManager.kt         # Call Log matching & classification
│   ├── Daos.kt                   # Room Data Access Objects
│   ├── Entities.kt               # Database Entities (Relative, CommunicationLog, etc.)
│   └── RelativeRepository.kt    # Repository abstraction layer
├── ui/
│   ├── components/
│   │   ├── CallLogBadge.kt       # Visual call type indicator
│   │   ├── CommitmentHeaderCard.kt# Progress arc header card
│   │   ├── DueRelativesCarousel.kt# Horizontal "Due for contact" carousel
│   │   ├── KinshipKnotIcon.kt    # Custom Canvas Kinship Knot artwork
│   │   ├── RelativeCard.kt       # Relative card with countdown & CRUD actions
│   │   └── SilaEmptyStateView.kt # Animated empty state illustration & action triggers
│   ├── dialogs/
│   │   ├── AddEditRelativeDialog.kt # Relative creation & edition dialog
│   │   ├── ImportContactsDialog.kt # Searchable device contact importer with interval picker
│   │   ├── RecordLogBottomSheet.kt # Quick manual log entry bottom sheet
│   │   └── SettingsDialog.kt     # App settings & Backup/Restore controls
│   ├── screens/
│   │   ├── ChallengesTabScreen.kt# Kinship challenges & badges
│   │   ├── MainDashboard.kt      # Main Navigation Host & history logs
│   │   ├── OnboardingScreen.kt   # 3-step animated intro screen
│   │   ├── RelativesTabScreen.kt # Relatives list, category filters & search
│   │   ├── SplashScreen.kt       # Spiritual splash screen with Hadith
│   │   └── TemplatesTabScreen.kt # Quick messaging templates
│   └── theme/
│       ├── Color.kt              # Soft-UI palette (PrimaryGreen, SoftGold, etc.)
│       └── Theme.kt              # Material 3 Theme Configuration
├── utils/
│   └── DateUtils.kt              # Exact Arabic relative time calculation
├── viewmodel/
│   └── RelativeViewModel.kt      # Core State Flow & Business Logic
├── widget/
│   └── SilaAppWidgetProvider.kt  # Home Screen Widget & Urgency Scoring
└── work/
    └── ReminderWorker.kt         # Smart degree-aware notification builder
```

---

## 🚀 Building & Running the Project

### Prerequisites
- **Android Studio**: Ladybug (2024.2.1) or newer
- **JDK**: Java 17 or Java 21
- **Android SDK**: API 24 (Android 7.0) minimum, Target API 34 (Android 14)

### Option 1: Via Android Studio (Recommended)
1. Clone or download this repository.
2. Open **Android Studio** and select **Open** -> Choose the `/Silah` folder.
3. Wait for Gradle Sync to complete.
4. Click the green **Run** button to launch on an emulator or physical device.
5. To generate the APK: Go to **Build -> Build Bundle(s) / APK(s) -> Build APK(s)**.

### Option 2: Via Command Line (Gradle Wrapper)
```bash
# Build Debug APK
./gradlew assembleDebug

# Output APK path:
app/build/outputs/apk/debug/app-debug.apk
```

---

## 🛡️ Android Permissions

- `READ_CONTACTS`: Used to import family members directly from your phone address book.
- `READ_CALL_LOG`: Used for automatic background call matching with saved relatives.
- `POST_NOTIFICATIONS` (Android 13+): Used to deliver timely kinship reminder notifications.

---

## 📄 License & Privacy

**Silah** is open-source and built with a **100% Privacy-First Principle**. No personal data, contacts, or call logs ever leave your device.
