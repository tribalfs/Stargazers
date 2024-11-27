package com.tribalfs.stargazers.ui.core.util

import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.preference.Preference
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import dev.oneuiproject.oneui.widget.CardItemView

fun ImageView.loadImageFromUrl(imageUrl: String){
    Glide.with(context)
        .load(imageUrl)
        .error(dev.oneuiproject.oneui.R.drawable.ic_oui_error_2)
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .circleCrop()
        .into(this)
}

fun Preference.loadImageFromUrl(imageUrl: String){
    Glide.with(this.context)
        .load(imageUrl)
        .circleCrop()
        .into(object : CustomTarget<Drawable>() {
            override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                this@loadImageFromUrl.icon = resource
            }

            override fun onLoadCleared(placeholder: Drawable?) {
            }
        })
}

fun CardItemView.loadImageFromUrl(imageUrl: String){
    Glide.with(this.context)
        .load(imageUrl)
        .circleCrop()
        .into(object : CustomTarget<Drawable>() {
            override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                this@loadImageFromUrl.icon = resource
            }

            override fun onLoadCleared(placeholder: Drawable?) {
            }
        })
}