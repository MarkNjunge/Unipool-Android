-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
# A resource is loaded with a relative path so the package of this class must be preserved.
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

-keep class com.apollographql.apollo.ApolloClient
-dontwarn com.apollographql.apollo.**
-dontwarn com.google.maps.**
-dontwarn org.joda.time.**
-dontwarn org.slf4j.**

-printmapping out.map

-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable
