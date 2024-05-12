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


# When editing this file, update the following files as well:
# - META-INF/com.android.tools/proguard/coroutines.pro
# - META-INF/com.android.tools/r8/coroutines.pro

# ServiceLoader support

#指定代码的压缩级别
-optimizationpasses 5

#包名不混合大小写
-dontusemixedcaseclassnames

#不去忽略非公共的库类
-dontskipnonpubliclibraryclasses

 #优化 不优化输入的类文件
-dontoptimize

 #预校验
-dontpreverify

 #混淆时是否记录日志
-verbose


#保护注解
-keepattributes *Annotation*

-keep public class * extends android.app.Application
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Fragment
-keep public class * extends android.app.Fragment
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.preference.Preference

-keepclasseswithmembernames class * {
  native <methods> ;
}
-keepclassmembers class * extends android.app.Activity {
  public void *(android.view.View);
}
-keepclassmembers enum * {
  public static **[] values();
  public static ** valueOf(java.lang.String);
}
-keep class * implements android.os.Parcelable {
 public static final android.os.Parcelable$Creator *;
}
-keepclassmembers class **.R$* {
  *;
}
-keep class * extends android.view.View{*;}
-keep class * extends android.app.Dialog{*;}
-keep class * implements java.io.Serializable{*;}

-keep class com.lmy.iconcapturer.** { *; }

#butterknife
-keep class butterknife.** { *; }
-dontwarn butterknife.internal.**
-keep class **$$ViewBinder { *; }

#volley
-dontwarn com.android.volley.**
-keep class com.android.volley.**{*;}

#fastjson
-dontwarn com.alibaba.fastjson.**
-keep class com.alibaba.fastjson.**{*;}

#happy-dns
-dontwarn com.qiniu.android.dns.**
-keep class com.qiniu.android.dns.**{*;}

#okhttp
-dontwarn com.squareup.okhttp.**
-keep class com.squareup.okhttp.**{*;}

-keep class okio.**{*;}

-keep class android.net.**{*;}
-keep class com.android.internal.http.multipart.**{*;}
-keep class org.apache.**{*;}

-keep class com.qiniu.android.**{*;}

-keep class android.support.annotation.**{*;}

-keep class com.squareup.wire.**{*;}

-keep class com.ant.liao.**{*;}

#腾讯
-keep class com.tencent.**{*;}

-keep class u.aly.**{*;}

#ImageLoader
-keep class com.nostra13.universalimageloader.**{*;}

#友盟
-dontwarn com.umeng.**
-keep class com.umeng.**{*;}

#pulltorefresh
-keep class com.handmark.pulltorefresh.** { *; }
-keep class android.support.v4.** { *;}
-keep public class * extends android.support.v4.**{
 public protected *;}
-keep class android.support.v7.** {*;}

-keep public class * implements com.bumptech.glide.module.GlideModule

-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

# These classes are only required by kotlinx.coroutines.debug.AgentPremain, which is only loaded when
# kotlinx-coroutines-core is used as a Java agent, so these are not needed in contexts where ProGuard is used.
-dontwarn java.lang.instrument.ClassFileTransformer
-dontwarn sun.misc.SignalHandler
-dontwarn java.lang.instrument.Instrumentation
-dontwarn sun.misc.Signal

# Only used in `kotlinx.coroutines.internal.ExceptionsConstructor`.
# The case when it is not available is hidden in a `try`-`catch`, as well as a check for Android.
-dontwarn java.lang.ClassValue

# An annotation used for build tooling, won't be directly accessed.
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement



# Okhttp3

# JSR 305 annotations are for embedding nullability information.
-dontwarn javax.annotation.**

# A resource is loaded with a relative path so the package of this class must be preserved.
-adaptresourcefilenames okhttp3/internal/publicsuffix/PublicSuffixDatabase.gz

# Animal Sniffer compileOnly dependency to ensure APIs are compatible with older versions of Java.
-dontwarn org.codehaus.mojo.animal_sniffer.*

# OkHttp platform used only on JVM and when Conscrypt and other security providers are available.
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# Keep Kotlin standard library classes
-keep class kotlin.** { *; }

# Keep AndroidX core library classes
-keep class androidx.** { *; }

# Keep Google Material library classes
-keep class com.google.android.material.** { *; }

# Keep AndroidX constraintlayout library classes
-keep class androidx.constraintlayout.** { *; }

# Keep Android Support library classes
-keep class android.support.** { *; }

# Keep Glide library classes (if you decide to uncomment the Glide dependency)
# -keep class com.bumptech.glide.** { *; }

# Keep CircleImageView library classes
-keep class de.hdodenhof.circleimageview.** { *; }

# Keep OpenImageGlideLib library classes
-keep class io.github.FlyJingFish.OpenImage.** { *; }

# Keep AndroidX lifecycle library classes
-keep class androidx.lifecycle.** { *; }

# Keep XLog library classes
-keep class com.elvishew.xlog.** { *; }

# Keep LitePal library classes
-keep class org.litepal.** { *; }

# Keep RecyclerView library classes
-keep class android.support.v7.widget.** { *; }

-keep class com.qcloud.** { *; }
-keep class net.lingala.** { *; }
-keep class com.google.code.** { *; }


# You may need to add more specific rules depending on other libraries used

