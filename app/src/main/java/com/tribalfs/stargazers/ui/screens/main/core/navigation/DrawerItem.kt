package com.tribalfs.stargazers.ui.screens.main.core.navigation

import android.view.View

sealed class DrawerItem {
    data class DestinationItem(val id: Int, val title: String, val iconResId: Int) : DrawerItem(){
        companion object{
            const val VIEW_TYPE = 1
        }
    }

    data object DividerItem : DrawerItem(){
        const val VIEW_TYPE = 0
    }

    data class Button(val onClick: View.OnClickListener) : DrawerItem(){
        companion object{
            const val VIEW_TYPE =2
        }
    }

    companion object{
        val DrawerItem.VIEW_TYPE: Int
            get() {
                return when (this) {
                    is DestinationItem -> DestinationItem.VIEW_TYPE
                    is DividerItem -> VIEW_TYPE
                    is Button -> Button.VIEW_TYPE
                }
            }

    }
}
