package com.nile.kmooc.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.nile.kmooc.base.BaseSingleFragmentActivity;
import com.nile.kmooc.base.WebViewProgramInfoFragment;
import com.nile.kmooc.module.analytics.Analytics;

public class ProgramInfoActivity extends BaseSingleFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        environment.getAnalyticsRegistry().trackScreenView(Analytics.Screens.PROGRAM_INFO_SCREEN);
    }

    @Override
    public Fragment getFirstFragment() {
        final WebViewProgramInfoFragment fragment = new WebViewProgramInfoFragment();
        fragment.setArguments(getIntent().getExtras());
        return fragment;
    }
}
