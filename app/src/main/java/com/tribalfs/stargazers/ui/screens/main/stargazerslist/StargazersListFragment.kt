package com.tribalfs.stargazers.ui.screens.main.stargazerslist

import android.app.ActivityOptions
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.LayoutDirection
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuProvider
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper.END
import androidx.recyclerview.widget.ItemTouchHelper.START
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.tribalfs.stargazers.R
import com.tribalfs.stargazers.data.StargazersRepo
import com.tribalfs.stargazers.databinding.FragmentStargazersListBinding
import com.tribalfs.stargazers.ui.core.base.AbsBaseFragment
import com.tribalfs.stargazers.ui.core.util.isOnline
import com.tribalfs.stargazers.ui.core.util.launchAndRepeatWithViewLifecycle
import com.tribalfs.stargazers.ui.core.util.openUrl
import com.tribalfs.stargazers.ui.core.util.seslSetFastScrollerEnabledForApi24
import com.tribalfs.stargazers.ui.core.util.toast
import com.tribalfs.stargazers.ui.screens.main.MainActivity
import com.tribalfs.stargazers.ui.screens.main.stargazerslist.StargazersListViewModel.Companion.SWITCH_TO_HPB_DELAY
import com.tribalfs.stargazers.ui.screens.main.stargazerslist.StargazersListViewModel.LoadState
import com.tribalfs.stargazers.ui.screens.main.stargazerslist.adapter.StargazersAdapter
import com.tribalfs.stargazers.ui.screens.main.stargazerslist.adapter.StargazersListItemDecoration
import com.tribalfs.stargazers.ui.screens.main.stargazerslist.model.StargazersListItemUiModel
import com.tribalfs.stargazers.ui.screens.main.stargazerslist.util.updateIndexer
import com.tribalfs.stargazers.ui.screens.profile.ProfileActivity
import com.tribalfs.stargazers.ui.screens.profile.ProfileActivity.Companion.KEY_STARGAZER
import com.tribalfs.stargazers.ui.screens.profile.ProfileActivity.Companion.KEY_TRANSITION_NAME
import com.tribalfs.stargazers.ui.screens.settings.main.MainSettingsActivity
import dev.oneuiproject.oneui.delegates.AppBarAwareYTranslator
import dev.oneuiproject.oneui.delegates.ViewYTranslator
import dev.oneuiproject.oneui.ktx.clearBadge
import dev.oneuiproject.oneui.ktx.configureItemSwipeAnimator
import dev.oneuiproject.oneui.ktx.enableCoreSeslFeatures
import dev.oneuiproject.oneui.ktx.setBadge
import dev.oneuiproject.oneui.layout.Badge
import dev.oneuiproject.oneui.layout.ToolbarLayout
import dev.oneuiproject.oneui.layout.ToolbarLayout.SearchModeOnBackBehavior
import dev.oneuiproject.oneui.layout.startActionMode
import dev.oneuiproject.oneui.layout.startSearchMode
import dev.oneuiproject.oneui.utils.ActivityUtils
import dev.oneuiproject.oneui.widget.TipPopup
import dev.oneuiproject.oneui.widget.TipPopup.Direction
import dev.oneuiproject.oneui.widget.TipPopup.Mode
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class StargazersListFragment : AbsBaseFragment(), ViewYTranslator by AppBarAwareYTranslator() {

    private var tipPopupShown = false
    private var tipPopup: TipPopup? = null
    private lateinit var stargazersAdapter: StargazersAdapter

    private var _binding: FragmentStargazersListBinding? = null
    private val binding  get() = _binding!!

    private lateinit var stargazersViewModel: StargazersListViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentStargazersListBinding.inflate(layoutInflater, container, false).also{
        _binding = it
        requireActivity().addMenuProvider(menuProvider, viewLifecycleOwner, Lifecycle.State.STARTED)
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureRecyclerView()
        configureSwipeRefresh()
        configureItemSwipeAnimator()
        setupFabClickListener()
        observeUIState()

        showFragmentMenu(!isHidden)
    }


    private fun configureRecyclerView() {
        binding.stargazersList.apply rv@{
            setLayoutManager(LinearLayoutManager(requireContext()))
            setAdapter(StargazersAdapter(requireContext()).also {
                it.setupOnClickListeners()
                stargazersAdapter = it
            })
            addItemDecoration(StargazersListItemDecoration(requireContext()))
            setItemAnimator(null)
            enableCoreSeslFeatures(fastScrollerEnabled = false)

            stargazersAdapter.configure(
                this,
                StargazersAdapter.Payload.SELECTION_MODE,
                onAllSelectorStateChanged = { stargazersViewModel.allSelectorStateFlow.value = it }
            )

            val fab = (requireActivity() as MainActivity).fab.also {
                it.hideOnScroll(this@rv)
            }

            binding.indexscrollView.apply {
                attachToRecyclerView(this@rv)
                attachScrollAwareFAB(fab)
            }
        }

        with((requireActivity() as MainActivity).drawerLayout.appBarLayout) {
            translateYWithAppBar(setOf(binding.nsvNoItem, binding.loadingPb), this, this@StargazersListFragment)
        }
    }

    private fun setupFabClickListener() {
        (requireActivity() as MainActivity).fab.apply {
            setOnClickListener {
                Snackbar.make(
                    it,
                    "STAR any of these repositories: Stargazers, OneUI6 design lib, sesl-androidx and sesl-material",
                    Snackbar.LENGTH_INDEFINITE
                ).apply {
                    setAction("OK") { requireContext().openUrl("https://github.com/tribalfs/oneui-design") }
                }.show()
            }
        }
    }


    private fun configureSwipeRefresh() {
        binding.swiperefreshView.apply {
            seslSetRefreshOnce(true)
            setOnRefreshListener {
                stargazersViewModel.refreshStargazers()
                viewLifecycleOwner.lifecycleScope.launch {
                    delay(SWITCH_TO_HPB_DELAY)
                    //We are switching to less intrusive horizontal progress bar
                    //if refreshing is not yet completed at this moment
                    isRefreshing = false
                }
            }
        }

        binding.retryBtn.apply {
            setOnClickListener {
                if (!isOnline(requireContext())) {
                    Snackbar.make(this,  "No internet connection detected.", Snackbar.LENGTH_SHORT).show()
                }
                stargazersViewModel.refreshStargazers(true)
            }
        }
    }


    private fun observeUIState(){
        val stargazersRepo = StargazersRepo(requireContext())
        val viewModelFactory = StargazersListViewModelFactory(stargazersRepo)
        stargazersViewModel = ViewModelProvider(this, viewModelFactory)[StargazersListViewModel::class.java]

        launchAndRepeatWithViewLifecycle {
            launch {
                stargazersViewModel.stargazersListScreenStateFlow
                    .collectLatest {
                        when (it.loadState) {
                            LoadState.LOADING -> {
                                binding.loadingPb.isVisible = true
                                binding.horizontalPb.isVisible = false
                                binding.retryBtn.isVisible = false
                            }

                            LoadState.REFRESHING -> {
                                binding.loadingPb.isVisible = false
                                binding.horizontalPb.isVisible = true
                                binding.retryBtn.isVisible = false

                            }

                            LoadState.LOADED,
                            LoadState.ERROR -> {
                                binding.loadingPb.isVisible = false
                                binding.horizontalPb.isVisible = false
                                binding.retryBtn.isVisible =
                                    it.loadState == LoadState.ERROR && it.itemsList.isEmpty()
                            }
                        }
                        val itemsList = it.itemsList
                        stargazersAdapter.submitList(itemsList)
                        if (itemsList.isNotEmpty()) {
                            updateRecyclerViewVisibility(true, it.noItemText)
                            binding.indexscrollView.updateIndexer(itemsList)
                            showTipPopup()
                        } else {
                            updateRecyclerViewVisibility(false, it.noItemText)
                        }
                        stargazersAdapter.highlightWord = it.query
                    }
            }

            launch {
                stargazersViewModel.stargazerSettingsStateFlow
                    .collectLatest {
                        if (it.enableIndexScroll) {
                            binding.indexscrollView.isVisible = true
                            binding.stargazersList.seslSetFastScrollerEnabledForApi24(false)
                        } else {
                            binding.indexscrollView.isVisible = false
                            binding.stargazersList.seslSetFastScrollerEnabledForApi24(true)
                        }
                        binding.indexscrollView.setAutoHide(it.autoHideIndexScroll)
                        stargazersAdapter.searchHighlightColor = it.searchHighlightColor
                    }
            }
        }
    }


    private fun updateRecyclerViewVisibility(visible: Boolean, noItemText: String){
        binding.nsvNoItem.isVisible = !visible
        binding.stargazersList.isVisible = visible
        binding.indexscrollView.isVisible = visible && stargazersViewModel.isIndexScrollEnabled()
        binding.tvNoItem.text = noItemText
    }


    private fun StargazersAdapter.setupOnClickListeners(){
        onClickItem = { stargazer, position, itemView ->
            if (isActionMode) {
                onToggleItem(stargazer.toStableId(), position)
            }else {
                when(stargazer){
                    is StargazersListItemUiModel.StargazerItem -> {
                        openProfileActivity(itemView, stargazer)
                    }
                    is StargazersListItemUiModel.GroupItem -> {
                        toast("${stargazer.groupName} clicked!")
                    }
                    else -> Unit
                }
            }
        }

        onLongClickItem = {
            if (!isActionMode) launchActionMode()
            binding.stargazersList.seslStartLongPressMultiSelection()
        }
    }

    private fun configureItemSwipeAnimator() {
        binding.stargazersList.configureItemSwipeAnimator(
            leftToRightLabel = "Github",
            rightToLeftLabel = "X",
            leftToRightColor = Color.parseColor("#4078c0"),
            rightToLeftColor = Color.parseColor("#31a5f3"),
            leftToRightDrawableRes = R.drawable.about_page_github,
            rightToLeftDrawableRes = R.drawable.x_logo,
            isLeftSwipeEnabled = {viewHolder ->
                viewHolder.itemViewType == StargazersListItemUiModel.StargazerItem.VIEW_TYPE
                        && !stargazersAdapter.isActionMode
            },
            isRightSwipeEnabled = {viewHolder ->
                viewHolder.itemViewType == StargazersListItemUiModel.StargazerItem.VIEW_TYPE
                        && (stargazersAdapter.getItemByPosition(viewHolder.layoutPosition)
                        as StargazersListItemUiModel.StargazerItem).stargazer.twitter_username != null
                        && !stargazersAdapter.isActionMode
            },
            onSwiped = { position, swipeDirection, _ ->
                val stargazer = (stargazersAdapter.getItemByPosition(position) as StargazersListItemUiModel.StargazerItem).stargazer
                if (swipeDirection == START) {
                    requireContext().openUrl("https://x.com/${stargazer.twitter_username}")
                }
                if (swipeDirection == END) {
                    requireContext().openUrl(stargazer.html_url)
                }
                true
            }
        )
    }


    private fun launchActionMode(initialSelected: Array<Long>? = null) {
        with (requireActivity() as MainActivity) {
            fab.temporaryHide = true
            drawerLayout.startActionMode(
                onInflateMenu = { menu ->
                    stargazersAdapter.onToggleActionMode(true, initialSelected)
                    requireActivity().menuInflater.inflate(R.menu.menu_stargazers_am, menu)
                },
                onEnd = {
                    stargazersAdapter.onToggleActionMode(false)
                    fab.temporaryHide = false
                },
                onSelectMenuItem = {
                    when (it.itemId) {
                        R.id.menu_contacts_am_share -> {
                            requireActivity().toast("Share!")
                            drawerLayout.endActionMode()
                            true
                        }

                        R.id.menu_contacts_am_delete -> {
                            requireActivity().toast("Delete!")
                            drawerLayout.endActionMode()
                            true
                        }

                        else -> false
                    }
                },
                onSelectAll = { isChecked: Boolean -> stargazersAdapter.onToggleSelectAll(isChecked) },
                allSelectorStateFlow = stargazersViewModel.allSelectorStateFlow,
                keepSearchMode = stargazersViewModel.getKeepSearchModeOnActionMode()
            )
        }
    }

    private val menuProvider = object : MenuProvider {

        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.menu_stargazers_list, menu)

            val searchMenuItem = menu.findItem(R.id.menu_sg_settings)
            searchMenuItem.setBadge(Badge.DOT)
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            return when (menuItem.itemId) {
                R.id.menu_sg_settings -> {
                    ActivityUtils.startPopOverActivity(
                        requireContext(),
                        Intent(requireContext(), MainSettingsActivity::class.java),
                        null,
                        ActivityUtils.POP_OVER_POSITION_TOP or
                                (if (!isRTL) ActivityUtils.POP_OVER_POSITION_RIGHT else ActivityUtils.POP_OVER_POSITION_LEFT)
                    )
                    menuItem.clearBadge()
                    true
                }

                R.id.menu_sg_search -> {
                    (requireActivity() as MainActivity).drawerLayout
                        .launchSearchMode(stargazersViewModel.getSearchModeOnBackBehavior())
                    true
                }

                else -> return false
            }
        }
    }

    private fun ToolbarLayout.launchSearchMode(onBackBehavior: SearchModeOnBackBehavior) {
        startSearchMode(
            onBackBehavior = onBackBehavior,
            onQuery = { query, _ ->
                stargazersViewModel.setQuery(query)
                true
            },
            onStart = {
                searchView.queryHint = "Search contact"
            },
            onEnd = { stargazersViewModel.setQuery("") }
        )
    }

    private fun showTipPopup() {
        binding.stargazersList.doOnLayout {
            if (!tipPopupShown) {
                tipPopupShown = true
                it.postDelayed({
                    val anchor = binding.stargazersList.layoutManager!!.findViewByPosition(2)
                    if (anchor != null) {
                        tipPopup = TipPopup(anchor, Mode.TRANSLUCENT).apply {
                            setMessage("Long-press item for multi-selection.\nSwipe left to open github. \nSwipe right to open X, if available.")
                            setExpanded(true)
                            show(Direction.DEFAULT)
                        }
                    }
                }, 300)
            }
        }
    }

    private fun openProfileActivity(
        itemView: View,
        stargazer: StargazersListItemUiModel.StargazerItem
    ) {
        suspendItemRipple(itemView)
        val transitionName = stargazer.stargazer.id.toString()
        val options = ActivityOptions.makeSceneTransitionAnimation(
            requireActivity(),
            itemView,
            transitionName
        )
        requireActivity().startActivity(
            Intent(
                requireActivity(),
                ProfileActivity::class.java
            ).apply {
                putExtra(KEY_STARGAZER, stargazer.stargazer)
                putExtra(KEY_TRANSITION_NAME, transitionName)
            }, options.toBundle()
        )
    }

    // TODO(find a better solution)
    /** Temporary disable item view's background to
     * workaround bleeding ripple on rounded corners
     * when calling startActivity with ActivityOptions.makeSceneTransitionAnimation()*/
    private fun suspendItemRipple(itemView: View){
        val backgroundBu = itemView.background
        itemView.postOnAnimation{itemView.background = null}
        itemView.postDelayed({ itemView.background = backgroundBu}, 1_000)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        if (!hidden) {
            showFragmentMenu(true)
            (requireActivity() as MainActivity).fab.apply {
                isVisible = true
                show()
            }
        } else {
            showFragmentMenu(false)
            (requireActivity() as MainActivity).fab.isVisible = false
        }
    }

    private fun showFragmentMenu(show: Boolean){
        (requireActivity() as MainActivity).apply {
            removeMenuProvider(menuProvider)
            if (show){
                addMenuProvider(
                    menuProvider,
                    viewLifecycleOwner,
                    Lifecycle.State.STARTED
                )
            }
        }
    }

    private val isRTL: Boolean
        get() = resources.configuration.layoutDirection == LayoutDirection.RTL

    override fun getLayoutResId(): Int = R.layout.fragment_stargazers_list

    override fun getIconResId(): Int = dev.oneuiproject.oneui.R.drawable.ic_oui_creatures_outline

    override fun getTitle(): CharSequence = "Stargazers"

    companion object{
        private const val TAG = "StargazersListFragment"
    }
}
