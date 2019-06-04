package com.nile.kmooc.view;

import android.support.v4.app.Fragment;

import com.google.inject.Inject;

import com.nile.kmooc.base.BaseSingleFragmentActivity;

public class CourseDiscussionResponsesActivity extends BaseSingleFragmentActivity {

    @Inject
    CourseDiscussionResponsesFragment courseDiscussionResponsesFragment;

    @Override
    public Fragment getFirstFragment() {
        courseDiscussionResponsesFragment.setArguments(getIntent().getExtras());
        courseDiscussionResponsesFragment.setRetainInstance(true);

        return courseDiscussionResponsesFragment;
    }
}
