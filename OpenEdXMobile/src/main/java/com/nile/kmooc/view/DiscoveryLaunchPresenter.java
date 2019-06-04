package com.nile.kmooc.view;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.nile.kmooc.module.prefs.LoginPrefs;
import com.nile.kmooc.util.Config;

public class DiscoveryLaunchPresenter extends ViewHoldingPresenter<DiscoveryLaunchPresenter.ViewInterface> {

    @NonNull
    private final LoginPrefs loginPrefs;

    @Nullable
    private final Config.CourseDiscoveryConfig courseDiscoveryConfig;

    public DiscoveryLaunchPresenter(@NonNull LoginPrefs loginPrefs, @NonNull Config.CourseDiscoveryConfig courseDiscoveryConfig) {
        this.loginPrefs = loginPrefs;
        this.courseDiscoveryConfig = courseDiscoveryConfig;
    }

    @Override
    public void attachView(@NonNull ViewInterface view) {
        super.attachView(view);
        view.setEnabledButtons(courseDiscoveryConfig != null && courseDiscoveryConfig.isDiscoveryEnabled());
    }

    public void onResume() {
        assert getView() != null;
        if (loginPrefs.getUsername() != null) {
            getView().navigateToMyCourses();
        }
    }

    public interface ViewInterface {
        void setEnabledButtons(boolean courseDiscoveryEnabled);

        void navigateToMyCourses();
    }
}
