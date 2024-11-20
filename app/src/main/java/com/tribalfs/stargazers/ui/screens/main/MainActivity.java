package com.tribalfs.stargazers.ui.screens.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

import dev.oneuiproject.oneui.layout.DrawerLayout;
import dev.oneuiproject.oneui.layout.ToolbarLayout.Badge;
import dev.oneuiproject.oneui.widget.ScrollAwareFloatingActionButton;

import com.tribalfs.stargazers.R;
import com.tribalfs.stargazers.databinding.ActivityMainBinding;
import com.tribalfs.stargazers.ui.core.base.FragmentInfo;
import com.tribalfs.stargazers.ui.screens.customabout.CustomAboutActivity;
import com.tribalfs.stargazers.ui.screens.main.stargazerslist.StargazersListFragment;
import com.tribalfs.stargazers.ui.core.drawer.DrawerListAdapter;

public class MainActivity extends AppCompatActivity
        implements DrawerListAdapter.DrawerListener {
    private ActivityMainBinding mBinding;
    private FragmentManager mFragmentManager;
    private final List<Fragment> fragments = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        setupSharedElementAnimation();
        mBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        initFragmentList();
        initDrawer();
        initFragments();
        initOnBackPressed();
    }


    private void setupSharedElementAnimation() {
        Window window = getWindow();
        window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
    }

    private void initFragmentList() {
        fragments.add(new StargazersListFragment());
//        fragments.add(null);
//        fragments.add(new IconsFragment());
//        fragments.add(new AppPickerFragment());
    }

    private OnBackPressedCallback mBackPressedCallback = new OnBackPressedCallback(false) {
        @Override
        public void handleOnBackPressed() {
            onDrawerItemSelected(0);
            ((DrawerListAdapter)mBinding.drawerListView.getAdapter()).setSelectedItem(0);
        }
    };

    private void initOnBackPressed() {
        getOnBackPressedDispatcher().addCallback(this, mBackPressedCallback);
    }

    private void initDrawer() {
        mBinding.drawerLayout.setDrawerButtonIcon(getDrawable(dev.oneuiproject.oneui.R.drawable.ic_oui_info_outline));
        mBinding.drawerLayout.setDrawerButtonTooltip("App info");
        mBinding.drawerLayout.setDrawerButtonOnClickListener(v -> {
            startActivity( new Intent(MainActivity.this, CustomAboutActivity.class));
            mBinding.drawerLayout.setDrawerButtonBadge(new Badge.None());
        });

        mBinding.drawerListView.setLayoutManager(new LinearLayoutManager(this));
        mBinding.drawerListView.setAdapter(new DrawerListAdapter(this, fragments, this));
        mBinding.drawerListView.setItemAnimator(null);
        mBinding.drawerListView.setHasFixedSize(true);
        mBinding.drawerListView.seslSetLastRoundedCorner(false);
        mBinding.drawerLayout.setButtonBadges(new Badge.Dot(), new Badge.Dot());
        mBinding.drawerLayout.setDrawerStateListener((state) -> {
            if (state == DrawerLayout.DrawerState.OPEN) {
                mBinding.drawerLayout.setNavigationButtonBadge(new Badge.None());
            }
            return null;
        });
    }

    private void initFragments() {
        mFragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        for (Fragment fragment : fragments) {
            if (fragment != null) transaction.add(R.id.main_content, fragment);
        }
        transaction.commit();
        mFragmentManager.executePendingTransactions();

        onDrawerItemSelected(0);
    }

    @Override
    public boolean onDrawerItemSelected(int position) {
        Fragment newFragment = fragments.get(position);
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        for (Fragment fragment : mFragmentManager.getFragments()) {
            transaction.hide(fragment);
        }
        transaction.show(newFragment).commit();

        if (newFragment instanceof FragmentInfo) {
            if (!((FragmentInfo) newFragment).isAppBarEnabled()) {
                mBinding.drawerLayout.setExpanded(false, false);
                mBinding.drawerLayout.setExpandable(false);
            } else {
                mBinding.drawerLayout.setExpandable(true);
                mBinding.drawerLayout.setExpanded(false, false);
            }
            mBinding.drawerLayout.setTitle(((FragmentInfo) newFragment).getTitle());
            if (newFragment instanceof StargazersListFragment) {
                mBinding.drawerLayout.setExpandedSubtitle("Pull down to refresh");
            }
        }
        mBinding.drawerLayout.setDrawerOpen(false, true);

        mBackPressedCallback.setEnabled(position != 0);

        return true;
    }

    public DrawerLayout getDrawerLayout() {
        return mBinding.drawerLayout;
    }

    public ScrollAwareFloatingActionButton getFab(){
        return mBinding.fab;
    }
}
