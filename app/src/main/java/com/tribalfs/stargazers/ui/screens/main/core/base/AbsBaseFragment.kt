package com.tribalfs.stargazers.ui.screens.main.core.base

import android.os.Bundle
import android.view.animation.PathInterpolator
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import com.google.android.material.transition.MaterialSharedAxis


abstract class AbsBaseFragment : Fragment() {

    @CallSuper
    @Override
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupFragmentTransitions()
    }

    private fun setupFragmentTransitions() {
        val interpolator = PathInterpolator(0.1f, 0.1f, 0f, 1f)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Y, true).apply {
            this.interpolator = interpolator
        }
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Y, false).apply {
            this.interpolator = interpolator
        }
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Y, false).apply {
            this.interpolator = interpolator
        }
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.Y, true).apply {
            this.interpolator = interpolator
        }
    }
}
