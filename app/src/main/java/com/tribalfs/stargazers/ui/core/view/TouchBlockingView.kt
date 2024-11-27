package com.tribalfs.stargazers.ui.core.view

import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewGroup

class TouchBlockingView(context: Context): View(context) {
    init {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        isClickable = true
        isFocusable = true
        setBackgroundColor(Color.parseColor("#80000000"))
    }
}