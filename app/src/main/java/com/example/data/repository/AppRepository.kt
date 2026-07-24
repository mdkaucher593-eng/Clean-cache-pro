package com.example.data.repository

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import com.example.data.model.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class AppRepository(private val context: Context) {

    suspend fun getInstalledApps(): List<AppInfo> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val apps = mutableListOf<AppInfo>()

        val packages = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getInstalledPackages(PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.getInstalledPackages(0)
            }
        } catch (e: Exception) {
            emptyList()
        }

        for (packageInfo in packages) {
            val appInfo = packageInfo.applicationInfo ?: continue
            val packageName = packageInfo.packageName ?: continue

            val appName = try {
                pm.getApplicationLabel(appInfo).toString()
            } catch (e: Exception) {
                packageName
            }

            val isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0

            val apkSize = try {
                val sourceDir = appInfo.sourceDir
                if (sourceDir != null) File(sourceDir).length() else 0L
            } catch (e: Exception) {
                0L
            }

            val versionName = packageInfo.versionName ?: "1.0"
            val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toLong()
            }

            val firstInstallTime = packageInfo.firstInstallTime
            val lastUpdateTime = packageInfo.lastUpdateTime
            val targetSdkVersion = appInfo.targetSdkVersion

            // Estimate potential cache footprint as proportional ratio for user guidance
            val estimatedCache = if (!isSystemApp) {
                (apkSize * 0.25).toLong().coerceIn(1_048_576L, 150_000_000L)
            } else {
                (apkSize * 0.10).toLong().coerceAtMost(50_000_000L)
            }

            apps.add(
                AppInfo(
                    packageName = packageName,
                    appName = appName,
                    versionName = versionName,
                    versionCode = versionCode,
                    firstInstallTime = firstInstallTime,
                    lastUpdateTime = lastUpdateTime,
                    targetSdkVersion = targetSdkVersion,
                    isSystemApp = isSystemApp,
                    apkSizeBytes = apkSize,
                    estimatedCacheBytes = estimatedCache
                )
            )
        }

        apps
    }
}
