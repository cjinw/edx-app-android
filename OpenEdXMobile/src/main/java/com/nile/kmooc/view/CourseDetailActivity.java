/*
 * CourseDetailActivity
 *
 * Activity that holds the fragments related to the course detail.
 */

package com.nile.kmooc.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import com.nile.kmooc.base.BaseSingleFragmentActivity;
import com.nile.kmooc.course.CourseDetail;
import com.nile.kmooc.module.analytics.Analytics;

public class CourseDetailActivity extends BaseSingleFragmentActivity {

    public static Intent newIntent(@NonNull Context context, @NonNull CourseDetail courseDetail) {
        return new Intent(context, CourseDetailActivity.class)
                .putExtra(CourseDetailFragment.COURSE_DETAIL, courseDetail);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        environment.getAnalyticsRegistry().trackScreenView(Analytics.Screens.COURSE_INFO_SCREEN);
    }

    @Override
    public Fragment getFirstFragment() {
        return new CourseDetailFragment();
    }
}
