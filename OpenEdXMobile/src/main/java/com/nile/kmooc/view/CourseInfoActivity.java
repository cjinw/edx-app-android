package com.nile.kmooc.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.nile.kmooc.R;
import com.nile.kmooc.base.BaseSingleFragmentActivity;
import com.nile.kmooc.base.WebViewCourseInfoFragment;
import com.nile.kmooc.module.analytics.Analytics;

public class CourseInfoActivity extends BaseSingleFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        environment.getAnalyticsRegistry().trackScreenView(Analytics.Screens.COURSE_INFO_SCREEN);
    }

    @Override
    public void onResume() {
        super.onResume();
        AuthPanelUtils.configureAuthPanel(findViewById(R.id.auth_panel), environment);
    }

    @Override
    public Fragment getFirstFragment() {
        final WebViewCourseInfoFragment fragment = new WebViewCourseInfoFragment();
        fragment.setArguments(getIntent().getExtras());
        return fragment;
    }
}
