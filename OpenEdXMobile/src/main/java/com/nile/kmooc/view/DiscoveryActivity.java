package com.nile.kmooc.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nile.kmooc.R;
import com.nile.kmooc.base.BaseSingleFragmentActivity;
import com.nile.kmooc.module.analytics.Analytics;

import static com.nile.kmooc.view.Router.EXTRA_PATH_ID;
import static com.nile.kmooc.view.Router.EXTRA_SCREEN_NAME;

public class DiscoveryActivity extends BaseSingleFragmentActivity implements ToolbarCallbacks {
    public static Intent newIntent(@NonNull Context context) {
        return new Intent(context, DiscoveryActivity.class);
    }

    public static Intent newIntent(@NonNull Context context, @Nullable String screenName,
                                   @Nullable String pathId) {
        final Intent intent = new Intent(context, DiscoveryActivity.class);
        intent.putExtra(EXTRA_SCREEN_NAME, screenName);
        intent.putExtra(EXTRA_PATH_ID, pathId);
        return intent;
    }

    @Override
    public Fragment getFirstFragment() {
        final Fragment fragment = new MainDiscoveryFragment();
        fragment.setArguments(getIntent().getExtras());
        return fragment;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.label_discover);
        environment.getAnalyticsRegistry().trackScreenView(Analytics.Screens.FIND_COURSES);
    }

    @Override
    public void onResume() {
        super.onResume();
        AuthPanelUtils.configureAuthPanel(findViewById(R.id.auth_panel), environment);
    }

    @Override
    protected int getToolbarLayoutId() {
        return R.layout.toolbar_with_profile_button;
    }


    @Override
    public void setTitle(int titleId) {
        setTitle(getResources().getString(titleId));
    }

    @Override
    public void setTitle(CharSequence title) {
        final View toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            final TextView titleView = getTitleView();
            if (titleView != null) {
                titleView.setText(title);
            }
        }
        super.setTitle(title);
    }

    @Override
    @Nullable
    public SearchView getSearchView() {
        final View searchView = findViewById(R.id.toolbar_search_view);
        if (searchView != null && searchView instanceof SearchView) {
            return (SearchView) searchView;
        }
        return null;
    }

    @Override
    @Nullable
    public TextView getTitleView() {
        final View titleView = findViewById(R.id.toolbar_title_view);
        if (titleView != null && titleView instanceof TextView) {
            return (TextView) titleView;
        }
        return null;
    }

    @Nullable
    @Override
    public ImageView getProfileView() {
        return null;
    }
}
