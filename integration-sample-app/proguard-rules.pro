# keep debug info for more useful stack traces
-keepattributes SourceFile,LineNumberTable
-dontobfuscate

# Kotlin-specific
-keep class kotlin.reflect.jvm.internal.** { *; }
-keep class kotlin.Metadata { *; }
-keepclassmembers class **$WhenMappings {
    <fields>;
}

# serialization-specific
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt # core serialization annotations
-dontnote kotlinx.serialization.SerializationKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# EIDU-specific
-keep class com.eidu.** { *; }
