package com.nile.kmooc.view;

import com.nile.kmooc.R;

public class WebViewDiscoverProgramsFragment extends WebViewDiscoverFragment {
    @Override
    protected String getSearchUrl() {
        return environment.getConfig().getDiscoveryConfig().getProgramDiscoveryConfig().getBaseUrl();
    }

    @Override
    protected int getQueryHint() {
        return R.string.search_for_programs;
    }

    @Override
    protected boolean isSearchEnabled() {
        return environment.getConfig().getDiscoveryConfig().getProgramDiscoveryConfig().isSearchEnabled();
    }
}
