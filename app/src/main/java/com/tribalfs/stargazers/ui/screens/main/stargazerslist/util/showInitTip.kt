package com.tribalfs.stargazers.ui.screens.main.stargazerslist.util

import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.tribalfs.stargazers.ui.core.view.TouchBlockingView
import dev.oneuiproject.oneui.widget.TipPopup
import dev.oneuiproject.oneui.widget.TipPopup.Direction

fun Fragment.showInitTip(anchor: View, message: String,
                                 action: () -> Unit) {
        val rootView = requireActivity().window.decorView.rootView as ViewGroup
        val blockingView = TouchBlockingView(requireContext())
        rootView.addView(blockingView)

        TipPopup(anchor, TipPopup.Mode.TRANSLUCENT).apply {
            setMessage(message)
            setExpanded(true)
            setOutsideTouchEnabled(false)
            setAction("Ok") {
                rootView.removeView(blockingView)
                action()
            }
            show(Direction.DEFAULT)
        }
    }
