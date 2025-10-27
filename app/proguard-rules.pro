# My Finances - ProGuard Rules
# Optimized for Jetpack Compose, Room, Hilt, and Vico Charts

# ========================
# General Configuration
# ========================

# Keep source file names and line numbers for better crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep generic signatures for reflection
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses

# ========================
# Kotlin
# ========================

# Keep Kotlin metadata
-keep class kotlin.Metadata { *; }

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# Kotlin serialization (if used in future)
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# ========================
# Jetpack Compose
# ========================

# Keep Composable functions
-keep @androidx.compose.runtime.Composable public class * { *; }
-keep class androidx.compose.** { *; }

# Compose UI
-dontwarn androidx.compose.ui.**
-keep class androidx.compose.ui.** { *; }

# Keep all Compose generated classes
-keep class androidx.compose.runtime.** { *; }

# ========================
# Room Database
# ========================

# Keep Room annotations
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao interface *
-keep @androidx.room.Database abstract class *

# Keep Room compiler generated code
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.* class *

# Keep entity classes and their fields
-keepclassmembers class * extends androidx.room.RoomDatabase {
    public abstract *;
}

# Keep DAO methods
-keepclassmembers @androidx.room.Dao interface * {
    *;
}

# Keep specific Room classes for My Finances
-keep class dev.aulianenko.myfinances.data.entity.** { *; }
-keep class dev.aulianenko.myfinances.data.dao.** { *; }
-keep class dev.aulianenko.myfinances.data.database.** { *; }

# ========================
# Hilt / Dagger
# ========================

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# Keep Hilt annotated classes
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }
-keep @dagger.Module class * { *; }
-keep @dagger.Component class * { *; }
-keep @javax.inject.Inject class * { *; }

# Keep Hilt entry points
-keep @dagger.hilt.InstallIn class *

# ========================
# Vico Charts
# ========================

# Keep Vico chart classes
-keep class com.patrykandpatrick.vico.** { *; }
-dontwarn com.patrykandpatrick.vico.**

# ========================
# DataStore
# ========================

# Keep DataStore preferences
-keep class androidx.datastore.** { *; }
-keepclassmembers class * extends androidx.datastore.preferences.protobuf.GeneratedMessageLite {
    <fields>;
}

# ========================
# Android Components
# ========================

# Keep Android framework
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

# Keep View constructors
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

# ========================
# Serialization & Parceling
# ========================

# Parcelable
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# ========================
# Navigation Component
# ========================

# Keep Navigation arguments
-keepnames class androidx.navigation.fragment.NavHostFragment
-keep class * extends androidx.navigation.Navigator

# ========================
# Debugging (Optional - Remove for production)
# ========================

# Uncomment to see what's being removed
# -whyareyoukeeping class dev.aulianenko.myfinances.**

# ========================
# Warnings to Ignore
# ========================

# Ignore warnings for optional dependencies
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# Ignore Kotlin internal warnings
-dontwarn kotlin.**
-dontwarn kotlinx.**

# ========================
# Project Specific
# ========================

# Keep domain models (used for Room, Hilt, and UI state)
-keep class dev.aulianenko.myfinances.domain.model.** { *; }

# Keep repository classes (injected by Hilt)
-keep class dev.aulianenko.myfinances.data.repository.** { *; }

# Keep ViewModels (used by Hilt and Compose)
-keep class dev.aulianenko.myfinances.ui.screens.**.* extends androidx.lifecycle.ViewModel { *; }

# Keep Currency provider
-keep class dev.aulianenko.myfinances.domain.Currency** { *; }
-keep class dev.aulianenko.myfinances.domain.CurrencyProvider { *; }