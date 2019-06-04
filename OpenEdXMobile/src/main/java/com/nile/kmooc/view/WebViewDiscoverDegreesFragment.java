package com.nile.kmooc.view;

import com.nile.kmooc.R;

public class WebViewDiscoverDegreesFragment extends WebViewDiscoverFragment {
    @Override
    protected String getSearchUrl() {
        return environment.getConfig().getDiscoveryConfig().getDegreeDiscoveryConfig().getBaseUrl();
    }

    @Override
    protected int getQueryHint() {
        return R.string.search_for_degrees;
    }

    @Override
    protected boolean isSearchEnabled() {
        return environment.getConfig().getDiscoveryConfig().getDegreeDiscoveryConfig().isSearchEnabled();
    }
}
