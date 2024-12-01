package com.tribalfs.stargazers.ui.screens.main.stargazerslist

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tribalfs.stargazers.app.StargazersApp
import com.tribalfs.stargazers.data.RefreshResult
import com.tribalfs.stargazers.data.StargazersRepo
import com.tribalfs.stargazers.data.model.FetchState
import com.tribalfs.stargazers.data.model.SearchModeOnActionMode
import com.tribalfs.stargazers.data.model.StargazersSettings
import com.tribalfs.stargazers.ui.core.util.isOnline
import com.tribalfs.stargazers.ui.screens.main.stargazerslist.model.StargazersListUiState
import com.tribalfs.stargazers.ui.screens.main.stargazerslist.util.toFilteredStargazerUiModelList
import dev.oneuiproject.oneui.delegates.AllSelectorState
import dev.oneuiproject.oneui.layout.ToolbarLayout
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.Lazily
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class StargazersListViewModel (
    private val stargazersRepo: StargazersRepo,
    app: StargazersApp): AndroidViewModel(app) {

    private val _queryStateFlow = MutableStateFlow("")
    private val _repoFilterStateFlow = MutableStateFlow("")

    val stargazerSettingsStateFlow = stargazersRepo.stargazersSettingsFlow
        .stateIn(viewModelScope, Lazily, StargazersSettings())

    fun getSearchModeOnBackBehavior(): ToolbarLayout.SearchModeOnBackBehavior
            = stargazerSettingsStateFlow.value.searchModeBackBehavior

    fun getKeepSearchModeOnActionMode(): Boolean
            = stargazerSettingsStateFlow.value.searchOnActionMode == SearchModeOnActionMode.RESUME

    suspend fun getStargazersById(ids: IntArray) = stargazersRepo.getStargazersById(ids)

    private val _stargazersListScreenStateFlow = MutableStateFlow(StargazersListUiState())
    val stargazersListScreenStateFlow = _stargazersListScreenStateFlow.asStateFlow()

    private val _userMessage: MutableStateFlow<String?> = MutableStateFlow(null)
    val userMessage: StateFlow<String?> = _userMessage
    fun setUserMessageShown() = _userMessage.update { null }

    init {
        viewModelScope.launch {
            launch {
                combine(
                    stargazersRepo.stargazersFlow,
                    _queryStateFlow,
                    _repoFilterStateFlow,
                    stargazersRepo.fetchStatusFlow
                ) { stargazers, query, repoFilter, fetchStatus ->
                    val itemsList = stargazers.toFilteredStargazerUiModelList(query, repoFilter)
                    val noItemText = getNoItemText(fetchStatus, query)
                    StargazersListUiState(
                        itemsList = itemsList,
                        query = query,
                        noItemText = noItemText,
                        fetchStatus = fetchStatus
                    )
                }.collectLatest { uiState ->
                    _stargazersListScreenStateFlow.value = uiState
                }
            }
        }
    }

    private fun getNoItemText(fetchState: FetchState, query: String): String{
        return when (fetchState) {
            FetchState.INITING -> "Loading stargazers..."
            FetchState.INIT_ERROR -> "Error loading stargazers."
            //These will only be visible when rv is empty.
            FetchState.INITED, FetchState.REFRESHED -> if (query.isEmpty()) "No stargazers yet." else "No results found."
            FetchState.NOT_INIT, FetchState.REFRESHING, FetchState.REFRESH_ERROR -> ""
        }
    }

    fun isIndexScrollEnabled() = stargazerSettingsStateFlow.value.enableIndexScroll

    fun refreshStargazers(notifyResult: Boolean = true) = viewModelScope.launch {
        if (!isOnline(getApplication())) {
            _userMessage.update { "No internet connection detected." }
            return@launch
        }

        stargazersRepo.refreshStargazers sr@{ result ->
            if (!notifyResult) return@sr
            when (result){
                RefreshResult.UpdateRunning -> {
                    _userMessage.update { "Stargazer's already refreshing." }
                }
                is RefreshResult.OtherException -> {
                    _userMessage.update {  result.exception.message ?: "Error fetching stargazers." }
                }
                RefreshResult.Updated -> {
                    _userMessage.update {  "Latest stargazers data successfully fetched!" }
                }
                RefreshResult.NetworkException -> {
                    _userMessage.update {  "Connection error occurred!" }
                }
            }
        }

    }

    private var setFilterJob: Job? = null
    fun setQuery(query: String?) = viewModelScope.launch {
        setFilterJob?.cancel()
        setFilterJob = launch {
            delay(200)
            _queryStateFlow.value = query ?: ""
        }
    }

    fun setRepoFilter(repoName: String)  { _repoFilterStateFlow.value = repoName }

    val allSelectorStateFlow= MutableStateFlow(AllSelectorState())

    fun setInitTipShown() = stargazersRepo.setInitTupShown()

    companion object{
        private const val TAG = "StargazersListViewModel"
    }
}

class StargazersListViewModelFactory(private val stargazersRepo: StargazersRepo,
                                     private val  app: StargazersApp) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StargazersListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StargazersListViewModel(stargazersRepo, app) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
