package com.tribalfs.stargazers.data.model

import androidx.annotation.Keep

@Keep
data class Release(
    @JvmField
    val tag_name: String
)