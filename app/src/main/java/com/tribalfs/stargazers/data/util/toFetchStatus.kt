package com.tribalfs.stargazers.data.util

import com.tribalfs.stargazers.data.model.FetchState

fun Int?.toFetchStatus(): FetchState = if (this != null){
        FetchState.entries[this]
    } else FetchState.NOT_INIT