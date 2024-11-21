package com.tribalfs.stargazers.ui.screens.customabout

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.TooltipCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.animation.PathInterpolatorCompat
import androidx.core.view.updateLayoutParams
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener
import com.tribalfs.stargazers.BuildConfig
import com.tribalfs.stargazers.R
import com.tribalfs.stargazers.databinding.ActivityAboutAppCustomBinding
import com.tribalfs.stargazers.ui.core.util.openApplicationSettings
import com.tribalfs.stargazers.ui.core.util.openUrl
import dev.oneuiproject.oneui.ktx.invokeOnBack
import dev.oneuiproject.oneui.ktx.isInMultiWindowModeCompat
import dev.oneuiproject.oneui.utils.DeviceLayoutUtil.isPortrait
import dev.oneuiproject.oneui.utils.internal.ToolbarLayoutUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.math.abs

class CustomAboutActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var mBinding: ActivityAboutAppCustomBinding
    private val mAppBarListener = AboutAppBarListener()
    private var mContentEnabled = false
    private val progressInterpolator =   PathInterpolatorCompat.create(0f, 0f, 0f, 1f)
    private val callbackIsActiveState = MutableStateFlow(false)
    private var isBackProgressing = false
    private var isExpanding = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityAboutAppCustomBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        applyInsetIfNeeded()
        setupToolbar()

        initContent()
        refreshAppBar(resources.configuration)
        initOnBackPressed()
    }

    private fun applyInsetIfNeeded() {
        if (Build.VERSION.SDK_INT >= 30 && !window.decorView.fitsSystemWindows) {
            mBinding.root.setOnApplyWindowInsetsListener { _, insets ->
                val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                mBinding.root.setPadding(
                    systemBarsInsets.left, systemBarsInsets.top,
                    systemBarsInsets.right, systemBarsInsets.bottom
                )
                insets
            }
        }
    }

    private fun setupToolbar(){
        setSupportActionBar(mBinding.aboutToolbar)
        //Should be called after setSupportActionBar
        mBinding.aboutToolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
        supportActionBar!!.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
        }
    }


    private fun initOnBackPressed() {
        invokeOnBack(
            triggerStateFlow = callbackIsActiveState,
            onBackPressed = {
                mBinding.aboutAppBar.setExpanded(true)
                isBackProgressing = false
                isExpanding = false
            },
            onBackStarted = {
                isBackProgressing = true
            },
            onBackProgressed = {
                val interpolatedProgress = progressInterpolator.getInterpolation(it.progress)
                if (interpolatedProgress > .4 && !isExpanding){
                    isExpanding = true
                    mBinding.aboutAppBar.setExpanded(true, true)
                }else if (interpolatedProgress < .10 && isExpanding){
                    isExpanding = false
                    mBinding.aboutAppBar.setExpanded(false, true)
                }
            },
            onBackCancelled = {
                mBinding.aboutAppBar.setExpanded(false)
                isBackProgressing = false
                isExpanding = false
            }
        )
        updateCallbackState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        refreshAppBar(newConfig)
        updateCallbackState()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_custom_about, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_app_info) {
            openApplicationSettings()
            return true
        }
        return false
    }


    @SuppressLint("RestrictedApi")
    private fun refreshAppBar(config: Configuration) {
        ToolbarLayoutUtils.hideStatusBarForLandscape(this, config.orientation)
        ToolbarLayoutUtils.updateListBothSideMargin(
            this,
            mBinding.aboutBottomContainer
        )
        if (config.orientation != Configuration.ORIENTATION_LANDSCAPE
            && !isInMultiWindowModeCompat
        ) {
            mBinding.aboutAppBar.apply {
                seslSetCustomHeightProportion(true, 0.5f)//expanded
                addOnOffsetChangedListener(mAppBarListener)
                setExpanded(true, false)
            }
            mBinding.aboutSwipeUpContainer.apply {
                updateLayoutParams { height = resources.displayMetrics.heightPixels / 2 }
                visibility = View.VISIBLE
            }
        } else {
            mBinding.aboutAppBar.apply {
                setExpanded(false, false)
                seslSetCustomHeightProportion(true, 0f)
                removeOnOffsetChangedListener(mAppBarListener)
            }
            mBinding.aboutBottomContainer.alpha = 1f
            mBinding.aboutSwipeUpContainer.visibility = View.GONE
            setBottomContentEnabled(true)
        }
    }

    private fun initContent() {
        val appIcon = getDrawable(R.mipmap.ic_launcher)
        mBinding.aboutHeaderAppIcon.setImageDrawable(appIcon)
        mBinding.aboutBottomAppIcon.setImageDrawable(appIcon)
        mBinding.aboutHeaderAppVersion.text = "Version ${BuildConfig.VERSION_NAME}"
        mBinding.aboutBottomAppVersion.text = "Version ${BuildConfig.VERSION_NAME}"

        mBinding.aboutHeaderGithub.setOnClickListener(this)
        TooltipCompat.setTooltipText(mBinding.aboutHeaderGithub, "GitHub")

        mBinding.aboutHeaderTelegram.setOnClickListener(this)
        TooltipCompat.setTooltipText(mBinding.aboutHeaderTelegram, "Telegram")

        with(mBinding.aboutBottomContent) {
            aboutBottomDevYann.setOnClickListener(this@CustomAboutActivity)
            aboutBottomDevMesa.setOnClickListener(this@CustomAboutActivity)
            aboutBottomOssApache.setOnClickListener(this@CustomAboutActivity)
            aboutBottomOssMit.setOnClickListener(this@CustomAboutActivity)
            aboutBottomRelativeJetpack.setOnClickListener(this@CustomAboutActivity)
            aboutBottomRelativeMaterial.setOnClickListener(this@CustomAboutActivity)
            aboutBottomDevTribalfs.setOnClickListener(this@CustomAboutActivity)
        }
    }

    private fun setBottomContentEnabled(enabled: Boolean) {
        if (mContentEnabled == enabled) return
        mContentEnabled = enabled
        mBinding.aboutHeaderGithub.isEnabled = !enabled
        mBinding.aboutHeaderTelegram.isEnabled = !enabled
        with(mBinding.aboutBottomContent) {
            aboutBottomDevYann.isEnabled = enabled
            aboutBottomDevMesa.isEnabled = enabled
            aboutBottomOssApache.isEnabled = enabled
            aboutBottomOssMit.isEnabled = enabled
            aboutBottomRelativeJetpack.isEnabled = enabled
            aboutBottomRelativeMaterial.isEnabled = enabled
            aboutBottomDevTribalfs.isEnabled = enabled
        }
    }

    override fun onClick(v: View) {
        with(mBinding.aboutBottomContent) {
            when (v.id) {
                mBinding.aboutHeaderGithub.id -> {
                    openUrl("https://github.com/tribalfs/oneui-design")
                }

                mBinding.aboutHeaderTelegram.id -> {
                    openUrl("https://t.me/oneuiproject")
                }

                aboutBottomDevTribalfs.id -> {
                    openUrl("https://github.com/tribalfs")
                }

                aboutBottomDevYann.id -> {
                    openUrl("https://github.com/Yanndroid")
                }

                aboutBottomDevMesa.id -> {
                    openUrl("https://github.com/salvogiangri")
                }

                aboutBottomOssApache.id -> {
                    openUrl("https://www.apache.org/licenses/LICENSE-2.0.txt")
                }

                aboutBottomOssMit.id -> {
                    openUrl("https://raw.githubusercontent.com/tribalfs/oneui-design/refs/heads/sample_setup_sesl6/LICENSE")
                }

                aboutBottomRelativeMaterial.id -> {
                    openUrl("https://github.com/tribalfs/sesl-material-components-android")
                }

                aboutBottomRelativeJetpack.id -> {
                    openUrl("https://github.com/tribalfs/sesl-androidx")
                }
            }
        }
    }


    private inner class AboutAppBarListener : OnOffsetChangedListener {
        override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
            // Handle the SwipeUp anim view
            val totalScrollRange = appBarLayout.totalScrollRange
            val abs = abs(verticalOffset)
            if (abs >= totalScrollRange / 2) {
                mBinding.aboutSwipeUpContainer.alpha = 0f
                setBottomContentEnabled(true)
            } else if (abs == 0) {
                mBinding.aboutSwipeUpContainer.alpha = 1f
                setBottomContentEnabled(false)
            } else {
                val offsetAlpha = appBarLayout.y / totalScrollRange
                mBinding.aboutSwipeUpContainer.alpha = (1 - offsetAlpha * -3).coerceIn(0f, 1f)
            }

            // Handle the bottom part of the UI
            val alphaRange = mBinding.aboutCtl.height * 0.143f
            val layoutPosition = abs(appBarLayout.top).toFloat()
            val bottomAlpha = (150.0f / alphaRange * (layoutPosition - mBinding.aboutCtl.height * 0.35f)).coerceIn(0f, 255f)

            mBinding.aboutBottomContainer.alpha = bottomAlpha / 255

            updateCallbackState(appBarLayout.getTotalScrollRange() + verticalOffset == 0)
        }
    }

    @SuppressLint("RestrictedApi")
    private fun updateCallbackState(enable: Boolean ? = null) {
        if (isBackProgressing) return
        callbackIsActiveState.value = (enable
                ?: (mBinding.aboutAppBar.seslIsCollapsed()
                && isPortrait(resources.configuration)
                && !isInMultiWindowModeCompat)).also {
                    Log.d("CustomAboutActivity", "updateCallbackState: $it")
        }
    }

}