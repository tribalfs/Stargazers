package com.tribalfs.stargazers.data.util

import dev.oneuiproject.oneui.layout.ToolbarLayout.SearchModeOnBackBehavior

fun String?.toSearchModeOnBackBehavior() =
        if (this == null) SearchModeOnBackBehavior.CLEAR_DISMISS else SearchModeOnBackBehavior.entries[toInt()]
