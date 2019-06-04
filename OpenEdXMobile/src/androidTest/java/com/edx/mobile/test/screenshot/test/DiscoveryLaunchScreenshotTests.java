package com.edx.mobile.test.screenshot.test;

import com.nile.kmooc.view.DiscoveryLaunchActivity;
import com.nile.kmooc.view.DiscoveryLaunchPresenter;
import com.nile.kmooc.view.PresenterActivityScreenshotTest;
import org.junit.Test;

public class DiscoveryLaunchScreenshotTests extends PresenterActivityScreenshotTest<DiscoveryLaunchActivity, DiscoveryLaunchPresenter, DiscoveryLaunchPresenter.ViewInterface> {

    @Test
    public void testScreenshot_withCourseDiscoveryDisabled() {
        view.setEnabledButtons(false);
    }

    @Test
    public void testScreenshot_withCourseDiscoveryEnabled() {
        view.setEnabledButtons(true);
    }
}
