package com.nile.kmooc.login;

import com.nile.kmooc.R;
import com.nile.kmooc.view.LoginActivity;
import com.nile.kmooc.view.PresenterActivityTest;
import com.nile.kmooc.view.login.LoginPresenter;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.android.api.Assertions.assertThat;

public class LoginActivityTest extends PresenterActivityTest<LoginActivity, LoginPresenter, LoginPresenter.LoginViewInterface> {

    @Before
    public void setup() {
        startActivity(LoginActivity.newIntent());
    }

    @Test
    public void testSetSocialLoginButtons_withFacebookEnabled_facebookButtonIsVisible() {
        view.setSocialLoginButtons(false, true, true, true);
        assertThat(activity.findViewById(R.id.panel_login_social)).isVisible();
        assertThat(activity.findViewById(R.id.google_button)).isNotVisible();
        assertThat(activity.findViewById(R.id.facebook_button)).isVisible();
    }

    @Test
    public void testSetSocialLoginButtons_withGoogleEnabled_googleButtonIsVisible() {
        view.setSocialLoginButtons(true, false, true, true);
        assertThat(activity.findViewById(R.id.panel_login_social)).isVisible();
        assertThat(activity.findViewById(R.id.google_button)).isVisible();
        assertThat(activity.findViewById(R.id.facebook_button)).isNotVisible();
    }

    @Test
    public void testSetSocialLoginButtons_withSocialLoginEnabled_socialLoginButtonsAreVisible() {
        view.setSocialLoginButtons(true, true, true, true);
        assertThat(activity.findViewById(R.id.panel_login_social)).isVisible();
        assertThat(activity.findViewById(R.id.google_button)).isVisible();
        assertThat(activity.findViewById(R.id.facebook_button)).isVisible();
    }

    @Test
    public void testSetSocialLoginButtons_withSocialLoginNotEnabled_socialLoginButtonsNotVisible() {
        view.setSocialLoginButtons(false, false, true, true);
        assertThat(activity.findViewById(R.id.panel_login_social)).isNotVisible();
    }
}
