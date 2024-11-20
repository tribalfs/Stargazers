package com.tribalfs.stargazers.ui.screens.settings.base

import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.tribalfs.stargazers.data.datastore.PreferenceDataStoreImpl

abstract class AbsBasePreferencesFragment : PreferenceFragmentCompat(),
    PreferenceDataStoreImpl.OnPreferencesChangeListener {

    @CallSuper
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.preferenceDataStore = PreferenceDataStoreImpl.getInstance(requireContext())
    }

    @CallSuper
    override fun onPreferenceChanged(key: String, newValue: Any?) {
        findPreference<Preference>(key)?.apply {
            onPreferenceChangeListener?.onPreferenceChange(this, newValue)
        }
    }

    @CallSuper
    @Override
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PreferenceDataStoreImpl.getInstance(requireContext()).addOnPreferencesChangeListener(this)
    }

    @CallSuper
    override fun onDestroy() {
        super.onDestroy()
        PreferenceDataStoreImpl.getInstance(requireContext()).removeOnPreferencesChangeListener(this)
    }

}