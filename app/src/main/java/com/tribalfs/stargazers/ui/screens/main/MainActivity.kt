package com.tribalfs.stargazers.ui.screens.main

import android.content.Intent
import android.os.Bundle
import android.util.LayoutDirection
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.tribalfs.stargazers.R
import com.tribalfs.stargazers.data.StargazersRepo
import com.tribalfs.stargazers.databinding.ActivityMainBinding
import com.tribalfs.stargazers.ui.core.util.launchAndRepeatWithLifecycle
import com.tribalfs.stargazers.ui.screens.customabout.CustomAboutActivity
import com.tribalfs.stargazers.ui.screens.main.core.navigation.AppNavigation
import com.tribalfs.stargazers.ui.screens.main.core.navigation.MainNavigationDelegate
import com.tribalfs.stargazers.ui.screens.settings.SettingsActivity
import dev.oneuiproject.oneui.ktx.setBadge
import dev.oneuiproject.oneui.layout.Badge
import dev.oneuiproject.oneui.layout.NavDrawerLayout
import dev.oneuiproject.oneui.utils.ActivityUtils
import kotlinx.coroutines.flow.collectLatest

//TODO/s:
// 1. navigate using navigation lib ✔
// 2. Add fragments for each repo ✔
// 3. Allow adding repo in drawer
class MainActivity : AppCompatActivity(),
    AppNavigation by MainNavigationDelegate() {

    private lateinit var mBinding: ActivityMainBinding
    private lateinit var navController: NavController

    private lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        mBinding.drawerLayout.setTitle(getString(R.string.app_name))
        setupNavigation()

        initViewModel()

        launchAndRepeatWithLifecycle(Lifecycle.State.RESUMED){
            mainViewModel.updateAvailableStateFlow.collectLatest {
                mBinding.drawerLayout.setHeaderButtonBadge(if (it) Badge.DOT else Badge.NONE)
            }
        }

        if (savedInstanceState == null) {
            mainViewModel.checkUpdate()
        }
    }

    private fun setupNavigation() {
        mBinding.drawerLayout.apply {
            setHeaderButtonIcon(
                ResourcesCompat.getDrawable(resources,
                dev.oneuiproject.oneui.R.drawable.ic_oui_settings_outline, theme)
            )
            setHeaderButtonTooltip("Stargazers Settings")
            setHeaderButtonOnClickListener {
                ActivityUtils.startPopOverActivity(
                    this@MainActivity,
                    Intent(this@MainActivity, SettingsActivity::class.java),
                    null,
                    ActivityUtils.POP_OVER_POSITION_TOP or
                            (if (isRTL) ActivityUtils.POP_OVER_POSITION_RIGHT else ActivityUtils.POP_OVER_POSITION_LEFT)
                )
            }
            setNavRailContentMinSideMargin(14)
        }

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_main) as NavHostFragment
        navController = navHostFragment.navController

        initNavigation(mBinding.drawerLayout, mBinding.drawerListView, navController)
    }

    private fun initViewModel() {
        val stargazersRepo = StargazersRepo.getInstance(this)
        val viewModelFactory = MainViewModelFactory(stargazersRepo)
        mainViewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_main_activity, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_app_info) {
            startActivity(Intent(this@MainActivity, CustomAboutActivity::class.java))
            return true
        }
        return false
    }

    private val isRTL: Boolean get() = resources.configuration.layoutDirection == LayoutDirection.RTL

    val drawerLayout: NavDrawerLayout get() = mBinding.drawerLayout

    companion object{
        const val KEY_REPO_NAME = "repoName"
    }
}
