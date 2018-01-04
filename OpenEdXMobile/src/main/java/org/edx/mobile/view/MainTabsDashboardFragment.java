package org.edx.mobile.view;

import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import org.edx.mobile.R;
import org.edx.mobile.model.FragmentItemModel;
import org.edx.mobile.module.analytics.Analytics;

import java.util.ArrayList;
import java.util.List;

public class MainTabsDashboardFragment extends TabsBaseFragment {
    @Override
    protected boolean showTitleWithTabs() {
        return true;
    }

    @Override
    public List<FragmentItemModel> getFragmentItems() {
        ArrayList<FragmentItemModel> items = new ArrayList<>();

        items.add(new FragmentItemModel(MyCoursesListFragment.class,
                getResources().getString(R.string.label_my_courses), FontAwesomeIcons.fa_list_alt,
                new FragmentItemModel.FragmentStateListener() {
                    @Override
                    public void onFragmentSelected() {
                        environment.getAnalyticsRegistry().trackScreenView(Analytics.Screens.MY_COURSES);
                    }
                }));

        if (environment.getConfig().getCourseDiscoveryConfig().isCourseDiscoveryEnabled()) {
            items.add(new FragmentItemModel(WebViewDiscoverCoursesFragment.class,
                    getResources().getString(R.string.label_discover), FontAwesomeIcons.fa_search,
                    new FragmentItemModel.FragmentStateListener() {
                        @Override
                        public void onFragmentSelected() {
                            environment.getAnalyticsRegistry().trackScreenView(Analytics.Screens.FIND_COURSES);
                        }
                    }));
        }

        return items;
    }
}
