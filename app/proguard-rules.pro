# ──────────────────────────────────────────────────────────────────────────────
# Sila (صِلَةِ) — ProGuard / R8 Rules for Production & Release Hardening
# ──────────────────────────────────────────────────────────────────────────────

# ── General Optimizations & Crash Logging ────────────────────────────────────
-keepattributes *Annotation*,SourceFile,LineNumberTable,InnerClasses,EnclosingMethod,Signature
-renamesourcefileattribute SourceFile

# Strip developer verbose & debug logs in release APK
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
}

# ── Kotlin & Coroutines ───────────────────────────────────────────────────────
-dontwarn kotlin.**
-keep class kotlin.Metadata { *; }
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.** { volatile <fields>; }

# ── Dagger / Hilt ────────────────────────────────────────────────────────────
-dontwarn dagger.hilt.**
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.internal.GeneratedComponent { *; }
-keep @dagger.hilt.android.HiltAndroidApp class * { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }
-keep class * extends androidx.hilt.work.WorkerAssistedFactory { *; }
-keep interface * extends androidx.hilt.work.WorkerAssistedFactory { *; }
-keepclasseswithmembers class * {
    @javax.inject.Inject <init>(...);
}
-keepclasseswithmembers class * {
    @dagger.assisted.AssistedInject <init>(...);
}

# ── Room Database (Keep Entities, DAOs, Migrations, Database) ────────────────
-keep class com.example.data.** { *; }
-keep class * extends androidx.room.RoomDatabase
-keepclassmembers class * extends androidx.room.RoomDatabase {
    abstract <methods>;
}
-keep @androidx.room.Entity class * { *; }
-keepclassmembers @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }
-keepclassmembers @androidx.room.Dao class * {
    public <methods>;
}
-keep @androidx.room.TypeConverter class * { *; }
-keepclassmembers @androidx.room.TypeConverter class * { *; }
-keep class * extends androidx.room.migration.Migration { *; }
-keep class androidx.room.RoomDatabase$Callback { *; }
-dontwarn androidx.room.paging.**

# ── WorkManager & @HiltWorker ─────────────────────────────────────────────────
-dontwarn androidx.work.**
-keep class * extends androidx.work.Worker { <init>(android.content.Context, androidx.work.WorkerParameters); }
-keep class * extends androidx.work.CoroutineWorker { <init>(android.content.Context, androidx.work.WorkerParameters); }
-keep class * extends androidx.work.ListenableWorker { <init>(android.content.Context, androidx.work.WorkerParameters); }
-keep @androidx.hilt.work.HiltWorker class * { *; }
-keep class com.example.work.** { *; }

# ── Domain Models & Data Serialization (Backup & Restore) ─────────────────────
-keep class org.json.** { *; }
-keep class com.example.data.BackupManager** { *; }
-keepclassmembers class com.example.data.BackupManager** { *; }
-keep class com.example.data.model.** { *; }
-keepclassmembers class com.example.data.model.** { *; }
-keep class com.example.core.datastore.** { *; }
-keepclassmembers class com.example.core.datastore.** { *; }
-keep class com.example.domain.usecase.BackupRestoreUseCase** { *; }
-keep class com.example.domain.usecase.BackupResult { *; }
-keepclassmembers class com.example.data.Relative { *; }
-keepclassmembers class com.example.data.CommunicationLog { *; }
-keepclassmembers class com.example.data.QuickTemplate { *; }
-keepclassmembers class com.example.data.FamilyMemory { *; }

# ── AndroidX Lifecycle & Jetpack Compose ──────────────────────────────────────
-keep class * extends androidx.lifecycle.ViewModel { <init>(...); }
-keep class * extends androidx.lifecycle.AndroidViewModel { <init>(...); }
-dontwarn androidx.compose.**
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.runtime.internal.** { *; }

# ── BroadcastReceivers & Services (Declared in AndroidManifest) ───────────────
-keep class com.example.work.NotificationActionReceiver { *; }
-keep class com.example.work.AlarmReceiver { *; }
-keep class com.example.work.BootReceiver { *; }
-keep class com.example.work.SnoozeAlarmReceiver { *; }
-keep class com.example.widget.** { *; }

# ── Firebase & Crashlytics ───────────────────────────────────────────────────
-dontwarn com.google.firebase.**
-keep class com.google.firebase.** { *; }
-keepclassmembers class com.google.firebase.crashlytics.** { *; }
-keep class com.example.core.util.SilaLogger { *; }
-keepclassmembers class com.example.core.util.SilaLogger { *; }

# ── Coil (Image Loading) ──────────────────────────────────────────────────────
-dontwarn coil.**
-keep class coil.** { *; }

# ── ZXing (QR Code Generation) ────────────────────────────────────────────────
-keep class com.google.zxing.** { *; }

# ── Haze (Frosted Glass UI) ───────────────────────────────────────────────────
-dontwarn dev.chrisbanes.haze.**

# ── ICU / Calendar ────────────────────────────────────────────────────────────
-keep class android.icu.** { *; }

# ── Enums ─────────────────────────────────────────────────────────────────────
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
