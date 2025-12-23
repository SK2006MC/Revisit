# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-dontobfuscate

# Kotlin specific rules
-keepattributes Signature
-keepattributes InnerClasses

# Keep names of data class members
-keepclassmembers class ** {
    @org.jetbrains.annotations.NotNull <fields>;
}

# Keep constructors of data classes for serialization/deserialization
-keepclassmembers class ** {
    public <init>(...);
}

# Keep enum members
-keepclassmembers class ** extends java.lang.Enum {
    <fields>;
    <methods>;
}

# For reflection
-keep class kotlin.Metadata { *; }
-keep class kotlin.coroutines.Continuation { *; }
-keep class kotlin.reflect.KFunction { *; }
-keep class kotlin.reflect.KProperty { *; }
-keepclassmembers class ** {
    public synthetic <fields>;
}

# For coroutines (if used)
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {
    kotlinx.coroutines.CoroutineDispatcher createDispatcher(java.util.List);
}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {
    void handleException(kotlin.coroutines.CoroutineContext, java.lang.Throwable);
}
