package com.tribalfs.stargazers.data

import android.content.Context
import android.graphics.Color
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.tribalfs.stargazers.data.local.StargazersDB
import com.tribalfs.stargazers.data.local.datastore.PreferenceDataStoreImpl
import com.tribalfs.stargazers.data.model.FetchState
import com.tribalfs.stargazers.data.model.FetchState.INITED
import com.tribalfs.stargazers.data.model.FetchState.INITING
import com.tribalfs.stargazers.data.model.FetchState.INIT_ERROR
import com.tribalfs.stargazers.data.model.FetchState.NOT_INIT
import com.tribalfs.stargazers.data.model.FetchState.REFRESHED
import com.tribalfs.stargazers.data.model.FetchState.REFRESHING
import com.tribalfs.stargazers.data.model.FetchState.REFRESH_ERROR
import com.tribalfs.stargazers.data.model.Stargazer
import com.tribalfs.stargazers.data.model.StargazersSettings
import com.tribalfs.stargazers.data.network.NetworkDataSource
import com.tribalfs.stargazers.data.network.Update
import com.tribalfs.stargazers.data.network.UpdateDataSource
import com.tribalfs.stargazers.data.util.determineDarkMode
import com.tribalfs.stargazers.data.util.toFetchStatus
import com.tribalfs.stargazers.data.util.getEffectiveHighlightColor
import com.tribalfs.stargazers.data.util.toSearchModeOnActionMode
import com.tribalfs.stargazers.data.util.toSearchModeOnBackBehavior
import dev.oneuiproject.oneui.layout.AppInfoLayout.UPDATE_AVAILABLE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class StargazersRepo private constructor(context: Context) {

    private val appContext = context.applicationContext
    private val database = StargazersDB.getDatabase(appContext)

    private val prefDataStoreImpl = PreferenceDataStoreImpl.getInstance(appContext)
    private val dataStore = prefDataStoreImpl.dataStore

    val stargazersFlow: Flow<List<Stargazer>> = database.stargazerDao().getAllStargazers()

    suspend fun refreshStargazers(onRefreshComplete: ((isSuccess: Boolean, exception: Exception?) -> Unit)? = null) =
        withContext(Dispatchers.IO) {
            try {
                setOnStartFetchStatus()
                val stargazers = NetworkDataSource.fetchStargazers()
                if (stargazers != null){
                    database.stargazerDao().replaceAll(stargazers)
                    updateLastRefresh(System.currentTimeMillis())
                    setOnFinishFetchStatus(true)
                    onRefreshComplete?.invoke(true, null)
                } else {
                    setOnFinishFetchStatus(false)
                    onRefreshComplete?.invoke(false, null)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                setOnFinishFetchStatus(false)
                onRefreshComplete?.invoke(false, e)
            }
        }

    private suspend fun setOnStartFetchStatus(){
        when(fetchStatusFlow.first()){
            NOT_INIT,
            INIT_ERROR,
            INITING -> setFetchStatus(INITING)
            INITED,
            REFRESH_ERROR,
            REFRESHED,
            REFRESHING -> setFetchStatus(REFRESHING)
        }
    }

    private suspend fun setOnFinishFetchStatus(isSuccess: Boolean){
        when(fetchStatusFlow.first()){
            REFRESHING -> setFetchStatus(if (isSuccess) REFRESHED else REFRESH_ERROR)
            INITING ->  setFetchStatus(if (isSuccess) INITED else INIT_ERROR)
            else -> Unit//we're not expecting this
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
            searchHighlightColor =  getEffectiveHighlightColor(
                it[PREF_CUSTOM_HIGHLIGHT_COLOR] ?: true,
                it[PREF_SEARCH_HIGHLIGHT_COLOR] ?: Color.parseColor("#2196F3")),
            darkModeOption = darkMode,
            enableIndexScroll = it[PREF_INDEXSCROLL_ENABLE] ?: true,
            lastRefresh = it[PREF_LAST_REFRESH] ?: 0,
            initTipShown = it[PREF_INIT_TIP_SHOWN] ?: false,
            updateAvailable = it[PREF_UPDATE_AVAILABLE] ?: false
        )
    }

    fun setInitTupShown() = prefDataStoreImpl.putBoolean(PREF_INIT_TIP_SHOWN.name, true)

    suspend fun getUpdate(): Update = UpdateDataSource.getUpdate().also {
        prefDataStoreImpl.putBoolean(PREF_UPDATE_AVAILABLE.name, it.status == UPDATE_AVAILABLE)
    }

    suspend fun getStargazersById(ids: IntArray) = database.stargazerDao().getStargazersById(ids)

    val fetchStatusFlow = dataStore.data.map{ it[PREF_INIT_FETCH_STATE].toFetchStatus() }

    fun setFetchStatus(state: FetchState) = prefDataStoreImpl.putInt(PREF_INIT_FETCH_STATE.name, state.ordinal)

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
        val PREF_CUSTOM_HIGHLIGHT_COLOR = booleanPreferencesKey("useCustomHighlightColor")
        val PREF_SEARCH_HIGHLIGHT_COLOR = intPreferencesKey("searchColor")
        val PREF_DARK_MODE = stringPreferencesKey("darkMode")
        val PREF_AUTO_DARK_MODE = booleanPreferencesKey("darkModeAuto")
        val PREF_LAST_REFRESH = longPreferencesKey("lastRefresh")
        val PREF_UPDATE_AVAILABLE = booleanPreferencesKey("updateAvailable")
        private val PREF_INIT_TIP_SHOWN = booleanPreferencesKey("initTipShown")
        private val PREF_INIT_FETCH_STATE = intPreferencesKey("initFetch")
    }
}
