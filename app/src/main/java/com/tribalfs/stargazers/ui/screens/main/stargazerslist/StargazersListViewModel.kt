package com.tribalfs.stargazers.ui.screens.main.stargazerslist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tribalfs.stargazers.data.StargazersRepo
import com.tribalfs.stargazers.data.model.SearchModeOnActionMode
import com.tribalfs.stargazers.data.model.StargazersSettings
import com.tribalfs.stargazers.ui.screens.main.stargazerslist.model.StargazersListUiState
import com.tribalfs.stargazers.ui.screens.main.stargazerslist.util.toFilteredStargazerUiModelList
import dev.oneuiproject.oneui.delegates.AllSelectorState
import dev.oneuiproject.oneui.layout.ToolbarLayout
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.Lazily
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


class StargazersListViewModel (
    private val stargazersRepo: StargazersRepo
): ViewModel() {

    enum class LoadState{
        LOADING,
        REFRESHING,
        LOADED,
        ERROR
    }

    private val _queryStateFlow = MutableStateFlow("")
    private val _repoFilterStateFlow = MutableStateFlow("")
    private val _loadStateFlow = MutableStateFlow(LoadState.LOADED)

    val stargazerSettingsStateFlow = stargazersRepo.stargazersSettingsFlow
        .stateIn(viewModelScope, Lazily, StargazersSettings())

    fun getSearchModeOnBackBehavior(): ToolbarLayout.SearchModeOnBackBehavior
            = stargazerSettingsStateFlow.value.searchModeBackBehavior

    fun getKeepSearchModeOnActionMode(): Boolean
            = stargazerSettingsStateFlow.value.searchOnActionMode == SearchModeOnActionMode.RESUME

    private val _stargazersListScreenStateFlow = MutableStateFlow(StargazersListUiState())
    val stargazersListScreenStateFlow = _stargazersListScreenStateFlow.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                stargazersRepo.stargazersFlow,
                _queryStateFlow,
                _repoFilterStateFlow,
                _loadStateFlow
            ) { stargazers, query, repoFilter, loadState ->
                if (stargazers.isEmpty()) {
                    refreshStargazers(true)
                }
                val itemsList = stargazers.toFilteredStargazerUiModelList(query, repoFilter)
                val noItemText = getNoItemText(loadState, query)
                StargazersListUiState(
                    itemsList = itemsList,
                    query = query,
                    noItemText = noItemText,
                    loadState = loadState
                )
            }.collectLatest { uiState ->
                _stargazersListScreenStateFlow.value = uiState
            }

        }
    }

    private fun getNoItemText(loadState: LoadState,  query: String): String{
        return when (loadState) {
            LoadState.LOADING -> "Loading stargazers..."
            LoadState.REFRESHING -> ""
            LoadState.LOADED -> if (query.isEmpty()) "No stargazers yet." else "No results found."
            LoadState.ERROR -> "Error loading stargazers."
        }
    }

    fun isIndexScrollEnabled(): Boolean = stargazerSettingsStateFlow.value.enableIndexScroll

    fun refreshStargazers(isRetryLoad: Boolean = false) = viewModelScope.launch {
        var isFinishRefresh = false

        if (isRetryLoad) {
            _loadStateFlow.value = LoadState.LOADING
        }

        stargazersRepo.refreshStargazers {success ->
            isFinishRefresh = true
            _loadStateFlow.value = if (success) LoadState.LOADED else LoadState.ERROR
        }

        if (!isRetryLoad){
            delay(SWITCH_TO_HPB_DELAY)
            //We will switch to less intrusive horizontal progress bar
            if (!isFinishRefresh){
                _loadStateFlow.value = LoadState.REFRESHING
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

    fun setRepoFilter(repoName: String)  {
        _repoFilterStateFlow.value = repoName
    }

    val allSelectorStateFlow: MutableStateFlow <AllSelectorState> = MutableStateFlow(AllSelectorState())

    fun setInitTipShown(){
        stargazersRepo.setInitTupShown()
    }

    companion object{
        private const val TAG = "StargazersListViewModel"
        const val SWITCH_TO_HPB_DELAY = 1_500L
    }
}

class StargazersListViewModelFactory(private val stargazersRepo: StargazersRepo) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StargazersListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StargazersListViewModel(stargazersRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
