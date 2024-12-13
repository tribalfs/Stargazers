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
import com.tribalfs.stargazers.data.util.getEffectiveHighlightColor
import com.tribalfs.stargazers.data.util.toFetchStatus
import com.tribalfs.stargazers.data.util.toSearchModeOnActionMode
import com.tribalfs.stargazers.data.util.toSearchModeOnBackBehavior
import dev.oneuiproject.oneui.layout.AppInfoLayout.Status
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import kotlin.coroutines.CoroutineContext

sealed class RefreshResult{
    object Updated : RefreshResult()
    object UpdateRunning : RefreshResult()
    object NetworkException : RefreshResult()
    data class OtherException(val exception: Throwable) : RefreshResult()
}

class StargazersRepo private constructor(
    context: Context,
    override val coroutineContext: CoroutineContext = Dispatchers.IO + SupervisorJob()
): CoroutineScope {

    private val appContext = context.applicationContext
    private val database = StargazersDB.getDatabase(appContext, this)

    private val prefDataStoreImpl = PreferenceDataStoreImpl.getInstance(appContext)
    private val dataStore = prefDataStoreImpl.dataStore

    val stargazersFlow: Flow<List<Stargazer>> = database.stargazerDao().getAllStargazers()

    private val refreshMutex = Mutex()

    suspend fun refreshStargazers(callback: ((result: RefreshResult) -> Unit)? = null) =
        withContext(Dispatchers.IO) {
            if (!refreshMutex.tryLock()) {
                callback?.invoke(RefreshResult.UpdateRunning)
                return@withContext
            }

            setOnStartFetchStatus()
            NetworkDataSource.fetchStargazers()
                .onSuccess {
                    database.stargazerDao().replaceAll(it)
                    updateLastRefresh(System.currentTimeMillis())
                    setOnFinishFetchStatus(true)
                    callback?.invoke(RefreshResult.Updated)
                }
                .onFailure {
                    setOnFinishFetchStatus(false)
                    when(it){
                        is HttpException -> callback?.invoke(RefreshResult.NetworkException)
                        else -> callback?.invoke(RefreshResult.OtherException(it))
                    }
                }

            refreshMutex.unlock()
        }

    val fetchStatusFlow: Flow<FetchState> = dataStore.data.map { it[PREF_INIT_FETCH_STATE].toFetchStatus() }

    // Use in-memory cache for local check;
    // we can't rely on asynchronous flow value
    private var fetchStatusCache: FetchState = NOT_INIT

    init {
        launch {
            fetchStatusCache = fetchStatusFlow.first()
        }
    }

    private fun setOnStartFetchStatus() {
        when (fetchStatusCache) {
            NOT_INIT, INIT_ERROR -> setFetchStatus(INITING)
            INITED, REFRESH_ERROR, REFRESHED -> setFetchStatus(REFRESHING)
            INITING, REFRESHING -> Unit
        }
    }

    private fun setOnFinishFetchStatus(isSuccess: Boolean) {
        when (fetchStatusCache) {
            REFRESHING -> setFetchStatus(if (isSuccess) REFRESHED else REFRESH_ERROR)
            INITING -> setFetchStatus(if (isSuccess) INITED else INIT_ERROR)
            else -> Unit//we're not expecting this
        }
    }

    fun setFetchStatus(state: FetchState) {
        fetchStatusCache = state
        prefDataStoreImpl.putInt(PREF_INIT_FETCH_STATE.name, state.ordinal)
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
            updateAvailable = it[PREF_UPDATE_AVAILABLE] ?: false,
            lockNavRailSearchMode = it[PREF_LOCK_DRAWER_NAV_RAIL_SEARCH_MODE] ?: false,
            lockNavRailActionMode = it[PREF_LOCK_DRAWER_NAV_RAIL_ACTION_MODE] ?: false
        )
    }

    fun setInitTupShown() = prefDataStoreImpl.putBoolean(PREF_INIT_TIP_SHOWN.name, true)

    suspend fun getUpdate(): Update = UpdateDataSource.getUpdate().also {
        prefDataStoreImpl.putBoolean(PREF_UPDATE_AVAILABLE.name, it.status == Status.UpdateAvailable)
    }

    suspend fun getStargazersById(ids: IntArray) = database.stargazerDao().getStargazersById(ids)

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
        val PREF_UPDATE_AVAILABLE = booleanPreferencesKey("updateAvailable1")
        private val PREF_INIT_TIP_SHOWN = booleanPreferencesKey("initTipShown")
        private val PREF_INIT_FETCH_STATE = intPreferencesKey("initFetch")
        private val PREF_LOCK_DRAWER_NAV_RAIL_ACTION_MODE = booleanPreferencesKey("lockDrawerNavRailActionMode")
        private val PREF_LOCK_DRAWER_NAV_RAIL_SEARCH_MODE = booleanPreferencesKey("lockDrawerNavRailSearchMode")

        private const val TAG = "StargazersRepo"
    }
}
