@file:Suppress("NOTHING_TO_INLINE")

package com.tribalfs.stargazers.ui.core.util

import android.view.View
import androidx.appcompat.widget.TooltipCompat

/**
 * Sets OneUI style tooltip text to this View.
 */
inline fun View.semSetToolTipText(toolTipText: CharSequence?) {
    TooltipCompat.setTooltipText(this, toolTipText)
}
