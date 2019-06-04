package com.nile.kmooc.view;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.google.inject.Inject;

import com.nile.kmooc.R;
import com.nile.kmooc.base.BaseFragmentActivity;
import com.nile.kmooc.databinding.ActivityLaunchBinding;
import com.nile.kmooc.module.analytics.Analytics;
import com.nile.kmooc.module.prefs.LoginPrefs;

public class LaunchActivity extends BaseFragmentActivity {

    @Inject
    LoginPrefs loginPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActivityLaunchBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_launch);
        binding.signInTv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(environment.getRouter().getLogInIntent());
            }
        });
        binding.signUpBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                environment.getAnalyticsRegistry().trackUserSignUpForAccount();
                startActivity(environment.getRouter().getRegisterIntent());
            }
        });
        environment.getAnalyticsRegistry().trackScreenView(Analytics.Screens.LAUNCH_ACTIVITY);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (environment.getLoginPrefs().getUsername() != null) {
            finish();
            environment.getRouter().showMainDashboard(this);
        }
    }
}
