# 定时提醒 ProGuard Rules

# Keep Room entities
-keep class com.example.timereminder.data.db.entity.** { *; }

# Keep serialization
-keepattributes *Annotation*

# Keep Compose
-dontwarn androidx.compose.**
