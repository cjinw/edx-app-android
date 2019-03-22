package com.nile.kmooc.view;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import com.nile.kmooc.event.CourseDashboardRefreshEvent;
import com.nile.kmooc.model.api.EnrolledCoursesResponse;

import static com.nile.kmooc.view.Router.EXTRA_ANNOUNCEMENTS;
import static com.nile.kmooc.view.Router.EXTRA_COURSE_DATA;

public class CourseTabsDashboardActivity extends OfflineSupportBaseActivity {
    public static Intent newIntent(@NonNull Activity activity,
                                   @NonNull EnrolledCoursesResponse courseData,
                                   boolean announcements) {
        Intent intent = new Intent(activity, CourseTabsDashboardActivity.class);
        intent.putExtra(EXTRA_COURSE_DATA, courseData);
        intent.putExtra(EXTRA_ANNOUNCEMENTS, announcements);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        return intent;
    }

    @Override
    public Fragment getFirstFragment() {
        return CourseTabsDashboardFragment.newInstance(
                (EnrolledCoursesResponse) getIntent().getExtras().getSerializable(EXTRA_COURSE_DATA));
    }

    @Override
    public Object getRefreshEvent() {
        return new CourseDashboardRefreshEvent();
    }
}
