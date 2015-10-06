# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/wangziwe/Library/Android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-dontobfuscate

-keepclassmembers class ** {
    public void onEvent*(**);
}

# Only required if you use AsyncExecutor
-keepclassmembers class * extends de.greenrobot.event.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}

# Slf4j and Logback
-keep class ch.qos.** { *; }
-keep class org.slf4j.** { *; }
-keepattributes *Annotation*
-dontwarn ch.qos.logback.core.net.*

# Guava
-dontwarn javax.annotation.**
-dontwarn javax.inject.**
-dontwarn sun.misc.Unsafe
-keep class com.google.common.collect.ImmutableList {
    public static ** reverse(**);
}
-keep class com.google.common.collect.ImmutableSet {
    public static ** reverse(**);
}
-keep class com.google.common.base.Splitter {
    public static ** reverse(**);
    public ** split(...);
}

# Android Annotations
-dontwarn org.androidannotations.api.rest.**

# Acacia
-dontwarn rx.**
-keep class com.mysticwind.disabledappmanager.** {
    *;
}

