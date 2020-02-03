package com.nile.kmooc.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.nile.kmooc.base.MainApplication;
import com.nile.kmooc.core.IEdxEnvironment;
import com.nile.kmooc.deeplink.DeepLinkManager;
import com.nile.kmooc.logger.Logger;
import com.nile.kmooc.util.Config;
import com.nile.kmooc.util.NetworkUtil;
import org.json.JSONException;

import io.branch.referral.Branch;

// We are extending the normal Activity class here so that we can use Theme.NoDisplay, which does not support AppCompat activities
public class SplashActivity extends Activity {
    protected final Logger logger = new Logger(getClass().getName());
    private Config config = new Config(MainApplication.instance());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!Config.FabricBranchConfig.isBranchEnabled(config.getFabricConfig())) {
            finish();
        }

        /*
        Recommended solution to avoid opening of multiple tasks of our app's launcher activity.
        For more info:
        - https://issuetracker.google.com/issues/36907463
        - https://stackoverflow.com/questions/4341600/how-to-prevent-multiple-instances-of-an-activity-when-it-is-launched-with-differ/
        - https://stackoverflow.com/questions/16283079/re-launch-of-activity-on-home-button-but-only-the-first-time/16447508#16447508
         */
        if (!isTaskRoot()) {
            final Intent intent = getIntent();
            if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && Intent.ACTION_MAIN.equals(intent.getAction())) {
                return;
            }
        }

        final IEdxEnvironment environment = MainApplication.getEnvironment(this);
        if (environment.getUserPrefs().getProfile() != null) {
            environment.getRouter().showMainDashboard(SplashActivity.this);
        } else if (!environment.getConfig().isRegistrationEnabled()) {
            startActivity(environment.getRouter().getLogInIntent());
        } else {
            environment.getRouter().showLaunchScreen(SplashActivity.this);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (Config.FabricBranchConfig.isBranchEnabled(config.getFabricConfig())) {
            Branch.getInstance().initSession((referringParams, error) -> {
                if (error == null) {
                    // params are the deep linked params associated with the link that the user
                    // clicked -> was re-directed to this app params will be empty if no data found
                    if (referringParams.optBoolean(DeepLinkManager.KEY_CLICKED_BRANCH_LINK)) {
                        try {
                            DeepLinkManager.parseAndReact(SplashActivity.this, referringParams);
                        } catch (JSONException e) {
                            logger.error(e, true);
                        }
                    }
                } else {
                    // Ignore the logging of errors occurred due to lack of network connectivity
                    if (NetworkUtil.isConnected(getApplicationContext())) {
                        logger.error(new Exception("Branch not configured properly, error:\n"
                                + error.getMessage()), true);
                    }
                }
            }, this.getIntent().getData(), this);

            finish();
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        this.setIntent(intent);
    }
}