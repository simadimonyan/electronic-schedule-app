# General Android settings
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Preserve annotations (for libraries like Gson, Retrofit, Room, etc.)
-keepattributes *Annotation*
-keepattributes Signature, InnerClasses

# Keep BouncyCastle classes
-keep class org.bouncycastle.jsse.** { *; }

# Keep Conscrypt classes
-keep class org.conscrypt.** { *; }

# Keep OpenJSSE classes
-keep class org.openjsse.** { *; }

# Suppress warnings for missing BouncyCastle classes
-dontwarn org.bouncycastle.jsse.BCSSLParameters
-dontwarn org.bouncycastle.jsse.BCSSLSocket
-dontwarn org.bouncycastle.jsse.provider.BouncyCastleJsseProvider

# Suppress warnings for missing Conscrypt classes
-dontwarn org.conscrypt.Conscrypt$Version
-dontwarn org.conscrypt.Conscrypt
-dontwarn org.conscrypt.ConscryptHostnameVerifier

# Suppress warnings for missing OpenJSSE classes
-dontwarn org.openjsse.javax.net.ssl.SSLParameters
-dontwarn org.openjsse.javax.net.ssl.SSLSocket
-dontwarn org.openjsse.net.ssl.OpenJSSE

-keep public class com.vk.push.** extends android.os.Parcelable

# Keep application model classes (serialization/deserialization)
-keep class com.mycollege.schedule.** { *; }

# For Gson (JSON serialization/deserialization)
-keep class com.google.gson.** { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keepattributes AnnotationDefault, RuntimeVisibleAnnotations

# Keep classes extending TypeToken (for generic handling in Gson)
-keep class * extends com.google.gson.reflect.TypeToken

# For Jetpack Compose (UI Framework)
-keep class androidx.compose.** { *; }
-keep class androidx.lifecycle.** { *; }
-keep class androidx.navigation.** { *; }

# For Coroutines
-dontwarn kotlinx.coroutines.**
-keep class kotlinx.coroutines.** { *; }

# Optional: Hide source file names to improve obfuscation
# -renamesourcefileattribute ObfuscatedFile

# Prevent stripping of enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Prevent stripping of Parcelable implementations
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator CREATOR;
}

# Optional: For debugging stack traces
# Uncomment the following to keep method names intact (useful for debugging but reduces obfuscation)
-keep class com.mycollege.schedule.** { *; }
-keepattributes SourceFile,LineNumberTable
-keep class * {
    public *;
}

# Optional: Exclude logging libraries like Timber from obfuscation
-keep class timber.log.** { *; }
-dontwarn timber.log.**

# Exclude generated code from being stripped (like Dagger, Hilt, etc.)
-keep class dagger.** { *; }
-dontwarn dagger.**
-keep class javax.inject.** { *; }
-dontwarn javax.inject.**

# Optional: For Kotlin-specific classes
-keep class kotlin.** { *; }
-keepclassmembers class kotlin.** { *; }
-dontwarn kotlin.**


-dontwarn javax.lang.model.SourceVersion
-dontwarn javax.lang.model.element.AnnotationMirror
-dontwarn javax.lang.model.element.AnnotationValue
-dontwarn javax.lang.model.element.AnnotationValueVisitor
-dontwarn javax.lang.model.element.Element
-dontwarn javax.lang.model.element.ElementKind
-dontwarn javax.lang.model.element.ElementVisitor
-dontwarn javax.lang.model.element.ExecutableElement
-dontwarn javax.lang.model.element.Modifier
-dontwarn javax.lang.model.element.Name
-dontwarn javax.lang.model.element.NestingKind
-dontwarn javax.lang.model.element.PackageElement
-dontwarn javax.lang.model.element.TypeElement
-dontwarn javax.lang.model.element.TypeParameterElement
-dontwarn javax.lang.model.element.VariableElement
-dontwarn javax.lang.model.type.ArrayType
-dontwarn javax.lang.model.type.DeclaredType
-dontwarn javax.lang.model.type.ErrorType
-dontwarn javax.lang.model.type.ExecutableType
-dontwarn javax.lang.model.type.NoType
-dontwarn javax.lang.model.type.PrimitiveType
-dontwarn javax.lang.model.type.TypeKind
-dontwarn javax.lang.model.type.TypeMirror
-dontwarn javax.lang.model.type.TypeVariable
-dontwarn javax.lang.model.type.TypeVisitor
-dontwarn javax.lang.model.type.WildcardType
-dontwarn javax.lang.model.util.ElementFilter
-dontwarn javax.lang.model.util.SimpleAnnotationValueVisitor7
-dontwarn javax.lang.model.util.SimpleAnnotationValueVisitor8
-dontwarn javax.lang.model.util.SimpleElementVisitor8
-dontwarn javax.lang.model.util.SimpleTypeVisitor7
-dontwarn javax.lang.model.util.SimpleTypeVisitor8
-dontwarn javax.lang.model.util.Types
-dontwarn javax.tools.Diagnostic$Kind
-dontwarn javax.tools.FileObject
-dontwarn javax.tools.ForwardingJavaFileObject
-dontwarn javax.tools.JavaFileManager$Location
-dontwarn javax.tools.JavaFileObject$Kind
-dontwarn javax.tools.JavaFileObject
-dontwarn javax.tools.SimpleJavaFileObject
-dontwarn javax.tools.StandardLocation
-dontwarn sun.reflect.annotation.AnnotationParser
-dontwarn sun.reflect.annotation.AnnotationType
-dontwarn sun.reflect.annotation.EnumConstantNotPresentExceptionProxy
-dontwarn sun.reflect.annotation.ExceptionProxy
