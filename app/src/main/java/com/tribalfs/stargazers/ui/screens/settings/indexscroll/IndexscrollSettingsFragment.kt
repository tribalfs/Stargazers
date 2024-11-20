package com.tribalfs.stargazers.ui.screens.settings.indexscroll

import android.os.Bundle
import com.tribalfs.stargazers.R
import com.tribalfs.stargazers.ui.screens.settings.base.AbsBasePreferencesFragment

class IndexscrollSettingsFragment : AbsBasePreferencesFragment() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)
        setPreferencesFromResource(R.xml.preferences_indexscroll, rootKey)
    }

}