package com.tribalfs.stargazers.ui.screens.main.stargazerslist.model

import com.tribalfs.stargazers.data.model.Stargazer


sealed class StargazersListItemUiModel{
    data class GroupItem(val groupName: String) : StargazersListItemUiModel(){
        companion object{ const val VIEW_TYPE = -1 }
    }

    data class StargazerItem(val stargazer: Stargazer) : StargazersListItemUiModel(){
        companion object{ const val VIEW_TYPE = 0 }
    }
    data class SeparatorItem(val indexText: String) : StargazersListItemUiModel(){
        companion object{ const val VIEW_TYPE = 1 }
    }

    fun toStableId(): Long{
        return when(this){
            is GroupItem -> groupName.hashCode().toLong()
            is StargazerItem -> stargazer.id.toLong()
            is SeparatorItem -> indexText.hashCode().toLong()
        }
    }
}