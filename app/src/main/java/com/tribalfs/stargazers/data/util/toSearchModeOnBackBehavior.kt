package com.tribalfs.stargazers.data.util


import com.tribalfs.stargazers.data.model.SearchModeOnActionMode

fun String?.toSearchModeOnActionMode() =
        if (this == null) SearchModeOnActionMode.DISMISS else SearchModeOnActionMode.entries[toInt()]
