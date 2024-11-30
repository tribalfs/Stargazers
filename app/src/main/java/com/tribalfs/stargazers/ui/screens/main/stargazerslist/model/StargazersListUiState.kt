package com.tribalfs.stargazers.ui.screens.main.stargazerslist.model

import com.tribalfs.stargazers.data.model.FetchState

data class StargazersListUiState(
    val itemsList: List<StargazersListItemUiModel> = emptyList(),
    val query: String = "",
    val noItemText: String = "No contacts",
    val fetchStatus: FetchState = FetchState.INITED
)