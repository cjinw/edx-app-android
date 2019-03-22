package com.nile.kmooc.view;

import android.support.v4.app.Fragment;

import com.nile.kmooc.R;
import com.nile.kmooc.base.BaseSingleFragmentActivity;

public class CourseHandoutActivity extends BaseSingleFragmentActivity {
    private Fragment fragment;

    @Override
    protected void onStart() {
        super.onStart();
        setTitle(getString(R.string.tab_label_handouts));
    }

    @Override
    public Fragment getFirstFragment() {
        return new CourseHandoutFragment();
    }
}
