# Security: Enhanced ProGuard rules for AndroidLedgerPay

# Basic Android rules
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception
-keepattributes *Annotation*

# Security: Hilt dependency injection
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.EntryPoint
-keep class * extends dagger.hilt.android.HiltAndroidApp
-keep class javax.inject.** { *; }
-keepattributes *Annotation*
-dontwarn javax.inject.**
-dontwarn dagger.hilt.**

# Security: Keep sensitive classes and methods
-keep class com.example.ledgerpay.core.data.prefs.SecureStorage {
    public *;
}
-keep class com.example.ledgerpay.core.network.ApiClient {
    public *;
}
-keep class com.example.ledgerpay.core.data.AuthRepository {
    public *;
}

# Security: Obfuscate but keep critical classes readable for debugging
-keepnames class com.example.ledgerpay.core.data.monitoring.Monitoring
-keepnames class com.example.ledgerpay.feature.auth.vm.AuthViewModel
-keepnames class com.example.ledgerpay.feature.payments.vm.PaymentsViewModel

# Security: Protect sensitive strings and resources
-obfuscationdictionary proguard-dict.txt
-classobfuscationdictionary proguard-dict.txt
-packageobfuscationdictionary proguard-dict.txt

# Security: Remove logging in release builds
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
}
-assumenosideeffects class timber.log.Timber {
    public static *** d(...);
    public static *** v(...);
}

# Security: Keep enums readable for API compatibility
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Security: Retrofit and Moshi
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault

-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

-dontwarn org.codehaus.moshi.**
-keepnames @com.squareup.moshi.JsonClass class *
-dontwarn com.squareup.moshi.JsonReader
-keep @com.squareup.moshi.JsonClass class *

# Security: Room database
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn org.codehaus.moshi.**

# Security: Biometric authentication
-keep class androidx.biometric.** { *; }
-keep class androidx.fragment.app.FragmentActivity

# Security: Compose
-keep class * extends androidx.compose.ui.platform.AbstractComposeView { *; }
-keep class * extends androidx.lifecycle.ViewModel { *; }

# Security: EncryptedSharedPreferences
-keep class androidx.security.crypto.** { *; }

# Security: Advanced obfuscation settings
-repackageclasses 'a'
-allowaccessmodification
-optimizations code/simplification/arithmetic,code/simplification/cast,!code/simplification/field,!code/simplification/branch,!code/simplification/string,!code/simplification/math
-mergeinterfacesaggressively
-overloadaggressively
