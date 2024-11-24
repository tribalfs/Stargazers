package com.tribalfs.stargazers.ui.screens.settings.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.preference.DropDownPreference
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.SeslSwitchPreferenceScreen
import androidx.preference.SwitchPreferenceCompat
import com.tribalfs.stargazers.R
import com.tribalfs.stargazers.data.DarkMode
import com.tribalfs.stargazers.data.StargazersRepo.Companion.PREF_ACTIONMODE_SEARCH
import com.tribalfs.stargazers.data.StargazersRepo.Companion.PREF_AUTO_DARK_MODE
import com.tribalfs.stargazers.data.StargazersRepo.Companion.PREF_DARK_MODE
import com.tribalfs.stargazers.data.StargazersRepo.Companion.PREF_INDEXSCROLL_ENABLE
import com.tribalfs.stargazers.data.StargazersRepo.Companion.PREF_SEACH_HIGHLIGHT_COLOR
import com.tribalfs.stargazers.ui.core.util.DarkModeUtils
import com.tribalfs.stargazers.ui.core.util.DarkModeUtils.toDarkMode
import com.tribalfs.stargazers.ui.core.util.openUrl
import com.tribalfs.stargazers.ui.screens.about.AboutAppActivity
import com.tribalfs.stargazers.ui.screens.settings.base.AbsBasePreferencesFragment
import com.tribalfs.stargazers.ui.screens.settings.indexscroll.IndexscrollSettingsActivity
import dev.oneuiproject.oneui.ktx.clearBadge
import dev.oneuiproject.oneui.ktx.isLightMode
import dev.oneuiproject.oneui.ktx.onClick
import dev.oneuiproject.oneui.ktx.onNewValue
import dev.oneuiproject.oneui.ktx.onUpdateValue
import dev.oneuiproject.oneui.ktx.setUpdatableSummaryColor
import dev.oneuiproject.oneui.ktx.showDotBadge
import dev.oneuiproject.oneui.preference.HorizontalRadioPreference
import dev.oneuiproject.oneui.preference.TipsCardPreference
import dev.oneuiproject.oneui.preference.internal.PreferenceRelatedCard
import dev.oneuiproject.oneui.utils.PreferenceUtils

//TODO:
// 1. lib:  make onNewValue type safe
//
class MainSettingsFragment : AbsBasePreferencesFragment(){
    private var mRelativeLinkCard: PreferenceRelatedCard? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)
        setPreferencesFromResource(R.xml.preferences_main, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initPreferences()
    }

    override fun onResume() {
        setRelativeLinkCard()
        super.onResume()
    }

    private fun initPreferences() {

        findPreference<HorizontalRadioPreference>(PREF_DARK_MODE.name)!!.apply {
            setDividerEnabled(false)
            setTouchEffectEnabled(false)
            value = if (requireContext().isLightMode()) "0" else "1"
            onNewValue<String> { DarkModeUtils.darkMode =   it.toDarkMode() }
        }

        findPreference<SwitchPreferenceCompat>(PREF_AUTO_DARK_MODE.name)!!.apply {
            onUpdateValue<Boolean> {
                if (it) DarkModeUtils.darkMode = DarkMode.AUTO
                true
            }
            isChecked = DarkModeUtils.darkMode == DarkMode.AUTO
        }

        findPreference<SeslSwitchPreferenceScreen>(PREF_INDEXSCROLL_ENABLE.name)!!.apply {
            setUpdatableSummaryColor(true)
            onNewValue<Boolean> {
                summary = if (it) "Enabled" else "Disabled"
            }
            onClick { requireActivity().startActivity(
                Intent(
                    requireContext(),
                    IndexscrollSettingsActivity::class.java
                )
            ) }
            summary = if (isChecked) "Enabled" else "Disabled"
        }

        findPreference<DropDownPreference>(StargazersRepo.PREF_ACTIONMODE_SEARCH.name)!!.apply {
            setUpdatableSummaryColor(true)
        }

        findPreference<ListPreference>(StargazersRepo.PREF_SEARCHMODE_BACK_BEHAVIOR.name)!!.apply {
            setUpdatableSummaryColor(true)
        }

        findPreference<Preference>("about")!!.apply {
            onClick {
                startActivity(Intent(requireActivity(), AboutAppActivity::class.java))
                preferenceDataStore!!.putBoolean("about_badge_seen", true)
                it.clearBadge()
            }
            if (!preferenceDataStore!!.getBoolean("about_badge_seen", false)){
                showDotBadge()
            }
        }

        findPreference<Preference>("yann")!!.apply {
            onClick { requireContext().openUrl("https://github.com/Yanndroid") }
        }

        findPreference<Preference>("salvo")!!.apply {
            onClick { requireContext().openUrl("https://github.com/salvogiangri") }
        }

        findPreference<Preference>("tribalfs")!!.apply {
            onClick { requireContext().openUrl("https://github.com/tribalfs") }
        }

    }

    private fun setRelativeLinkCard() {
        if (mRelativeLinkCard == null) {
            mRelativeLinkCard = PreferenceUtils.createRelatedCard(requireContext())
            mRelativeLinkCard!!
                .addButton("OneUI6 Design Lib"){
                    requireContext().openUrl("https://github.com/tribalfs/oneui-design")}
                .addButton("SESL6 Android Jetpack Modules") {
                    requireContext().openUrl("https://github.com/tribalfs/sesl-androidx")}
                .addButton("SESL6 Material Components for Android"){
                    requireContext().openUrl("https://github.com/tribalfs/sesl-material-components-android")
                }
                .show(this)
        }
    }

}