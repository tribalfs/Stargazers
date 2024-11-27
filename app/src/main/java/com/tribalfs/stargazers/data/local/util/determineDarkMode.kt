package com.tribalfs.stargazers.data.local.util

import com.tribalfs.stargazers.data.model.DarkMode

fun determineDarkMode(darkModeValue: String, darkModeAutoValue: Boolean): DarkMode {
    return if (darkModeAutoValue) {
        DarkMode.AUTO
    } else {
        when (darkModeValue) {
            "0" -> DarkMode.DISABLED
            else -> DarkMode.ENABLED
        }
    }
}