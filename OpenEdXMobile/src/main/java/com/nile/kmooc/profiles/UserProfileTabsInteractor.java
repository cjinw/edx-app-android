package com.nile.kmooc.profiles;

import android.support.annotation.NonNull;

import com.nile.kmooc.R;
import com.nile.kmooc.http.callback.Callback;
import com.nile.kmooc.interfaces.RefreshListener;
import com.nile.kmooc.model.Page;
import com.nile.kmooc.user.UserService;
import com.nile.kmooc.util.Config;
import com.nile.kmooc.util.observer.CachingObservable;
import com.nile.kmooc.util.observer.Observable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UserProfileTabsInteractor implements RefreshListener {

    @NonNull
    private final String username;

    @NonNull
    private final UserService userService;

    private final boolean isBadgesEnabled;

    @NonNull
    private final CachingObservable<List<UserProfileTab>> tabs = new CachingObservable<>();

    public UserProfileTabsInteractor(@NonNull String username, @NonNull final UserService userService, @NonNull Config config) {
        this.username = username;
        this.userService = userService;
        tabs.onData(builtInTabs());
        isBadgesEnabled = config.isBadgesEnabled();
        onRefresh();
    }

    @NonNull
    public Observable<List<UserProfileTab>> observeTabs() {
        return tabs;
    }


    private List<UserProfileTab> builtInTabs() {
        return Collections.singletonList(new UserProfileTab(R.string.profile_tab_bio, UserProfileBioFragment.class));
    }

    private void handleBadgesLoaded(@NonNull Page<BadgeAssertion> badges) {
        if (badges.getCount() == 0) {
            return;
        }
        final List<UserProfileTab> knownTabs = new ArrayList<>();
        knownTabs.addAll(builtInTabs());
        knownTabs.add(new UserProfileTab(R.string.profile_tab_accomplishment, UserProfileAccomplishmentsFragment.class));
        tabs.onData(knownTabs);
    }

    @Override
    public void onRefresh() {
        if (isBadgesEnabled) {
            userService.getBadges(UserProfileTabsInteractor.this.username, 1)
                    .enqueue(new Callback<Page<BadgeAssertion>>() {
                        @Override
                        protected void onResponse(@NonNull Page<BadgeAssertion> badges) {
                            handleBadgesLoaded(badges);
                        }

                        @Override
                        protected void onFailure(@NonNull Throwable error) {
                            // do nothing. Better to just deal show what we can
                        }
                    });
        }
    }
}
