package com.tribalfs.stargazers.ui.screens.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.tribalfs.stargazers.R
import com.tribalfs.stargazers.databinding.ActivityPreferencesBinding

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding =ActivityPreferencesBinding.inflate(layoutInflater).apply {
            setContentView(root)
            toolbarLayout.setNavigationButtonAsBack()
        }

        val navHostFragment = binding.navHostSettings.getFragment() as NavHostFragment
        val navController = navHostFragment.navController

        navController.addOnDestinationChangedListener { _, dest, _ ->
            if (dest.id == R.id.main_preference_dest) {
                binding.toolbarLayout.setTitle("Stargazers Settings")
            } else {
                binding.toolbarLayout.setTitle("Indexscroll Settings")
            }
        }
    }


}