package com.tribalfs.stargazers.ui.screens.profile

import android.app.Dialog
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.BundleCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.tribalfs.stargazers.data.model.Stargazer
import com.tribalfs.stargazers.databinding.ActivityStargazerQrBinding
import com.tribalfs.stargazers.ui.core.util.toast
import com.tribalfs.stargazers.ui.screens.profile.ProfileActivity.Companion.KEY_STARGAZER

class QRBottomSheet : BottomSheetDialogFragment() {

    private  var _binding: ActivityStargazerQrBinding? = null
    private val binding get() = _binding!!

    companion object {
        fun newInstance(stargazer: Stargazer): QRBottomSheet {
            return QRBottomSheet().apply{
                arguments = Bundle().apply {
                    putParcelable(KEY_STARGAZER, stargazer)
                }
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return (super.onCreateDialog(savedInstanceState) as BottomSheetDialog).apply {
            behavior.skipCollapsed = true
            setOnShowListener {
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ActivityStargazerQrBinding.inflate(inflater, container, false).also {
        _binding = it
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val stargazer = BundleCompat.getParcelable(
            requireArguments(),
            KEY_STARGAZER,
            Stargazer::class.java
        )!!
        binding.sgName.text = stargazer.getDisplayName()

        Glide.with(this)
            .load(stargazer.avatar_url)
            .circleCrop()
            .into(object : CustomTarget<Drawable>() {
                override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                    binding.qrCode.apply {
                        setContent(stargazer.html_url)
                        setIcon(resource)
                        invalidate()
                    }
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
            })

        binding.quickShareBtn.setOnClickListener {
            toast("Todo(Quick Share)")
        }

        binding.saveBtn.setOnClickListener {
            toast("Todo(Save image)")
        }
    }

}