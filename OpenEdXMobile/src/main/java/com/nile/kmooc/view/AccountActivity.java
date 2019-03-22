package com.nile.kmooc.view;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;

import com.nile.kmooc.base.BaseSingleFragmentActivity;
import com.nile.kmooc.logger.Logger;
import com.nile.kmooc.util.Config;

import javax.inject.Inject;

public class AccountActivity extends BaseSingleFragmentActivity {
    protected Logger logger = new Logger(getClass().getSimpleName());

    @Inject
    private Config config;

    public static Intent newIntent(Activity activity) {
        return new Intent(activity, AccountActivity.class);
    }

    @Override
    public Fragment getFirstFragment() {
        return new AccountFragment();
    }
}
