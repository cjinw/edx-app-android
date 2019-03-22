package com.nile.kmooc.view;

import android.support.v4.app.Fragment;

import com.google.inject.Inject;

import com.nile.kmooc.base.BaseSingleFragmentActivity;

public class DiscussionAddPostActivity extends BaseSingleFragmentActivity {
    @Inject
    DiscussionAddPostFragment discussionAddPostFragment;

    @Override
    public Fragment getFirstFragment() {
        discussionAddPostFragment.setArguments(getIntent().getExtras());
        return discussionAddPostFragment;
    }
}
