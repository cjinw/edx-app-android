package com.nile.kmooc.login;

import android.view.View;

import com.nile.kmooc.R;
import com.nile.kmooc.view.LoginActivity;
import com.nile.kmooc.view.PresenterActivityTest;
import com.nile.kmooc.view.login.LoginPresenter;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LoginActivityTest extends PresenterActivityTest<LoginActivity, LoginPresenter, LoginPresenter.LoginViewInterface> {

    @Before
    public void setup() {
        startActivity(LoginActivity.newIntent());
    }

    @Test
    public void testSetSocialLoginButtons_withFacebookEnabled_facebookButtonIsVisible() {
        view.setSocialLoginButtons(false, true, true, true);
        assertThat(activity.findViewById(R.id.panel_login_social).getVisibility()).isEqualTo(View.VISIBLE);
        assertThat(activity.findViewById(R.id.google_button).getVisibility()).isNotEqualTo(View.VISIBLE);
        assertThat(activity.findViewById(R.id.facebook_button).getVisibility()).isEqualTo(View.VISIBLE);
    }

    @Test
    public void testSetSocialLoginButtons_withGoogleEnabled_googleButtonIsVisible() {
        view.setSocialLoginButtons(true, false, true, true);
        assertThat(activity.findViewById(R.id.panel_login_social).getVisibility()).isEqualTo(View.VISIBLE);
        assertThat(activity.findViewById(R.id.google_button).getVisibility()).isEqualTo(View.VISIBLE);
        assertThat(activity.findViewById(R.id.facebook_button).getVisibility()).isNotEqualTo(View.VISIBLE);
    }

    @Test
    public void testSetSocialLoginButtons_withSocialLoginEnabled_socialLoginButtonsAreVisible() {
        view.setSocialLoginButtons(true, true, true, true);
        assertThat(activity.findViewById(R.id.panel_login_social).getVisibility()).isEqualTo(View.VISIBLE);
        assertThat(activity.findViewById(R.id.google_button).getVisibility()).isEqualTo(View.VISIBLE);
        assertThat(activity.findViewById(R.id.facebook_button).getVisibility()).isEqualTo(View.VISIBLE);
    }

    @Test
    public void testSetSocialLoginButtons_withSocialLoginNotEnabled_socialLoginButtonsNotVisible() {
        view.setSocialLoginButtons(false, false, true, true);
        assertThat(activity.findViewById(R.id.panel_login_social).getVisibility()).isNotEqualTo(View.VISIBLE);
    }
}
