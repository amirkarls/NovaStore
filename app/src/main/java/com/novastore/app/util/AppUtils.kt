package com.novastore.app.util

import android.content.Context
import android.content.pm.PackageManager

object AppUtils {
    fun getInstalledVersionCode(context: Context, packageName: String): Long? {
        return try {
            val info = context.packageManager.getPackageInfo(packageName, 0)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                info.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                info.versionCode.toLong()
            }
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    fun isInstalled(context: Context, packageName: String): Boolean {
        return getInstalledVersionCode(context, packageName) != null
    }
}
