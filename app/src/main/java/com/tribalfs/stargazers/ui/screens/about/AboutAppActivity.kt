package com.tribalfs.stargazers.ui.screens.about

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.tribalfs.stargazers.BuildConfig
import com.tribalfs.stargazers.R
import com.tribalfs.stargazers.data.StargazersRepo
import com.tribalfs.stargazers.ui.core.util.isOnline
import com.tribalfs.stargazers.ui.core.util.openUrl
import dev.oneuiproject.oneui.layout.AppInfoLayout
import dev.oneuiproject.oneui.layout.AppInfoLayout.Status
import kotlinx.coroutines.launch

class AboutAppActivity : AppCompatActivity() {
    private lateinit var appInfoLayout: AppInfoLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_app)

        appInfoLayout = findViewById<AppInfoLayout?>(R.id.appInfoLayout).apply {
            addOptionalText("OneUI Design version " + BuildConfig.ONEUI_DESIGN_VERSION)
            setMainButtonClickListener(object : AppInfoLayout.OnClickListener {
                override fun onUpdateClicked(v: View) {
                    this@AboutAppActivity
                        .openUrl("https://github.com/tribalfs/Stargazers/raw/master/app/release/app-release.apk")
                }

                override fun onRetryClicked(v: View) {
                    fetchLatestRelease()
                }
            })
        }
        fetchLatestRelease()
    }

    private fun fetchLatestRelease() {
        appInfoLayout.updateStatus = Status.Loading

        if (!isOnline(applicationContext)) {
            appInfoLayout.updateStatus = Status.NoConnection
        } else {
            lifecycleScope.launch {
                StargazersRepo.getInstance(applicationContext).getUpdate().status.let {
                    appInfoLayout.updateStatus = it
                }
            }
        }
    }

    fun openGitHubPage(v: View?) {
        this.openUrl("https://github.com/tribalfs/Stargazers")
    }

    fun openOSL(v: View?) {
        this.openUrl("https://raw.githubusercontent.com/tribalfs/Stargazers/refs/heads/master/app/OpenSourceLicenses")
    }
}