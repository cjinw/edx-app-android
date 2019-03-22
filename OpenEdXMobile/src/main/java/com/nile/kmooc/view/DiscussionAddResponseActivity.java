package com.nile.kmooc.view;

import android.support.v4.app.Fragment;

import com.google.inject.Inject;

import com.nile.kmooc.base.BaseSingleFragmentActivity;

public class DiscussionAddResponseActivity extends BaseSingleFragmentActivity {
    @Inject
    DiscussionAddResponseFragment discussionAddResponseFragment;

    @Override
    public Fragment getFirstFragment() {
        discussionAddResponseFragment.setArguments(getIntent().getExtras());
        return discussionAddResponseFragment;
    }
}
