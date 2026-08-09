# R8 full mode is on by default in AGP 8.x (PLAN.md §6).

# Strip all logging from release builds. AppLog is the single entry point for logging
# (core:common), so removing these calls guarantees no file path, package name, or OCR
# text can reach logcat in a shipped build.
-assumenosideeffects class android.util.Log {
    public static *** v(...);
    public static *** d(...);
    public static *** i(...);
    public static boolean isLoggable(...);
}

# Room generates implementations reflectively at runtime.
-keep class * extends androidx.room.RoomDatabase { <init>(); }
-dontwarn androidx.room.paging.**

# Hilt / Dagger generated components.
-keep,allowobfuscation @interface dagger.hilt.**

# ML Kit text recognition loads its model implementation dynamically.
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**

# Kotlin coroutines debug agent is absent in release.
-dontwarn kotlinx.coroutines.debug.**

# Keep the app's own crash-relevant line numbers while still obfuscating names.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
