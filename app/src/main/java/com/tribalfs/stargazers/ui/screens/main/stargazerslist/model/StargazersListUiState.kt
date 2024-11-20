package com.tribalfs.stargazers.ui.screens.main.stargazerslist.model

import com.tribalfs.stargazers.ui.screens.main.stargazerslist.StargazersListViewModel

data class StargazersListUiState(
    val itemsList: List<StargazersListItemUiModel> = emptyList(),
    val query: String = "",
    val noItemText: String = "No contacts",
    val loadState: StargazersListViewModel.LoadState = StargazersListViewModel.LoadState.LOADING
)