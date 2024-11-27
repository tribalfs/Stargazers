package com.tribalfs.stargazers.ui.screens.main.core.navigation

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph
import androidx.navigation.NavOptions
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tribalfs.stargazers.R
import com.tribalfs.stargazers.ui.core.util.toast
import com.tribalfs.stargazers.ui.screens.main.MainActivity.Companion.KEY_REPO_NAME
import dev.oneuiproject.oneui.layout.DrawerLayout

class MainNavigationDelegate: AppNavigation {
    private lateinit var mAdapter: DrawerNavAdapter
    private lateinit var drawerLayout: DrawerLayout

    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {
        mAdapter.setSelectedDestinationId(destination.id)
        drawerLayout.apply {
            setCollapsedSubtitle(destination.label)
            setExpandedSubtitle(destination.label)
            post { setDrawerOpen(false, animate = true) }
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
            else ->  bundleOf("repoName" to "")
        }

    override fun initNavigation(drawerLayout: DrawerLayout, drawerListView: RecyclerView, navController: NavController) {
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
                val iconResId = dev.oneuiproject.oneui.R.drawable.ic_oui_creatures_outline
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
    fun initNavigation(drawerLayout: DrawerLayout, drawerListView: RecyclerView, navController: NavController)
}