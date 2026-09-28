package com.novastore.app.util

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

object IconSwitcher {

    val allIcons = listOf("white", "purple", "blue", "pink", "green", "orange")

    private fun aliasName(context: Context, icon: String): ComponentName {
        val pkg = context.packageName
        val suffix = when (icon) {
            "white" -> "IconWhite"
            "purple" -> "IconPurple"
            "blue" -> "IconBlue"
            "pink" -> "IconPink"
            "green" -> "IconGreen"
            "orange" -> "IconOrange"
            else -> "IconWhite"
        }
        return ComponentName(pkg, "$pkg.$suffix")
    }

    fun switchIcon(context: Context, icon: String) {
        val pm = context.packageManager
        allIcons.forEach { current ->
            val component = aliasName(context, current)
            val newState = if (current == icon) {
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            } else {
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            }
            try {
                pm.setComponentEnabledSetting(
                    component,
                    newState,
                    PackageManager.DONT_KILL_APP
                )
            } catch (e: Exception) {
            }
        }
    }
}
