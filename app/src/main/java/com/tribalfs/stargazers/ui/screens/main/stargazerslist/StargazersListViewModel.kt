package com.tribalfs.stargazers.ui.screens.main.stargazerslist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.oneuiproject.oneui.delegates.AllSelectorState
import dev.oneuiproject.oneui.layout.ToolbarLayout
import com.tribalfs.stargazers.data.SearchModeOnActionMode
import com.tribalfs.stargazers.data.StargazersRepo
import com.tribalfs.stargazers.data.StargazersSettings
import com.tribalfs.stargazers.ui.screens.main.stargazerslist.model.StargazersListUiState
import com.tribalfs.stargazers.ui.screens.main.stargazerslist.util.toFilteredStargazerUiModelList
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.Lazily
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
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
    private val _loadStateFlow = MutableStateFlow(LoadState.LOADING)

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
                stargazersRepo.stargazersFlow, _queryStateFlow, _loadStateFlow
            ) { stargazersResult, query, loadState ->
                StargazersListUiState(
                    itemsList = stargazersResult.toFilteredStargazerUiModelList(query),
                    query = query,
                    noItemText = when(loadState){
                        LoadState.LOADING -> "Loading stargazers..."
                        LoadState.REFRESHING -> ""
                        LoadState.LOADED -> if (query.isEmpty()) "No stargazers yet." else "No results found."
                        LoadState.ERROR -> "Error loading stargazers."
                    },
                    loadState = loadState
                )
            }.collectLatest {
                _stargazersListScreenStateFlow.value = it
            }
        }

        refreshStargazers(true)
    }

    fun isIndexScrollEnabled(): Boolean = stargazerSettingsStateFlow.value.enableIndexScroll

    fun refreshStargazers(isFirstOrRetryLoad: Boolean = false) = viewModelScope.launch {
        var isRefreshCompleted = false
        launch {
            stargazersRepo.refreshStargazers {success ->
                isRefreshCompleted = true
                _loadStateFlow.update { if (success) LoadState.LOADED else LoadState.ERROR }
            }
        }
        if (isFirstOrRetryLoad){
            if (!isRefreshCompleted) {
                _loadStateFlow.update { LoadState.LOADING }
            }
        }else {
            delay(SWITCH_TO_HPB_DELAY)
            if (!isRefreshCompleted) {
                //We will switch to less intrusive horizontal progress bar
                _loadStateFlow.update { LoadState.REFRESHING }
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

    val allSelectorStateFlow: MutableStateFlow <AllSelectorState> = MutableStateFlow(AllSelectorState())

    companion object{
        private const val TAG = "ContactsViewModel"
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
