package org.edx.mobile.view;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.inject.Inject;
import com.joanzapata.iconify.IconDrawable;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragment;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.databinding.FragmentTabsBaseBinding;
import org.edx.mobile.model.FragmentItemModel;
import org.edx.mobile.view.adapters.FragmentItemPagerAdapter;

import java.util.List;

public abstract class TabsBaseFragment extends BaseFragment {
    @Inject
    protected IEdxEnvironment environment;

    @Nullable
    protected FragmentTabsBaseBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_tabs_base, container, false);
        initializeTabs();
        return binding.getRoot();
    }

    public void initializeTabs() {
        // Get fragment items list
        final List<FragmentItemModel> fragmentItems = getFragmentItems();
        // Init tabs
        final TabLayout tabLayout = binding.tabLayout;

        // No need to show tabs if we only have
        if (fragmentItems.size() <= 1) {
            tabLayout.setVisibility(View.GONE);
        } else {
            for (FragmentItemModel fragmentItem : fragmentItems) {
                tabLayout.addTab(createTab(tabLayout, fragmentItem));
            }
            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    binding.viewPager.setCurrentItem(tab.getPosition());
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {

                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {

                }
            });
        }
        // Init view pager
        final FragmentItemPagerAdapter adapter = new FragmentItemPagerAdapter(this.getActivity().getSupportFragmentManager(), fragmentItems);
        binding.viewPager.setAdapter(adapter);
        binding.viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout) {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                final FragmentItemModel item = fragmentItems.get(position);
                getActivity().setTitle(item.getTitle());
                if (item.getListener() != null) {
                    item.getListener().onFragmentSelected();
                }
            }
        });
        // It will load all of the fragments on creation and will stay in memory till ViewPager's
        // life time, it will greatly improve our user experience as all fragments will be available
        // to view all the time. We can decrease the limit if it creates memory problems on low-end devices.
        binding.viewPager.setOffscreenPageLimit(fragmentItems.size() - 1);
    }

    private TabLayout.Tab createTab(TabLayout tabLayout, FragmentItemModel fragmentItem) {
        final IconDrawable iconDrawable = new IconDrawable(getContext(), fragmentItem.getIcon());
        iconDrawable.colorRes(getContext(), R.color.edx_brand_primary_base);
        final TabLayout.Tab tab;
        tab = tabLayout.newTab();
        if (showTitleWithTabs()) {
            iconDrawable.sizeRes(getContext(), R.dimen.edx_small);
            final View tabItem = LayoutInflater.from(getContext()).inflate(R.layout.tab_item, null);
            final TextView title = (TextView) tabItem.findViewById(R.id.title);
            final ImageView icon = (ImageView) tabItem.findViewById(R.id.icon);
            title.setText(fragmentItem.getTitle());
            icon.setImageDrawable(iconDrawable);
            tab.setCustomView(tabItem);
        } else {
            tab.setIcon(iconDrawable);
        }
        tab.setContentDescription(fragmentItem.getTitle());
        return tab;
    }

    protected abstract boolean showTitleWithTabs();

    public abstract List<FragmentItemModel> getFragmentItems();
}
