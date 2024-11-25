package com.tribalfs.stargazers.ui.screens.main.stargazerslist

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
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
import com.tribalfs.stargazers.ui.core.util.isOnline
import com.tribalfs.stargazers.ui.core.util.launchAndRepeatWithViewLifecycle
import com.tribalfs.stargazers.ui.core.util.openUrl
import com.tribalfs.stargazers.ui.core.util.seslSetFastScrollerEnabledForApi24
import com.tribalfs.stargazers.ui.core.util.toast
import com.tribalfs.stargazers.ui.core.view.TouchBlockingView
import com.tribalfs.stargazers.ui.screens.main.MainActivity
import com.tribalfs.stargazers.ui.screens.main.MainActivity.Companion.KEY_REPO_NAME
import com.tribalfs.stargazers.ui.screens.main.core.base.AbsBaseFragment
import com.tribalfs.stargazers.ui.screens.main.stargazerslist.StargazersListViewModel.Companion.SWITCH_TO_HPB_DELAY
import com.tribalfs.stargazers.ui.screens.main.stargazerslist.StargazersListViewModel.LoadState
import com.tribalfs.stargazers.ui.screens.main.stargazerslist.adapter.StargazersAdapter
import com.tribalfs.stargazers.ui.screens.main.stargazerslist.model.StargazersListItemUiModel
import com.tribalfs.stargazers.ui.screens.main.stargazerslist.util.updateIndexer
import com.tribalfs.stargazers.ui.screens.profile.ProfileActivity
import com.tribalfs.stargazers.ui.screens.profile.ProfileActivity.Companion.KEY_STARGAZER
import com.tribalfs.stargazers.ui.screens.profile.ProfileActivity.Companion.KEY_TRANSITION_AVATAR
import com.tribalfs.stargazers.ui.screens.profile.ProfileActivity.Companion.KEY_TRANSITION_CONTAINER
import com.tribalfs.stargazers.ui.screens.profile.ProfileActivity.Companion.KEY_TRANSITION_NAME
import dev.oneuiproject.oneui.delegates.AppBarAwareYTranslator
import dev.oneuiproject.oneui.delegates.ViewYTranslator
import dev.oneuiproject.oneui.ktx.configureItemSwipeAnimator
import dev.oneuiproject.oneui.ktx.dpToPx
import dev.oneuiproject.oneui.ktx.enableCoreSeslFeatures
import dev.oneuiproject.oneui.layout.ToolbarLayout
import dev.oneuiproject.oneui.layout.ToolbarLayout.SearchModeOnBackBehavior
import dev.oneuiproject.oneui.layout.startActionMode
import dev.oneuiproject.oneui.layout.startSearchMode
import dev.oneuiproject.oneui.utils.ItemDecorRule
import dev.oneuiproject.oneui.utils.SemItemDecoration
import dev.oneuiproject.oneui.widget.TipPopup
import dev.oneuiproject.oneui.widget.TipPopup.Direction
import dev.oneuiproject.oneui.widget.TipPopup.Mode
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class StargazersListFragment : AbsBaseFragment(), ViewYTranslator by AppBarAwareYTranslator() {

    private lateinit var stargazersAdapter: StargazersAdapter

    private var _binding: FragmentStargazersListBinding? = null
    private val binding get() = _binding!!

    private lateinit var stargazersViewModel: StargazersListViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentStargazersListBinding.inflate(layoutInflater, container, false).also {
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

        if (savedInstanceState == null) {
            arguments?.getString(KEY_REPO_NAME)?.let {
                stargazersViewModel.setRepoFilter(it)
            }
        }
    }


    private fun configureRecyclerView() {
        binding.stargazersList.apply rv@{
            setLayoutManager(LinearLayoutManager(requireContext()))
            setAdapter(StargazersAdapter(requireContext()).also {
                it.setupOnClickListeners()
                stargazersAdapter = it
            })
            addItemDecoration(
                SemItemDecoration(requireContext(),
                    dividerRule = ItemDecorRule.SELECTED{
                        it.itemViewType == StargazersListItemUiModel.StargazerItem.VIEW_TYPE
                    },
                    subHeaderRule = ItemDecorRule.SELECTED{
                        it.itemViewType == StargazersListItemUiModel.SeparatorItem.VIEW_TYPE
                    }).apply {
                    setDividerInsetStart(78.dpToPx(resources))
                }
            )
            setItemAnimator(null)
            enableCoreSeslFeatures(fastScrollerEnabled = false)

            stargazersAdapter.configure(
                this,
                StargazersAdapter.Payload.SELECTION_MODE,
                onAllSelectorStateChanged = { stargazersViewModel.allSelectorStateFlow.value = it }
            )

            binding.fab.hideOnScroll(this@rv/*, binding.indexscrollView*/)

            binding.indexscrollView.attachToRecyclerView(this@rv)
        }

        translateYWithAppBar(
            setOf(binding.nsvNoItem, binding.loadingPb),
            (requireActivity() as MainActivity).drawerLayout.appBarLayout,
            this@StargazersListFragment
        )
    }

    private fun setupFabClickListener() {
        binding.fab.apply {
            setOnClickListener {
                TipPopup(it, Mode.TRANSLUCENT).apply {
                    setMessage("STAR any of these repositories: Stargazers, OneUI6 design lib, sesl-androidx and sesl-material.")
                    setExpanded(true)
                    setAction("Ok") {
                        requireContext().openUrl("https://github.com/tribalfs/${arguments?.getString(KEY_REPO_NAME)?:""}")
                    }
                    show(Direction.TOP_LEFT)
                }
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
                    Snackbar.make(this, "No internet connection detected.", Snackbar.LENGTH_SHORT)
                        .show()
                }
                stargazersViewModel.refreshStargazers(true)
            }
        }
    }


    private fun observeUIState() {
        val stargazersRepo = StargazersRepo.getInstance(requireContext())
        val viewModelFactory = StargazersListViewModelFactory(stargazersRepo)

        stargazersViewModel =
            ViewModelProvider(this, viewModelFactory)[StargazersListViewModel::class.java]

        launchAndRepeatWithViewLifecycle {
            launch {
                stargazersViewModel.stargazersListScreenStateFlow
                    .collectLatest {
                        val itemsList = it.itemsList

                        updateLoadStateViews(it.loadState, itemsList.isEmpty())
                        stargazersAdapter.submitList(itemsList)

                        if (itemsList.isNotEmpty()) {
                            updateRecyclerViewVisibility(true, it.noItemText)
                            binding.indexscrollView.updateIndexer(itemsList)
                        } else {
                            updateRecyclerViewVisibility(false, it.noItemText)
                        }

                        stargazersAdapter.highlightWord = it.query
                    }
            }

            launch {
                stargazersViewModel.stargazerSettingsStateFlow
                    .collectLatest {
                        binding.stargazersList.seslSetFastScrollerEnabledForApi24(!it.enableIndexScroll)

                        binding.indexscrollView.apply {
                            setAutoHide(it.autoHideIndexScroll)
                            setIndexBarTextMode(it.isTextModeIndexScroll)
                            isVisible = it.enableIndexScroll
                        }

                        stargazersAdapter.searchHighlightColor = it.searchHighlightColor

                        val shouldAutoRefresh = it.lastRefresh != 0L &&
                                (System.currentTimeMillis() - it.lastRefresh) > 1000*60*60*15

                        if (shouldAutoRefresh){
                            stargazersViewModel.refreshStargazers(true)
                        }

                        if (!it.initTipShown){
                            showInitTip()
                        }
                    }
            }
        }
    }

    private var lastStateReceived: LoadState? = null

    private fun updateLoadStateViews(loadState: LoadState, isEmpty: Boolean) {
        if (lastStateReceived == loadState) return
        lastStateReceived = loadState

        when (loadState) {
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

            LoadState.LOADED ->{
                binding.loadingPb.isVisible = false
                binding.horizontalPb.isVisible = false
            }

            LoadState.ERROR -> {
                binding.loadingPb.isVisible = false
                binding.horizontalPb.isVisible = false
                binding.retryBtn.isVisible = isEmpty
                if (!isEmpty){
                    Snackbar.make(requireActivity().window.decorView, "Error loading stargazers.", Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun updateRecyclerViewVisibility(visible: Boolean, noItemText: String) {
        binding.nsvNoItem.isVisible = !visible
        binding.stargazersList.isVisible = visible
        binding.indexscrollView.isVisible = visible && stargazersViewModel.isIndexScrollEnabled()
        binding.tvNoItem.text = noItemText
    }


    private fun StargazersAdapter.setupOnClickListeners() {
        onClickItem = { stargazer, position, vh ->
            if (isActionMode) {
                onToggleItem(stargazer.toStableId(), position)
            } else {
                when (stargazer) {
                    is StargazersListItemUiModel.StargazerItem -> {
                        openProfileActivity(vh, stargazer)
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
            isLeftSwipeEnabled = { viewHolder ->
                viewHolder.itemViewType == StargazersListItemUiModel.StargazerItem.VIEW_TYPE
                        && !stargazersAdapter.isActionMode
            },
            isRightSwipeEnabled = { viewHolder ->
                viewHolder.itemViewType == StargazersListItemUiModel.StargazerItem.VIEW_TYPE
                        && (stargazersAdapter.getItemByPosition(viewHolder.layoutPosition)
                        as StargazersListItemUiModel.StargazerItem).stargazer.twitter_username != null
                        && !stargazersAdapter.isActionMode
            },
            onSwiped = { position, swipeDirection, _ ->
                val stargazer =
                    (stargazersAdapter.getItemByPosition(position) as StargazersListItemUiModel.StargazerItem).stargazer
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
        with(requireActivity() as MainActivity) {
            binding.fab.isVisible = false
            drawerLayout.startActionMode(
                onInflateMenu = { menu ->
                    stargazersAdapter.onToggleActionMode(true, initialSelected)
                    requireActivity().menuInflater.inflate(R.menu.menu_stargazers_am, menu)
                },
                onEnd = {
                    stargazersAdapter.onToggleActionMode(false)
                    binding.fab.isVisible = !drawerLayout.isSearchMode
                },
                onSelectMenuItem = {
                    when (it.itemId) {
                        R.id.menu_contacts_am_share -> {
                            requireActivity().toast("Todo(Share!)")
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


    private fun ToolbarLayout.launchSearchMode(onBackBehavior: SearchModeOnBackBehavior) {
        startSearchMode(
            onBackBehavior = onBackBehavior,
            onQuery = { query, _ ->
                stargazersViewModel.setQuery(query)
                true
            },
            onStart = {
                searchView.queryHint = "Search stargazer"
                binding.fab.isVisible = false
            },
            onEnd = {
                stargazersViewModel.setQuery("")
                binding.fab.isVisible = !isActionMode
            }
        )
    }

    private fun showInitTip() {
        binding.stargazersList.doOnLayout {
            it.postDelayed({
                val anchor = binding.stargazersList.layoutManager!!.findViewByPosition(2)
                if (anchor != null) {
                    stargazersViewModel.setInitTipShown()
                    showInitTip(anchor, "Long-press item for multi-selection." +
                            "\nSwipe left to open github." +
                            "\nSwipe right to open X, if available.", { })
                }
            }, 700)
        }
    }

    private fun showInitTip(anchor: View, message: String,
                            action: () -> Unit) {
        val rootView = requireActivity().window.decorView.rootView as ViewGroup
        val blockingView = TouchBlockingView(requireContext())
        rootView.addView(blockingView)

        TipPopup(anchor, Mode.TRANSLUCENT).apply {
            setMessage(message)
            setExpanded(true)
            setOutsideTouchEnabled(false)
            setAction("Ok") {
                rootView.removeView(blockingView)
                action()
            }
            show(Direction.DEFAULT)
        }
    }

    private fun openProfileActivity(
        vh: StargazersAdapter.ViewHolder,
        stargazer: StargazersListItemUiModel.StargazerItem
    ) {
        suspendItemRipple(vh.itemView)
        val transitionName = stargazer.stargazer.id.toString()
        val transitionName2 = "${transitionName}1"
        val transitionName3 = "${transitionName}2"

        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
            requireActivity(),
            Pair(vh.itemView, transitionName),
            Pair(vh.avatarView, transitionName2),
            Pair(vh.nameView, transitionName3)
        )

        requireActivity().startActivity(
            Intent(
                requireActivity(),
                ProfileActivity::class.java
            ).apply {
                putExtra(KEY_STARGAZER, stargazer.stargazer)
                putExtra(KEY_TRANSITION_CONTAINER, transitionName)
                putExtra(KEY_TRANSITION_AVATAR, transitionName2)
                putExtra(KEY_TRANSITION_NAME, transitionName3)
            }, options.toBundle()
        )
    }

    // TODO(find a better solution)
    /** Temporary disable item view's background to
     * workaround bleeding ripple on rounded corners
     * when calling startActivity with ActivityOptions.makeSceneTransitionAnimation()*/
    private fun suspendItemRipple(itemView: View) {
        val backgroundBu = itemView.background
        itemView.postOnAnimation { itemView.background = null }
        itemView.postDelayed({ itemView.background = backgroundBu }, 1_000)
    }

    private val menuProvider = object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.menu_stargazers_list, menu)
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            return when (menuItem.itemId) {
                R.id.menu_sg_search -> {
                    (requireActivity() as MainActivity).drawerLayout
                        .launchSearchMode(stargazersViewModel.getSearchModeOnBackBehavior())
                    true
                }
                else -> return false
            }
        }
    }

    companion object {
        private const val TAG = "StargazersListFragment"
    }
}
