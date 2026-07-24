package com.example.data.model

data class AppInfo(
    val packageName: String,
    val appName: String,
    val versionName: String,
    val versionCode: Long,
    val firstInstallTime: Long,
    val lastUpdateTime: Long,
    val targetSdkVersion: Int,
    val isSystemApp: Boolean,
    val apkSizeBytes: Long,
    val estimatedCacheBytes: Long = 0L
) {
    val totalEstimatedStorageBytes: Long
        get() = apkSizeBytes + estimatedCacheBytes
}

enum class SortOrder(val displayName: String) {
    NAME_ASC("Name (A-Z)"),
    NAME_DESC("Name (Z-A)"),
    SIZE_DESC("Storage Size (Largest)"),
    DATE_DESC("Recently Updated")
}

enum class AppFilter(val displayName: String) {
    ALL("All Apps"),
    USER("User Installed"),
    SYSTEM("System Apps")
}
