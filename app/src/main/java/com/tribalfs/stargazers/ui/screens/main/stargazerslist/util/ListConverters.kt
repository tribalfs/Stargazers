package com.tribalfs.stargazers.ui.screens.main.stargazerslist.util

import androidx.core.text.isDigitsOnly
import com.tribalfs.stargazers.data.model.Stargazer
import com.tribalfs.stargazers.ui.core.util.containsTokenOf
import com.tribalfs.stargazers.ui.screens.main.stargazerslist.model.StargazersListItemUiModel

fun List<StargazersListItemUiModel>.toStringsList(): List<String> {
    return map { model ->
        when(model){
            is StargazersListItemUiModel.GroupItem -> model.groupName
            is StargazersListItemUiModel.SeparatorItem -> model.indexText
            is StargazersListItemUiModel.StargazerItem -> model.stargazer.getDisplayName().run{
                if (!first().isLetterOrDigit()) "#" else this}
        }
    }
}

fun List<StargazersListItemUiModel>.toIndexCharsArray(): Array<String> {
    return filterIsInstance<StargazersListItemUiModel.SeparatorItem>()
        .map {
            it.indexText
        }
        .toTypedArray()
}


fun List<Stargazer>.toFilteredStargazerUiModelList(query: String, repoFilter: String): List<StargazersListItemUiModel> {
    val list = mutableListOf<StargazersListItemUiModel>()

    var previousChar: String? = null
    for (i in indices) {
        val item = this[i]
        val showItem = (repoFilter.isEmpty() || item.starredRepos.contains(repoFilter))
                && item.getSearchableString().containsTokenOf(query)
        if (showItem) {
            val char = item.getDisplayName()[0].toString().run { if (this.isDigitsOnly()) "#" else this.uppercase() }
            if (char != previousChar) {
                list.add(StargazersListItemUiModel.SeparatorItem(char))
                previousChar = char
            }
            list.add(StargazersListItemUiModel.StargazerItem(item))
        }
    }
    return list
}