package com.tribalfs.stargazers.ui.screens.main.icons;

import static dev.oneuiproject.oneui.layout.ToolbarLayout.SearchModeOnBackBehavior.CLEAR_DISMISS;
import static com.tribalfs.stargazers.ui.core.util.ToastKt.toast;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.menu.SeslMenuItem;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuProvider;
import androidx.core.widget.NestedScrollView;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver;

import dev.oneuiproject.oneui.delegates.AppBarAwareYTranslator;
import dev.oneuiproject.oneui.delegates.ViewYTranslator;
import dev.oneuiproject.oneui.layout.DrawerLayout;
import dev.oneuiproject.oneui.layout.ToolbarLayout;
import dev.oneuiproject.oneui.widget.TipPopup;

import com.tribalfs.stargazers.R;
import com.tribalfs.stargazers.ui.screens.main.MainActivity;
import com.tribalfs.stargazers.ui.core.base.AbsBaseFragment;
import com.tribalfs.stargazers.data.IconsRepo;
import com.tribalfs.stargazers.ui.screens.main.icons.adapter.StartgazersAdapter;
import com.tribalfs.stargazers.ui.core.ItemDecoration;

public class IconsFragment extends AbsBaseFragment {

    private DrawerLayout drawerLayout;
    private StartgazersAdapter adapter;
    private AdapterDataObserver observer;
    private boolean tipPopupShown = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        drawerLayout = ((MainActivity)getActivity()).getDrawerLayout();
        adapter = new StartgazersAdapter(getContext());
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (!hidden) {
            adapter.registerAdapterDataObserver(observer);
            requireActivity().addMenuProvider(menuProvider, getViewLifecycleOwner(), Lifecycle.State.STARTED);
            showTipPopup();
        }else{
            adapter.unregisterAdapterDataObserver(observer);
            requireActivity().removeMenuProvider(menuProvider);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView iconListView = getView().findViewById(R.id.recyclerView);

        setupRecyclerView(iconListView, adapter);
        setupSelection(iconListView, adapter);
        setupAdapterClickListeners(iconListView, adapter);

        IconsRepo iconsRepo = new IconsRepo();
        adapter.submitList(iconsRepo.getIcons());
    }

    private void setupRecyclerView(RecyclerView iconListView, StartgazersAdapter adapter){
        iconListView.setItemAnimator(null);
        iconListView.setAdapter(adapter);
        iconListView.addItemDecoration(new ItemDecoration(requireContext()));
        iconListView.setLayoutManager(new LinearLayoutManager(mContext));
        iconListView.seslSetFillBottomEnabled(true);
        iconListView.seslSetLastRoundedCorner(true);
        iconListView.seslSetFastScrollerEnabled(true);
        iconListView.seslSetGoToTopEnabled(true);
        iconListView.seslSetSmoothScrollEnabled(true);
        iconListView.seslSetIndexTipEnabled(true);

        observer = new AdapterDataObserver() {
            RecyclerView iconListView = getView().findViewById(R.id.recyclerView);
            NestedScrollView notItemView = getView().findViewById(R.id.nsvNoItem);
            @Override
            public void onChanged() {
                if(adapter.getItemCount() > 0){
                    iconListView.setVisibility(View.VISIBLE);
                    notItemView.setVisibility(View.GONE);
                }else{
                    iconListView.setVisibility(View.GONE);
                    notItemView.setVisibility(View.VISIBLE);
                }
            }
        };
    }

    private void setupSelection(RecyclerView iconListView, StartgazersAdapter adapter){
            adapter.configure(
                iconListView,
                null,
                adapter::getItem,
                ass -> {drawerLayout.updateAllSelector(ass.totalSelected, ass.isEnabled, ass.isChecked); return null;}
        );
    }

    private void setupAdapterClickListeners(RecyclerView iconListView, StartgazersAdapter adapter){
        adapter.onItemClickListener = new StartgazersAdapter.OnItemClickListener() {

            @Override
            public void onItemClick(Integer iconId, int position) {
                if (adapter.isActionMode()) {
                    adapter.onToggleItem(iconId, position);
                }else {
                    toast(IconsFragment.this, getResources().getResourceEntryName(iconId) + " clicked!");
                }
            }

            @Override
            public void onItemLongClick(Integer iconId, int position) {
                if (!adapter.isActionMode()){
                    launchActionMode(adapter);
                }
                iconListView.seslStartLongPressMultiSelection();
            }
        };
    }

    private void launchActionMode(StartgazersAdapter adapter) {
        adapter.onToggleActionMode(true, null);

        drawerLayout.startActionMode(new ToolbarLayout.ActionModeListener() {
            @Override
            public void onInflateActionMenu(@NonNull Menu menu) {
                requireActivity().getMenuInflater().inflate(R.menu.menu_action_mode_icons, menu);
            }

            @Override
            public void onEndActionMode() {
                adapter.onToggleActionMode(false, null);
            }

            @Override
            public boolean onMenuItemClicked(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.icons_am_menu1
                        || item.getItemId() == R.id.icons_am_menu2) {
                    toast(IconsFragment.this, item.getTitle().toString());
                    return true;
                }
                return false;
            }

            @Override
            public void onSelectAll(boolean isChecked) {
                adapter.onToggleSelectAll(isChecked);
            }
        });
    }

    private void launchSearchMode(){
        RecyclerView iconListView = getView().findViewById(R.id.recyclerView);
        NestedScrollView notItemView = getView().findViewById(R.id.nsvNoItem);
        StartgazersAdapter adapter = (StartgazersAdapter) iconListView.getAdapter();

        final ViewYTranslator translatorDelegate = new AppBarAwareYTranslator();
        translatorDelegate.translateYWithAppBar(notItemView, drawerLayout.getAppBarLayout(), requireActivity());

        drawerLayout.startSearchMode(new ToolbarLayout.SearchModeListener() {
            @Override
            public boolean onQueryTextSubmit(@Nullable String query) {
                adapter.filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(@Nullable String newText) {
                adapter.filter(newText);
                return true;
            }

            @Override
            public void onSearchModeToggle(@NonNull SearchView searchView, boolean visible) {
                if (visible) {
                    searchView.setQueryHint( "Search icons");
                }else{
                    adapter.filter("");
                }
            }
        }, CLEAR_DISMISS);
    }


    private void showTipPopup(){
        if (!tipPopupShown) {
            RecyclerView iconListView = getView().findViewById(R.id.recyclerView);
            iconListView.postDelayed(() -> {
                View anchor = iconListView.getLayoutManager().findViewByPosition(0);
                if (anchor != null) {
                    TipPopup tipPopup = new TipPopup(anchor, TipPopup.MODE_TRANSLUCENT);
                    tipPopup.setMessage("Long-press item to trigger multi-selection.");
                    tipPopup.setAction("Close", view -> tipPopupShown = true);
                    tipPopup.setExpanded(true);
                    tipPopup.show(TipPopup.DIRECTION_DEFAULT);
                }
            }, 500);
        }
    }

    @Override
    public int getLayoutResId() {
        return R.layout.fragment_icons;
    }

    @Override
    public int getIconResId() {
        return dev.oneuiproject.oneui.R.drawable.ic_oui_creatures_outline;
    }

    @Override
    public CharSequence getTitle() {
        return "Icons";
    }

    private MenuProvider menuProvider = new MenuProvider() {
        @Override
        public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
            menuInflater.inflate(R.menu.menu_icons, menu);

            MenuItem searchItem = menu.findItem(R.id.menu_icons_search);
            drawerLayout.setMenuItemBadge((SeslMenuItem) searchItem, new ToolbarLayout.Badge.Dot());
        }

        @Override
        public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
            if (menuItem.getItemId() == R.id.menu_icons_search) {
                launchSearchMode();
                drawerLayout.setMenuItemBadge((SeslMenuItem) menuItem, new ToolbarLayout.Badge.None());
                return true;
            }
            return false;
        }
    };

}
