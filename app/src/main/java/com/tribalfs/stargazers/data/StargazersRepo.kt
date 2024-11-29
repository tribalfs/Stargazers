package com.tribalfs.stargazers.data

import android.content.Context
import android.graphics.Color
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.tribalfs.stargazers.data.local.StargazersDB
import com.tribalfs.stargazers.data.local.datastore.PreferenceDataStoreImpl
import com.tribalfs.stargazers.data.model.Stargazer
import com.tribalfs.stargazers.data.model.StargazersSettings
import com.tribalfs.stargazers.data.network.NetworkDataSource
import com.tribalfs.stargazers.data.network.Update
import com.tribalfs.stargazers.data.network.UpdateDataSource
import dev.oneuiproject.oneui.layout.AppInfoLayout
import com.tribalfs.stargazers.data.util.determineDarkMode
import com.tribalfs.stargazers.data.util.toSearchModeOnActionMode
import com.tribalfs.stargazers.data.util.toSearchModeOnBackBehavior
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class StargazersRepo private constructor(context: Context) {

    private val appContext = context.applicationContext
    private val database = StargazersDB.getDatabase(appContext)

    private val prefDataStoreImpl = PreferenceDataStoreImpl.getInstance(appContext)
    private val dataStore = prefDataStoreImpl.dataStore

    val stargazersFlow: Flow<List<Stargazer>> = database.stargazerDao().getAllStargazers()

    suspend fun refreshStargazers(onRefreshComplete: (isSuccess: Boolean) -> Unit) =
        withContext(Dispatchers.IO) {
            try {
                NetworkDataSource.fetchStargazers()?.let {
                    database.stargazerDao().replaceAll(it)
                    updateLastRefresh(System.currentTimeMillis())
                    onRefreshComplete(true)
                } ?: run {
                    onRefreshComplete(false)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onRefreshComplete(false)
            }
        }

    fun updateLastRefresh(timeMillis: Long) = prefDataStoreImpl.putLong(PREF_LAST_REFRESH.name, timeMillis)

    val stargazersSettingsFlow: Flow<StargazersSettings> = dataStore.data.map {
        val darkMode = determineDarkMode(
            it[PREF_DARK_MODE] ?: "0",
            it[PREF_AUTO_DARK_MODE] ?: true)

        StargazersSettings(
            isTextModeIndexScroll = it[PREF_INDEXSCROLL_TEXT_MODE] ?: false,
            autoHideIndexScroll = it[PREF_INDEXSCROLL_AUTO_HIDE] ?: true,
            searchOnActionMode = it[PREF_ACTIONMODE_SEARCH].toSearchModeOnActionMode(),
            searchModeBackBehavior = it[PREF_SEARCHMODE_BACK_BEHAVIOR].toSearchModeOnBackBehavior(),
            searchHighlightColor = it[PREF_SEACH_HIGHLIGHT_COLOR] ?: Color.parseColor("#2196F3"),
            darkModeOption = darkMode,
            enableIndexScroll = it[PREF_INDEXSCROLL_ENABLE] ?: true,
            lastRefresh = it[PREF_LAST_REFRESH] ?: 0,
            initTipShown = it[PREF_INIT_TIP_SHOWN] ?: false,
            updateAvailable = it[PREF_UPDATE_AVAILABLE] ?: false
        )
    }

    fun setInitTupShown() = prefDataStoreImpl.putBoolean(PREF_INIT_TIP_SHOWN.name, true)

    suspend fun getUpdate(): Update{
        return UpdateDataSource.getUpdate().also {
            prefDataStoreImpl.putBoolean(PREF_UPDATE_AVAILABLE.name, it.status == AppInfoLayout.UPDATE_AVAILABLE)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: StargazersRepo? = null

        /**
         * Returns the singleton instance of [StargazersRepo].
         */
        fun getInstance(context: Context): StargazersRepo = INSTANCE
            ?: synchronized(this) {
                StargazersRepo(context.applicationContext).also {
                    INSTANCE = it
                }
            }

        val PREF_INDEXSCROLL_ENABLE = booleanPreferencesKey("enableIndexScroll")
        val PREF_INDEXSCROLL_TEXT_MODE = booleanPreferencesKey("indexScrollTextMode")
        val PREF_INDEXSCROLL_AUTO_HIDE = booleanPreferencesKey("indexScrollAutoHide")
        val PREF_ACTIONMODE_SEARCH = stringPreferencesKey("actionModeSearch")
        val PREF_SEARCHMODE_BACK_BEHAVIOR = stringPreferencesKey("searchModeBackBehavior")
        val PREF_SEACH_HIGHLIGHT_COLOR = intPreferencesKey("searchColor")
        val PREF_DARK_MODE = stringPreferencesKey("darkMode")
        val PREF_AUTO_DARK_MODE = booleanPreferencesKey("darkModeAuto")
        val PREF_LAST_REFRESH = longPreferencesKey("lastRefresh")
        val PREF_UPDATE_AVAILABLE = booleanPreferencesKey("updateAvailable")
        private val PREF_INIT_TIP_SHOWN = booleanPreferencesKey("initTipShown")
    }
}
