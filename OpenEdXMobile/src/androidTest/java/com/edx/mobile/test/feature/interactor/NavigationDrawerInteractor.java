package com.edx.mobile.test.feature.interactor;

import com.nile.kmooc.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.nile.kmooc.test.feature.matcher.ActionBarMatcher.isInActionBar;
import static org.hamcrest.CoreMatchers.allOf;

public class NavigationDrawerInteractor {
    public static NavigationDrawerInteractor open() {
        onView(allOf(isInActionBar(), withContentDescription("dummy string"))).perform(click());
        return new NavigationDrawerInteractor();
    }

    public LogInScreenInteractor logOut() {
        onView(withText(R.string.logout)).perform(click());
        return new LogInScreenInteractor();
    }
}
