# Grove ProGuard rules

# Keep Compose
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Keep DataStore
-keep class androidx.datastore.** { *; }
-dontwarn androidx.datastore.**

# Keep model classes
-keep class com.grove.app.data.model.** { *; }
-keep class com.grove.app.data.BudgetState { *; }
-keep class com.grove.app.data.UserPreferences { *; }

# Keep Kotlin metadata
-keepattributes RuntimeVisibleAnnotations
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses

# Coroutines
-dontwarn kotlinx.**
-keep class kotlinx.coroutines.** { *; }
