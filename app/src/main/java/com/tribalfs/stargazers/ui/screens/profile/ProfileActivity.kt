package com.tribalfs.stargazers.ui.screens.profile

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.transition.AutoTransition
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.IntentCompat
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import com.tribalfs.stargazers.R
import com.tribalfs.stargazers.data.model.Stargazer
import com.tribalfs.stargazers.databinding.ActivityStargazerBinding
import com.tribalfs.stargazers.ui.core.animation.ActivityBackAnimator
import com.tribalfs.stargazers.ui.core.util.SharingUtils.isSamsungQuickShareAvailable
import com.tribalfs.stargazers.ui.core.util.SharingUtils.share
import com.tribalfs.stargazers.ui.core.util.loadImageFromUrl
import com.tribalfs.stargazers.ui.core.util.onSingleClick
import com.tribalfs.stargazers.ui.core.util.openUrl
import com.tribalfs.stargazers.ui.core.util.semSetToolTipText
import com.tribalfs.stargazers.ui.core.util.toast
import dev.oneuiproject.oneui.utils.DeviceLayoutUtil.isTabletLayoutOrDesktop
import dev.oneuiproject.oneui.widget.CardItemView
import dev.oneuiproject.oneui.R as designR


class ProfileActivity : AppCompatActivity(){

    companion object{
        private const val TAG = "ProfileActivity"
        const val KEY_STARGAZER = "key_stargazer"
        const val KEY_TRANSITION_CONTAINER = "key_transition_name"
        const val KEY_TRANSITION_AVATAR = "key_transition_name_avatar"
        const val KEY_TRANSITION_NAME = "key_transition_name_sgname"
    }

    private lateinit var mBinding: ActivityStargazerBinding
    private lateinit var stargazer: Stargazer

    override fun onCreate(savedInstanceState: Bundle?) {
        setupSharedElementTransitionWindow()
        super.onCreate(savedInstanceState)
        postponeEnterTransition()

        mBinding = ActivityStargazerBinding.inflate(layoutInflater)
        setupSharedElementTransitionView()

        mBinding.toolbarLayout.apply {
            setNavigationButtonAsBack()
            toolbar.fadeInIfSupported()
        }
        setContentView(mBinding.root)

        initContent()

        setupBottomNav()

    }

    override fun onStart() {
        super.onStart()
        if (isSupportBackTransition) {
            ActivityBackAnimator.init(this, mBinding.root).apply {
                onBackInvoked = {
                    mBinding.toolbarLayout.toolbar.alpha = 0f
                    mBinding.bottomNav.alpha = 0f
                    mBinding.stargazerDetailsContainer.alpha = 0f
                }
            }
        }
    }

    private fun initContent() {
        stargazer = IntentCompat.getParcelableExtra(intent, KEY_STARGAZER, Stargazer::class.java)!!
        Log.d(TAG, "Stargazer: $stargazer")

        with (stargazer) {
            mBinding.stargazerAvatar.loadImageFromUrl(avatar_url)
            mBinding.stargazerName.text = getDisplayName()
            mBinding.stargazerGithubUrl.text = html_url

            mBinding.stargazerButtons.stargazerGithubBtn.apply {
                onSingleClick { openUrl(html_url) }
                semSetToolTipText(html_url)
            }

            email?.let {e ->
                mBinding.stargazerButtons.stargazerEmailBtn.apply {
                    isVisible = true
                    onSingleClick { toast("Todo") }
                    semSetToolTipText(e)
                }
            }

            twitter_username?.let {x ->
                mBinding.stargazerButtons.stargazerTwitterBtn.apply {
                    isVisible = true
                    onSingleClick { openUrl("https://x.com/$x") }
                    semSetToolTipText(x)
                }
            }

            stargazer.blog?.let {b ->
                if (b.isEmpty()) return@let
                mBinding.stargazerButtons.stargazerBlog.apply {
                    isVisible = true
                    onSingleClick { openUrl(b) }
                    semSetToolTipText(b)
                }
            }

            setupStargazerDetails()
        }
    }

    private fun setupStargazerDetails(){
        mBinding.stargazerDetailsContainer.fadeInIfSupported()
        with (stargazer) {
            val cardDetailsMap = mapOf(
                location to designR.drawable.ic_oui_location,
                company to designR.drawable.ic_oui_work,
                email to designR.drawable.ic_oui_email,
                bio to designR.drawable.ic_oui_tag
            )

            var added = 0
            for (i in cardDetailsMap) {
                if (i.key.isNullOrEmpty()) continue
                addCardItemView(
                    icon = AppCompatResources.getDrawable(this@ProfileActivity, i.value)!!,
                    title = i.key!!,
                    showTopDivider = added > 0
                )
                added += 1
            }
        }
    }

    private fun addCardItemView(
        icon: Drawable,
        title: String,
        showTopDivider: Boolean
    ) {
        mBinding.stargazerDetailsContainer.addView(
            CardItemView(this@ProfileActivity).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                this.icon = icon
                this.title = title
                this.showTopDivider = showTopDivider
            }
        )
    }

    /**
     * Call in onCreate but before calling super.onCreate()
     */
    private fun setupSharedElementTransitionWindow(){
        window.apply {
            requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
            setSharedElementsUseOverlay(false)
            AutoTransition().let {
                it.duration = 250
                sharedElementExitTransition = it
                sharedElementEnterTransition = it
                sharedElementReturnTransition = it
                sharedElementReenterTransition = it
            }
        }
    }

    private fun setupSharedElementTransitionView(){
        mBinding.stargazerAvatar.transitionName = intent.getStringExtra(KEY_TRANSITION_AVATAR)
        mBinding.headerContainer.transitionName = intent.getStringExtra(KEY_TRANSITION_CONTAINER)
        //mBinding.stargazerName.transitionName = intent.getStringExtra(KEY_TRANSITION_NAME)
        mBinding.root.apply {
            isTransitionGroup = true
            doOnPreDraw { startPostponedEnterTransition() }
        }
    }

    private fun setupBottomNav(){
        mBinding.bottomNav.menu.findItem(R.id.menu_sg_share).apply {
            title = if (isSamsungQuickShareAvailable()) "Quick share" else "Share"
        }

        mBinding.bottomNav.setOnItemSelectedListener {
            when(it.itemId){
                R.id.menu_sg_share -> {
                    val shareText = "Check out this amazing stargazer's profile:" +
                            "\n${stargazer.html_url}"
                    shareText.share(this)
                    true
                }
                R.id.menu_sg_qrcode -> {
                    QRBottomSheet.newInstance(stargazer).show(supportFragmentManager, null)
                    true
                }
                else -> false
            }
        }
        mBinding.bottomNav.fadeInIfSupported()
    }

    private fun View.fadeInIfSupported() = apply {
        if (!isSupportBackTransition) return@apply
        alpha = 0f
        animate()
            .alpha(1f)
            .setStartDelay(150)
            .duration = 250
    }

    private val isSupportBackTransition get() = !isTabletLayoutOrDesktop(this)
}