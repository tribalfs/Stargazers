package com.tribalfs.stargazers.app

import android.app.Application
import com.tribalfs.stargazers.ui.core.util.DarkModeUtils.reapplyDarkModePrefs

class StargazersApp : Application() {
    override fun onCreate() {
        super.onCreate()
        reapplyDarkModePrefs()
    }
}