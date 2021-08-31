# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /home/platypii/android/sdk/tools/proguard/proguard-android.txt
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

# EventBus ProGuard config:
-keepattributes *Annotation*
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }

# If a field is annotated for gson don't erase it
-keepclassmembers class ** {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Only required if you use AsyncExecutor
-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}

-keepattributes EnclosingMethod
-keepattributes InnerClasses

-dontobfuscate

# For stacktraces // https://developer.android.com/studio/build/shrink-code
-keepattributes LineNumberTable,SourceFile
