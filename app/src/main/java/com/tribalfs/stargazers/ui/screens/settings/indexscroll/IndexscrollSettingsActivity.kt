package com.tribalfs.stargazers.ui.screens.settings.indexscroll

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tribalfs.stargazers.databinding.ActivityPreferencesBinding

class IndexscrollSettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityPreferencesBinding.inflate(layoutInflater).apply {
            setContentView(root)
            toolbarLayout.setTitle("IndexScrollView Settings")
            toolbarLayout.setNavigationButtonAsBack()
        }

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(binding.fragContainer.id, IndexscrollSettingsFragment())
                .commitNow()
        }
    }

}