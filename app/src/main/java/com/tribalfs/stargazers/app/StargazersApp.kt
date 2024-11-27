package com.tribalfs.stargazers.app

import android.app.Application
import com.tribalfs.stargazers.data.StargazersRepo
import com.tribalfs.stargazers.ui.core.util.applyDarkMode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class StargazersApp : Application() {
    override fun onCreate() {
        super.onCreate()
        applyDarkModeFromPrefs()
    }

    //App dark mode settings maybe different from the device settings
    private fun applyDarkModeFromPrefs() = runBlocking {
        val darkMode = StargazersRepo.getInstance(this@StargazersApp).stargazersSettingsFlow.first().darkModeOption
        applyDarkMode(darkMode)
    }
}