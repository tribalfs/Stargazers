package com.tribalfs.stargazers.ui.screens.settings.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tribalfs.stargazers.databinding.ActivityPreferencesBinding

class MainSettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityPreferencesBinding.inflate(layoutInflater).apply {
            setContentView(root)
            toolbarLayout.setTitle("Stargazers Settings")
            toolbarLayout.setNavigationButtonAsBack()
        }

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(binding.fragContainer.id, MainSettingsFragment())
                .commitNow()
        }
    }

}