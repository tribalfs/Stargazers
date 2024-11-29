package com.tribalfs.stargazers.ui.screens.settings

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.preference.DropDownPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.Preference.SummaryProvider
import androidx.preference.SeslSwitchPreferenceScreen
import androidx.preference.SwitchPreferenceCompat
import com.tribalfs.stargazers.R
import com.tribalfs.stargazers.data.StargazersRepo.Companion.PREF_ACTIONMODE_SEARCH
import com.tribalfs.stargazers.data.StargazersRepo.Companion.PREF_AUTO_DARK_MODE
import com.tribalfs.stargazers.data.StargazersRepo.Companion.PREF_DARK_MODE
import com.tribalfs.stargazers.data.StargazersRepo.Companion.PREF_INDEXSCROLL_ENABLE
import com.tribalfs.stargazers.data.StargazersRepo.Companion.PREF_SEACH_HIGHLIGHT_COLOR
import com.tribalfs.stargazers.data.StargazersRepo.Companion.PREF_SEARCHMODE_BACK_BEHAVIOR
import com.tribalfs.stargazers.data.StargazersRepo.Companion.PREF_UPDATE_AVAILABLE
import com.tribalfs.stargazers.data.util.determineDarkMode
import com.tribalfs.stargazers.ui.core.util.applyDarkMode
import com.tribalfs.stargazers.ui.core.util.loadImageFromUrl
import com.tribalfs.stargazers.ui.core.util.openUrl
import com.tribalfs.stargazers.ui.screens.about.AboutAppActivity
import com.tribalfs.stargazers.ui.screens.settings.base.AbsBasePreferencesFragment
import dev.oneuiproject.oneui.ktx.onClick
import dev.oneuiproject.oneui.ktx.onNewValue
import dev.oneuiproject.oneui.ktx.setSummaryUpdatable
import dev.oneuiproject.oneui.ktx.showDotBadge
import dev.oneuiproject.oneui.preference.ColorPickerPreference
import dev.oneuiproject.oneui.preference.HorizontalRadioPreference
import dev.oneuiproject.oneui.preference.internal.PreferenceRelatedCard
import dev.oneuiproject.oneui.utils.PreferenceUtils


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
            onNewValue {
                // Ensure it is saved first before updating the app's night mode,
                // which will recreate the activity that prevents the preference from being saved.
                value = it
                applyDarkModePrefs()
            }
        }

        findPreference<SwitchPreferenceCompat>(PREF_AUTO_DARK_MODE.name)!!.apply {
            onNewValue{
                // Ensure it is saved first before updating the app's night mode,
                // which will recreate the activity that prevents the preference from being saved.
                isChecked = it
                applyDarkModePrefs()
            }
        }

        findPreference<SeslSwitchPreferenceScreen>(PREF_INDEXSCROLL_ENABLE.name)!!.apply {
            onClick {
                findNavController().navigate(R.id.to_indexscroll_preference_action)
            }
            summaryProvider = SummaryProvider<SeslSwitchPreferenceScreen> { "Autohide  •  Show letters" }
        }

        findPreference<DropDownPreference>(PREF_ACTIONMODE_SEARCH.name)!!.setSummaryUpdatable(true)

        findPreference<ListPreference>(PREF_SEARCHMODE_BACK_BEHAVIOR.name)!!.setSummaryUpdatable(true)

        findPreference<ColorPickerPreference>(PREF_SEACH_HIGHLIGHT_COLOR.name)!!.apply {
            setSummaryUpdatable(true)
            @OptIn(ExperimentalStdlibApi::class)
            summaryProvider = SummaryProvider<ColorPickerPreference> {
                "#${it.value.toHexString(HexFormat.UpperCase)}"
            }
        }


        findPreference<Preference>("about")!!.apply {
            onClick {
                startActivity(Intent(requireActivity(), AboutAppActivity::class.java))
            }
            if (preferenceDataStore!!.getBoolean(PREF_UPDATE_AVAILABLE.name, false)){
                showDotBadge()
            }
        }

        findPreference<Preference>("yann")!!.apply {
            loadImageFromUrl("https://avatars.githubusercontent.com/u/57589186?v=4")
            onClick { requireContext().openUrl("https://github.com/Yanndroid") }
        }

        findPreference<Preference>("salvo")!!.apply {
            loadImageFromUrl("https://avatars.githubusercontent.com/u/13062958?v=4")
            onClick { requireContext().openUrl("https://github.com/salvogiangri") }
        }

        findPreference<Preference>("tribalfs")!!.apply {
            loadImageFromUrl("https://avatars.githubusercontent.com/u/65062033?v=4")
            onClick { requireContext().openUrl("https://github.com/tribalfs") }
        }
    }

    private fun Preference.applyDarkModePrefs() {
        val darkMode = with(preferenceDataStore!!) {
            determineDarkMode(getString(PREF_DARK_MODE.name, "0")!!,
                getBoolean(PREF_AUTO_DARK_MODE.name, true))
        }
        applyDarkMode(darkMode)
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