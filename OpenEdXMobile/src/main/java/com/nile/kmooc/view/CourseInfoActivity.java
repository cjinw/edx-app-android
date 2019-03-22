package com.nile.kmooc.view;

import android.os.Bundle;

import com.nile.kmooc.R;
import com.nile.kmooc.base.BaseWebViewFindCoursesActivity;
import com.nile.kmooc.module.analytics.Analytics;

import roboguice.inject.ContentView;

@ContentView(R.layout.activity_find_course_info)
public class CourseInfoActivity extends BaseWebViewFindCoursesActivity {

    public static final String EXTRA_PATH_ID = "path_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        environment.getAnalyticsRegistry().trackScreenView(Analytics.Screens.COURSE_INFO_SCREEN);
    }

    @Override
    protected void onStart() {
        super.onStart();

        final String pathId = getIntent().getStringExtra(EXTRA_PATH_ID).replaceAll("\\p{Space}" , "+");
        final String url = environment.getConfig().getCourseDiscoveryConfig()
                .getCourseInfoUrlTemplate()
                .replace("{" + EXTRA_PATH_ID + "}", pathId);
        loadUrl(url);
    }

    @Override
    public void onResume() {
        super.onResume();
        AuthPanelUtils.configureAuthPanel(findViewById(R.id.auth_panel), environment);
    }

    @Override
    protected boolean isAllLinksExternal() {
        // treat all links on this screen as external links, so that they open in external browser
        return true;
    }
}
