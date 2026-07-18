# SQLCipher
-keep class net.sqlcipher.** { *; }
-dontwarn net.sqlcipher.**

# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Hilt / Dagger
-dontwarn com.google.errorprone.annotations.**

# Keep entity/data classes used by Room reflection
-keep class com.wealthwise.app.data.local.entity.** { *; }
-keep class com.wealthwise.app.domain.model.** { *; }
