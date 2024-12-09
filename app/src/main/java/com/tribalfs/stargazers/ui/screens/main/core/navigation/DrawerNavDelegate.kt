package com.tribalfs.stargazers.ui.screens.main.core.navigation

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.core.view.doOnLayout
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph
import androidx.navigation.NavOptions
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tribalfs.stargazers.R
import com.tribalfs.stargazers.ui.core.util.getRandomOUIDrawableId
import com.tribalfs.stargazers.ui.core.util.toast
import com.tribalfs.stargazers.ui.screens.main.MainActivity.Companion.KEY_REPO_NAME
import dev.oneuiproject.oneui.layout.DrawerLayout.DrawerState
import dev.oneuiproject.oneui.layout.NavDrawerLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MainNavigationDelegate: AppNavigation {
    private lateinit var mAdapter: DrawerNavAdapter
    private lateinit var drawerLayout: NavDrawerLayout

    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {
        mAdapter.setSelectedDestinationId(destination.id)
        drawerLayout.apply {
            setCollapsedSubtitle(destination.label)
            setExpandedSubtitle(destination.label)
            if (isActionMode) endActionMode()
            if (isSearchMode) endSearchMode()
            if (isLargeScreenMode){
                closeNavRailOnBack = destination.id == controller.graph.startDestinationId
            }else{
                post { setDrawerOpen(false, animate = true) }
            }
        }
    }

    override fun getNavOptions(destinationId: Int, startDestinationId: Int): NavOptions =
        NavOptions.Builder()
            .setLaunchSingleTop(true)
            .setRestoreState(true)
            .setPopUpTo(startDestinationId, inclusive = false, saveState = true)
            .build()


    override fun getNavArguments(destinationId: Int): Bundle =
        when (destinationId){
            R.id.sesl_androidx_dest -> bundleOf(KEY_REPO_NAME to "sesl-androidx")
            R.id.sesl_material_dest -> bundleOf(KEY_REPO_NAME to "sesl-material-components-android")
            R.id.design_lib_dest -> bundleOf(KEY_REPO_NAME to "oneui-design")
            R.id.stargazer_dest -> bundleOf(KEY_REPO_NAME to "Stargazers")
            else ->  bundleOf(KEY_REPO_NAME to "")
        }

    override fun initNavigation(drawerLayout: NavDrawerLayout, drawerListView: RecyclerView, navController: NavController) {
        this.drawerLayout = drawerLayout

        val navGraph = navController.graph
        val drawerItems = getDrawerItems(navGraph)

        mAdapter = DrawerNavAdapter( drawerItems){ destinationId ->
            val navOptions = getNavOptions(destinationId, navGraph.startDestinationId)
            val args = getNavArguments(destinationId)
            navController.navigate(
                destinationId,
                args,
                navOptions)
        }

        drawerListView.apply {
            layoutManager = LinearLayoutManager(this.context)
            adapter = mAdapter
            itemAnimator = null
            setHasFixedSize(true)
            seslSetLastRoundedCorner(false)
        }

        navController.addOnDestinationChangedListener (this)

       setupNavRailFadeEffect()
    }

    private fun setupNavRailFadeEffect(){
        drawerLayout.apply {
            if (!isLargeScreenMode) return
            setDrawerStateListener {
                when(it){
                    DrawerState.OPEN -> {
                        offsetUpdaterJob?.cancel()
                        mAdapter.updateOffset(1f)
                    }
                    DrawerState.CLOSE-> {
                        offsetUpdaterJob?.cancel()
                        mAdapter.updateOffset(0f)
                    }

                    DrawerState.CLOSING,
                    DrawerState.OPENING -> {
                        startOffsetUpdater()
                    }
                }
            }
            //Set initial offset
            doOnLayout {
                mAdapter.updateOffset(drawerLayout.drawerOffset)
            }
        }
    }

    private var offsetUpdaterJob: Job? = null
    private fun startOffsetUpdater(){
        offsetUpdaterJob = CoroutineScope(Dispatchers.Main).launch {
            while(isActive) {
                mAdapter.updateOffset(drawerLayout.drawerOffset)
                delay(50)
            }
        }
    }


    override fun getDrawerItems(navGraph: NavGraph): List<DrawerItem>{
        val drawerItems = mutableListOf<DrawerItem>()

        navGraph.findNode(navGraph.startDestinationId)?.let {
            val iconResId = dev.oneuiproject.oneui.R.drawable.ic_oui_creatures
            drawerItems.add(
                DrawerItem.DestinationItem(
                    it.id,
                    it.label.toString(),
                    iconResId
                )
            )
        }

        drawerItems.add(DrawerItem.DividerItem)

        navGraph.iterator().asSequence()
            .filter { it.id != navGraph.startDestinationId }
            .forEach { destination ->
                val iconResId = getRandomOUIDrawableId()
                drawerItems.add(
                    DrawerItem.DestinationItem(
                        destination.id,
                        destination.label.toString(),
                        iconResId
                    )
                )
            }

        drawerItems.add(DrawerItem.Button{
            //TODO
            drawerLayout.context.toast("TODO: Add repo")
        })
        return  drawerItems
    }
}




interface AppNavigation: NavController.OnDestinationChangedListener{
    fun getDrawerItems(navGraph: NavGraph): List<DrawerItem>
    fun getNavOptions(destinationId: Int, startDestinationId: Int): NavOptions
    fun getNavArguments(destinationId: Int): Bundle
    fun initNavigation(drawerLayout: NavDrawerLayout, drawerListView: RecyclerView, navController: NavController)
}
