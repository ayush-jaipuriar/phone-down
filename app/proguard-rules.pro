# Phone Down ProGuard/R8 Rules

# Keep model classes for serialization
-keep class phonedown.core.model.** { *; }

# Keep repository interfaces for dependency injection
-keep interface phonedown.core.model.repository.** { *; }

# Keep Hilt-generated classes
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.internal.GeneratedComponent { *; }

# Keep Room entities and DAOs
-keep class phonedown.core.database.entity.** { *; }
-keep interface phonedown.core.database.dao.** { *; }

# Keep DataStore keys
-keepclassmembers class phonedown.core.datastore.repository.DataStoreSettingsRepository$Companion {
    *;
}

# Obfuscate but keep certificate pinning config structure
-keep class phonedown.app.security.CertificatePinningConfig { *; }

# Remove logging in release builds
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}

-assumenosideeffects class phonedown.app.security.SecureLogger {
    public static void d(...);
    public static void i(...);
    public static void w(...);
    public static void e(...);
}

# SQL injection prevention - Room handles this automatically
# but we keep the warning here for documentation
# Room uses parameterized queries for all @Query methods
