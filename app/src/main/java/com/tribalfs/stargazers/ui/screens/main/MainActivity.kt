package com.tribalfs.stargazers.ui.screens.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.tribalfs.stargazers.R
import com.tribalfs.stargazers.databinding.ActivityMainBinding
import com.tribalfs.stargazers.ui.core.base.FragmentInfo
import com.tribalfs.stargazers.ui.core.drawer.DrawerListAdapter
import com.tribalfs.stargazers.ui.screens.customabout.CustomAboutActivity
import com.tribalfs.stargazers.ui.screens.main.stargazerslist.StargazersListFragment
import dev.oneuiproject.oneui.ktx.invokeOnBackPressed
import dev.oneuiproject.oneui.layout.Badge
import dev.oneuiproject.oneui.layout.DrawerLayout
import dev.oneuiproject.oneui.layout.DrawerLayout.DrawerState
import dev.oneuiproject.oneui.layout.setButtonBadges
import dev.oneuiproject.oneui.layout.setDrawerButtonBadge
import dev.oneuiproject.oneui.layout.setNavigationBadge
import kotlinx.coroutines.flow.MutableStateFlow


class MainActivity : AppCompatActivity(), DrawerListAdapter.DrawerListener {
    private var mBinding: ActivityMainBinding? = null
    private var mFragmentManager: FragmentManager? = null
    private val fragments: MutableList<Fragment> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding!!.root)

        initFragmentList()
        initDrawer()
        initFragments()
        initOnBackPressed()
    }


    private fun initFragmentList() {
        fragments.add(StargazersListFragment())
    }

    private val callBackState = MutableStateFlow(false)

    private fun initOnBackPressed() {
        invokeOnBackPressed(callBackState){
            onDrawerItemSelected(0)
            (mBinding!!.drawerListView.adapter as DrawerListAdapter).setSelectedItem(0)
        }
    }

    private fun initDrawer() {
        mBinding!!.drawerLayout.setDrawerButtonIcon(
            getDrawable(dev.oneuiproject.oneui.R.drawable.ic_oui_info_outline))
        mBinding!!.drawerLayout.setDrawerButtonTooltip("App info")
        mBinding!!.drawerLayout.setDrawerButtonOnClickListener { v: View? ->
            startActivity(
                Intent(
                    this@MainActivity,
                    CustomAboutActivity::class.java
                )
            )
            mBinding!!.drawerLayout.setDrawerButtonBadge(Badge.NONE)
        }

        mBinding!!.drawerListView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            mBinding!!.drawerListView.adapter =
                DrawerListAdapter(this@MainActivity, fragments, this@MainActivity)
            mBinding!!.drawerListView.itemAnimator = null
            mBinding!!.drawerListView.setHasFixedSize(true)
            mBinding!!.drawerListView.seslSetLastRoundedCorner(false)
            mBinding!!.drawerLayout.setButtonBadges(Badge.NONE, Badge.NONE)
            mBinding!!.drawerLayout.setDrawerStateListener { state: DrawerState ->
                if (state == DrawerState.OPEN) mBinding!!.drawerLayout.setNavigationBadge(Badge.NONE)
            }
        }

    }

    private fun initFragments() {
        mFragmentManager = supportFragmentManager
        val transaction = mFragmentManager!!.beginTransaction()
        for (fragment in fragments) {
            if (fragment != null) transaction.add(R.id.main_content, fragment)
        }
        transaction.commit()
        mFragmentManager!!.executePendingTransactions()

        onDrawerItemSelected(0)
    }

    override fun onDrawerItemSelected(position: Int): Boolean {
        val newFragment = fragments[position]
        val transaction = mFragmentManager!!.beginTransaction()
        for (fragment in mFragmentManager!!.fragments) {
            transaction.hide(fragment)
        }
        transaction.show(newFragment).commit()

        if (newFragment is FragmentInfo) {
            if (!(newFragment as FragmentInfo).isAppBarEnabled) {
                mBinding!!.drawerLayout.setExpanded(false, false)
                mBinding!!.drawerLayout.isExpandable = false
            } else {
                mBinding!!.drawerLayout.isExpandable = true
                mBinding!!.drawerLayout.setExpanded(false, false)
            }
            mBinding!!.drawerLayout.setTitle((newFragment as FragmentInfo).title)
            if (newFragment is StargazersListFragment) {
                mBinding!!.drawerLayout.setExpandedSubtitle("Pull down to refresh")
            }
        }
        mBinding!!.drawerLayout.setDrawerOpen(false, true)

        callBackState.value = position != 0

        return true
    }

    val drawerLayout: DrawerLayout
        get() = mBinding!!.drawerLayout
}
