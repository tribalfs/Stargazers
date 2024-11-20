package com.tribalfs.stargazers.ui.core.util

import android.app.Activity

val Activity.isMultiWindowModeCompat: Boolean
    get() = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N &&
                isInMultiWindowMode
