package com.tribalfs.stargazers.data.model

import android.graphics.Color
import android.os.Parcelable
import androidx.annotation.ColorInt
import dev.oneuiproject.oneui.layout.ToolbarLayout.SearchModeOnBackBehavior
import kotlinx.parcelize.Parcelize

@Parcelize
data class StargazersSettings(
    val isTextModeIndexScroll: Boolean = false,
    val autoHideIndexScroll: Boolean = true,
    val searchOnActionMode: SearchModeOnActionMode = SearchModeOnActionMode.DISMISS,
    val searchModeBackBehavior: SearchModeOnBackBehavior = SearchModeOnBackBehavior.CLEAR_DISMISS,
    @ColorInt
    val searchHighlightColor: Int = Color.parseColor("#2196F3"),
    val darkModeOption: DarkMode = DarkMode.AUTO,
    val enableIndexScroll: Boolean = true,
    val lastRefresh: Long = 0L,
    val initTipShown: Boolean = true,
    val updateAvailable: Boolean = false
) : Parcelable
