# ──────────────────────────────────────────────────────────────────────────────
# Sila (صِلَةِ) — ProGuard / R8 Rules for Production
# ──────────────────────────────────────────────────────────────────────────────

# ── Kotlin ────────────────────────────────────────────────────────────────────
-dontwarn kotlin.**
-keep class kotlin.Metadata { *; }
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

# ── Room Database (critical — keeps entities and DAOs from being stripped) ────
-keep class com.example.data.** { *; }
-keep class * extends androidx.room.RoomDatabase
-keepclassmembers class * extends androidx.room.RoomDatabase {
    abstract <methods>;
}
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }
-dontwarn androidx.room.paging.**

# ── Coroutines ────────────────────────────────────────────────────────────────
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.** { volatile <fields>; }

# ── AndroidX / Lifecycle ──────────────────────────────────────────────────────
-keep class * extends androidx.lifecycle.ViewModel { <init>(...); }
-keep class * extends androidx.lifecycle.AndroidViewModel { <init>(...); }

# ── BroadcastReceivers (must not be renamed — declared in Manifest) ──────────
-keep class com.example.work.NotificationActionReceiver { *; }
-keep class com.example.work.AlarmReceiver { *; }
-keep class com.example.work.BootReceiver { *; }
-keep class com.example.work.SnoozeAlarmReceiver { *; }

# ── Workers (WorkManager needs reflection to instantiate) ─────────────────────
-keep class * extends androidx.work.Worker { <init>(android.content.Context, androidx.work.WorkerParameters); }
-keep class * extends androidx.work.CoroutineWorker { <init>(android.content.Context, androidx.work.WorkerParameters); }
-keep class * extends androidx.work.ListenableWorker { <init>(android.content.Context, androidx.work.WorkerParameters); }

# ── Widget ────────────────────────────────────────────────────────────────────
-keep class com.example.widget.** { *; }

# ── Compose (R8 full mode compatibility) ──────────────────────────────────────
-dontwarn androidx.compose.**
-keep class androidx.compose.runtime.** { *; }

# ── Coil (image loading) ─────────────────────────────────────────────────────
-dontwarn coil.**
-keep class coil.** { *; }

# ── ZXing (QR code) ──────────────────────────────────────────────────────────
-keep class com.google.zxing.** { *; }

# ── Haze (glassmorphism) ─────────────────────────────────────────────────────
-dontwarn dev.chrisbanes.haze.**

# ── Android ICU (Islamic Calendar) ───────────────────────────────────────────
-keep class android.icu.** { *; }

# ── Debugging (keep line numbers for crash stack traces) ──────────────────────
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ── Serialization (JSON backup) ──────────────────────────────────────────────
-keep class org.json.** { *; }

# ── Enums ─────────────────────────────────────────────────────────────────────
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
