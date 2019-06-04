package com.edx.mobile.login;

import com.nile.kmooc.test.PresenterTest;
import com.nile.kmooc.util.Config;
import com.nile.kmooc.util.NetworkUtil;
import com.nile.kmooc.view.login.LoginPresenter;
import org.junit.Test;
import org.mockito.Mock;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LoginPresenterTest extends PresenterTest<LoginPresenter, LoginPresenter.LoginViewInterface> {

    @Mock
    NetworkUtil.ZeroRatedNetworkInfo zeroRatedNetworkInfo;

    @Mock
    Config config;

    @Test
    public void testOnViewCreation_withGoogleLoginDisabled_googleButtonNotVisible() {
        when(config.getGoogleConfig()).thenReturn(new Config.GoogleConfig(false));
        when(config.getFacebookConfig()).thenReturn(new Config.FacebookConfig(false, "dummy app id"));
        startPresenter(new LoginPresenter(config, zeroRatedNetworkInfo));
        verify(view).setSocialLoginButtons(false, false);
    }

    @Test
    public void testOnViewCreation_withFacebookLoginDisabled_facebookButtonNotVisible() {
        when(config.getGoogleConfig()).thenReturn(new Config.GoogleConfig(false));
        when(config.getFacebookConfig()).thenReturn(new Config.FacebookConfig(true, "dummy app id"));
        startPresenter(new LoginPresenter(config, zeroRatedNetworkInfo));
        verify(view).setSocialLoginButtons(false, true);
    }

    @Test
    public void testOnViewCreation_withGoogleFacebookEnabled_socialLoginButtonsVisible() {
        when(config.getGoogleConfig()).thenReturn(new Config.GoogleConfig(true));
        when(config.getFacebookConfig()).thenReturn(new Config.FacebookConfig(false, "dummy app id"));
        startPresenter(new LoginPresenter(config, zeroRatedNetworkInfo));
        verify(view).setSocialLoginButtons(true, false);
    }

    @Test
    public void testOnViewCreation_withZeroRatedNetwork_socialLoginButtonsNotVisible() {
        when(zeroRatedNetworkInfo.isOnZeroRatedNetwork()).thenReturn(true);
        when(config.getGoogleConfig()).thenReturn(new Config.GoogleConfig(true));
        when(config.getFacebookConfig()).thenReturn(new Config.FacebookConfig(true, "dummy app id"));
        startPresenter(new LoginPresenter(config, zeroRatedNetworkInfo));
        verify(view).setSocialLoginButtons(false, false);
    }
}
