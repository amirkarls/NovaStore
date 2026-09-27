package com.novastore.app.util

import android.content.Context
import android.content.pm.PackageManager

object AppUtils {
    fun isInstalled(context: Context, packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
}
