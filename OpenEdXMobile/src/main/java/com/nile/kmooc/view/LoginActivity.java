package com.nile.kmooc.view;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;

import com.google.inject.Inject;

import com.nile.kmooc.BuildConfig;
import com.nile.kmooc.R;
import com.nile.kmooc.authentication.AuthResponse;
import com.nile.kmooc.authentication.LoginTask;
import com.nile.kmooc.databinding.ActivityLoginBinding;
import com.nile.kmooc.exception.LoginErrorMessage;
import com.nile.kmooc.exception.LoginException;
import com.nile.kmooc.http.HttpStatus;
import com.nile.kmooc.http.HttpStatusException;
import com.nile.kmooc.model.api.ProfileModel;
import com.nile.kmooc.module.analytics.Analytics;
import com.nile.kmooc.module.prefs.LoginPrefs;
import com.nile.kmooc.social.SocialFactory;
import com.nile.kmooc.social.SocialLoginDelegate;
import com.nile.kmooc.task.Task;
import com.nile.kmooc.util.AppStoreUtils;
import com.nile.kmooc.util.Config;
import com.nile.kmooc.util.IntentFactory;
import com.nile.kmooc.util.NetworkUtil;
import com.nile.kmooc.util.TextUtils;
import com.nile.kmooc.util.images.ErrorUtils;
import com.nile.kmooc.view.dialog.ResetPasswordDialogFragment;
import com.nile.kmooc.view.login.LoginPresenter;

import com.nhn.android.naverlogin.OAuthLogin;
import com.nhn.android.naverlogin.OAuthLoginHandler;
import com.nhn.android.naverlogin.ui.view.OAuthLoginButton;

import static com.nhn.android.naverlogin.OAuthLogin.mOAuthLoginHandler;

public class LoginActivity
        extends PresenterActivity<LoginPresenter, LoginPresenter.LoginViewInterface>
        implements SocialLoginDelegate.MobileLoginCallback {
    private SocialLoginDelegate socialLoginDelegate;
    private ActivityLoginBinding activityLoginBinding;

    @Inject
    LoginPrefs loginPrefs;

    @NonNull
    public static Intent newIntent() {
        return IntentFactory.newIntentForComponent(LoginActivity.class);
    }

    @NonNull
    @Override
    protected LoginPresenter createPresenter(@Nullable Bundle savedInstanceState) {
        return new LoginPresenter(
                environment.getConfig(),
                new NetworkUtil.ZeroRatedNetworkInfo(getApplicationContext(), environment.getConfig()));
    }

    @NonNull
    @Override
    protected LoginPresenter.LoginViewInterface createView(@Nullable Bundle savedInstanceState) {
        activityLoginBinding = DataBindingUtil.setContentView(this, R.layout.activity_login);

        hideSoftKeypad();
        socialLoginDelegate = new SocialLoginDelegate(this, savedInstanceState, this,
                environment.getConfig(), environment.getLoginPrefs(), SocialLoginDelegate.Feature.SIGN_IN);

        activityLoginBinding.socialAuth.facebookButton.getRoot().setOnClickListener(
                socialLoginDelegate.createSocialButtonClickHandler(
                        SocialFactory.SOCIAL_SOURCE_TYPE.TYPE_FACEBOOK));
        activityLoginBinding.socialAuth.googleButton.getRoot().setOnClickListener(
                socialLoginDelegate.createSocialButtonClickHandler(
                        SocialFactory.SOCIAL_SOURCE_TYPE.TYPE_GOOGLE));
        activityLoginBinding.socialAuth.naverButton.getRoot().setOnClickListener(
                socialLoginDelegate.createSocialButtonClickHandler(
                        SocialFactory.SOCIAL_SOURCE_TYPE.TYPE_NAVER));
        activityLoginBinding.socialAuth.kakaoButton.getRoot().setOnClickListener(
                socialLoginDelegate.createSocialButtonClickHandler(
                        SocialFactory.SOCIAL_SOURCE_TYPE.TYPE_KAKAO));

        activityLoginBinding.loginButtonLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check for Validation˜
                callServerForLogin();
            }
        });

        activityLoginBinding.forgotPasswordTv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Calling help dialog
                if (NetworkUtil.isConnected(LoginActivity.this)) {
                    showResetPasswordDialog();
                } else {
                    showAlertDialog(getString(R.string.reset_no_network_title), getString(R.string.network_not_connected));
                }
            }
        });

        activityLoginBinding.endUserAgreementTv.setMovementMethod(LinkMovementMethod.getInstance());
        activityLoginBinding.endUserAgreementTv.setText(TextUtils.generateLicenseText(getResources(), R.string.by_signing_in));

        environment.getAnalyticsRegistry().trackScreenView(Analytics.Screens.LOGIN);

        // enable login buttons at launch
        tryToSetUIInteraction(true);

        Config config = environment.getConfig();
        setTitle(getString(R.string.login_title));

        String envDisplayName = config.getEnvironmentDisplayName();
        if (envDisplayName != null && envDisplayName.length() > 0) {
            activityLoginBinding.versionEnvTv.setVisibility(View.VISIBLE);
            String versionName = BuildConfig.VERSION_NAME;
            String text = String.format("%s %s %s",
                    getString(R.string.label_version), versionName, envDisplayName);
            activityLoginBinding.versionEnvTv.setText(text);
        }

        return new LoginPresenter.LoginViewInterface() {
            @Override
            public void disableToolbarNavigation() {
                ActionBar actionBar = getSupportActionBar();
                if (actionBar != null) {
                    actionBar.setHomeButtonEnabled(false);
                    actionBar.setDisplayHomeAsUpEnabled(false);
                    actionBar.setDisplayShowHomeEnabled(false);
                }
            }

            @Override
            public void setSocialLoginButtons(boolean googleEnabled, boolean facebookEnabled, boolean naverEnabled, boolean kakaoEnabled) {
                if (!facebookEnabled && !googleEnabled && !naverEnabled && !kakaoEnabled) {
                    activityLoginBinding.panelLoginSocial.setVisibility(View.GONE);
                } else if (!facebookEnabled) {
                    activityLoginBinding.socialAuth.facebookButton.getRoot().setVisibility(View.GONE);
                } else if (!googleEnabled) {
                    activityLoginBinding.socialAuth.googleButton.getRoot().setVisibility(View.GONE);
                } else if (!naverEnabled) {
                    activityLoginBinding.socialAuth.naverButton.getRoot().setVisibility(View.GONE);
                } else if (!kakaoEnabled) {
                        activityLoginBinding.socialAuth.kakaoButton.getRoot().setVisibility(View.GONE);
                    }
            }
        };
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        socialLoginDelegate.onActivityDestroyed();
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("username", activityLoginBinding.emailEt.getText().toString().trim());

        socialLoginDelegate.onActivitySaveInstanceState(outState);

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (activityLoginBinding.emailEt.getText().toString().length() == 0) {
            displayLastEmailId();
        }

        socialLoginDelegate.onActivityStarted();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            activityLoginBinding.emailEt.setText(savedInstanceState.getString("username"));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        tryToSetUIInteraction(true);
        socialLoginDelegate.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case ResetPasswordDialogFragment.REQUEST_CODE: {
                if (resultCode == Activity.RESULT_OK) {
                    showAlertDialog(getString(R.string.success_dialog_title_help),
                            getString(R.string.success_dialog_message_help));
                }
                break;
            }
        }
    }

    private void displayLastEmailId() {
        activityLoginBinding.emailEt.setText(loginPrefs.getLastAuthenticatedEmail());
    }

    public void callServerForLogin() {
        if (!NetworkUtil.isConnected(this)) {
            showAlertDialog(getString(R.string.no_connectivity),
                    getString(R.string.network_not_connected));
            return;
        }

        final String emailStr = activityLoginBinding.emailEt.getText().toString().trim();
        final String passwordStr = activityLoginBinding.passwordEt.getText().toString().trim();

        if (activityLoginBinding.emailEt != null && emailStr.length() == 0) {
            showAlertDialog(getString(R.string.login_error),
                    getString(R.string.error_enter_email));
            activityLoginBinding.emailEt.requestFocus();
        } else if (activityLoginBinding.passwordEt != null && passwordStr.length() == 0) {
            showAlertDialog(getString(R.string.login_error),
                    getString(R.string.error_enter_password));
            activityLoginBinding.passwordEt.requestFocus();
        } else {
            activityLoginBinding.emailEt.setEnabled(false);
            activityLoginBinding.passwordEt.setEnabled(false);
            activityLoginBinding.forgotPasswordTv.setEnabled(false);
            activityLoginBinding.endUserAgreementTv.setEnabled(false);

            LoginTask logintask = new LoginTask(this, activityLoginBinding.emailEt.getText().toString().trim(),
                    activityLoginBinding.passwordEt.getText().toString()) {
                @Override
                public void onSuccess(@NonNull AuthResponse result) {
                    onUserLoginSuccess(result.profile);
                }

                @Override
                public void onException(Exception ex) {
                    if (ex instanceof HttpStatusException &&
                            ((HttpStatusException) ex).getStatusCode() == HttpStatus.UNAUTHORIZED) {
                        onUserLoginFailure(new LoginException(new LoginErrorMessage(
                                getString(R.string.login_error),
                                getString(R.string.login_failed))), null, null);
                    } else {
                        onUserLoginFailure(ex, null, null);
                    }
                }
            };
            tryToSetUIInteraction(false);
            logintask.setProgressDialog(activityLoginBinding.progress.progressIndicator);
            logintask.execute();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        socialLoginDelegate.onActivityStopped();
    }

    public String getEmail() {
        return activityLoginBinding.emailEt.getText().toString().trim();
    }

    private void showResetPasswordDialog() {
        ResetPasswordDialogFragment.newInstance(getEmail()).show(getSupportFragmentManager(), null);
    }

    // make sure that on the login activity, all errors show up as a dialog as opposed to a flying snackbar
    @Override
    public void showAlertDialog(@Nullable String header, @NonNull String message) {
        super.showAlertDialog(header, message);
    }


    /**
     * Starts fetching profile of the user after login by Facebook or Google.
     *
     * @param accessToken
     * @param backend
     */
    public void onSocialLoginSuccess(String accessToken, String backend, Task task) {
        tryToSetUIInteraction(false);
        task.setProgressDialog(activityLoginBinding.progress.progressIndicator);
    }

    public void onUserLoginSuccess(ProfileModel profile) {
        setResult(RESULT_OK);
        finish();
        if (!environment.getConfig().isRegistrationEnabled()) {
            environment.getRouter().showMainDashboard(this);
        }
    }

    public void onUserLoginFailure(Exception ex, String accessToken, String backend) {
        tryToSetUIInteraction(true);

        if (ex != null && ex instanceof LoginException) {
            LoginErrorMessage errorMessage = (((LoginException) ex).getLoginErrorMessage());
            showAlertDialog(
                    errorMessage.getMessageLine1(),
                    (errorMessage.getMessageLine2() != null) ?
                            errorMessage.getMessageLine2() : getString(R.string.login_failed));
        } else if (ex != null && ex instanceof HttpStatusException &&
                ((HttpStatusException) ex).getStatusCode() == HttpStatus.UPGRADE_REQUIRED) {
            LoginActivity.this.showAlertDialog(null,
                    getString(R.string.app_version_unsupported_login_msg),
                    getString(R.string.label_update),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            AppStoreUtils.openAppInAppStore(LoginActivity.this);
                        }
                    }, getString(android.R.string.cancel), null);
        } else {
            showAlertDialog(getString(R.string.login_error), ErrorUtils.getErrorMessage(ex, LoginActivity.this));
            logger.error(ex);
        }
    }

    @Override
    public boolean tryToSetUIInteraction(boolean enable) {
        if (enable) {
            unblockTouch();
            activityLoginBinding.loginButtonLayout.setEnabled(enable);
            activityLoginBinding.loginBtnTv.setText(getString(R.string.login));
        } else {
            blockTouch();
            activityLoginBinding.loginButtonLayout.setEnabled(enable);
            activityLoginBinding.loginBtnTv.setText(getString(R.string.signing_in));
        }


        activityLoginBinding.socialAuth.facebookButton.getRoot().setClickable(enable);
        activityLoginBinding.socialAuth.googleButton.getRoot().setClickable(enable);
        activityLoginBinding.socialAuth.naverButton.getRoot().setClickable(enable);
        activityLoginBinding.socialAuth.kakaoButton.getRoot().setClickable(enable);

        activityLoginBinding.emailEt.setEnabled(enable);
        activityLoginBinding.passwordEt.setEnabled(enable);

        activityLoginBinding.forgotPasswordTv.setEnabled(enable);
        activityLoginBinding.endUserAgreementTv.setEnabled(enable);

        return true;
    }
}