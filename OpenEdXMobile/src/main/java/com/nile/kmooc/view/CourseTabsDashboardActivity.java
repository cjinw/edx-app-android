package com.nile.kmooc.view;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.nile.kmooc.deeplink.ScreenDef;
import com.nile.kmooc.event.CourseDashboardRefreshEvent;
import com.nile.kmooc.model.api.EnrolledCoursesResponse;

import static com.nile.kmooc.view.Router.EXTRA_ANNOUNCEMENTS;
import static com.nile.kmooc.view.Router.EXTRA_COURSE_DATA;
import static com.nile.kmooc.view.Router.EXTRA_COURSE_ID;
import static com.nile.kmooc.view.Router.EXTRA_DISCUSSION_THREAD_ID;
import static com.nile.kmooc.view.Router.EXTRA_DISCUSSION_TOPIC_ID;
import static com.nile.kmooc.view.Router.EXTRA_SCREEN_NAME;

public class CourseTabsDashboardActivity extends OfflineSupportBaseActivity {
    public static Intent newIntent(@NonNull Activity activity,
                                   @Nullable EnrolledCoursesResponse courseData,
                                   @Nullable String courseId,
                                   @Nullable String topicId,
                                   @Nullable String threadId, boolean announcements,
                                   @Nullable @ScreenDef String screenName) {
        final Intent intent = new Intent(activity, CourseTabsDashboardActivity.class);
        intent.putExtra(EXTRA_COURSE_DATA, courseData);
        intent.putExtra(EXTRA_COURSE_ID, courseId);
        intent.putExtra(EXTRA_DISCUSSION_TOPIC_ID, topicId);
        intent.putExtra(EXTRA_DISCUSSION_THREAD_ID, threadId);
        intent.putExtra(EXTRA_ANNOUNCEMENTS, announcements);
        intent.putExtra(EXTRA_SCREEN_NAME, screenName);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        return intent;
    }

    @Override
    public Fragment getFirstFragment() {
        return CourseTabsDashboardFragment.newInstance(getIntent().getExtras());
    }

    @Override
    public Object getRefreshEvent() {
        return new CourseDashboardRefreshEvent();
    }
}
