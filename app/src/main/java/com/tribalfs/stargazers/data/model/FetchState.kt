package com.tribalfs.stargazers.data.model

enum class FetchState{
    NOT_INIT,
    INITING,
    INIT_ERROR,
    INITED,
    REFRESHING,
    REFRESH_ERROR,
    REFRESHED
}

