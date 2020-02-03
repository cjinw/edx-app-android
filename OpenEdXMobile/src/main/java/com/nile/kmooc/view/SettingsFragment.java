package com.nile.kmooc.view;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;

import com.google.inject.Inject;

import com.nile.kmooc.R;
import com.nile.kmooc.base.BaseFragment;
import com.nile.kmooc.core.IEdxEnvironment;
import com.nile.kmooc.event.MediaStatusChangeEvent;
import com.nile.kmooc.logger.Logger;
import com.nile.kmooc.module.analytics.Analytics;
import com.nile.kmooc.module.prefs.PrefManager;
import com.nile.kmooc.util.FileUtil;
import com.nile.kmooc.view.dialog.IDialogCallback;
import com.nile.kmooc.view.dialog.NetworkCheckDialogFragment;

import de.greenrobot.event.EventBus;


public class SettingsFragment extends BaseFragment {

    public static final String TAG = SettingsFragment.class.getCanonicalName();

    private final Logger logger = new Logger(SettingsFragment.class);

    @Inject
    protected IEdxEnvironment environment;

    @Inject
    ExtensionRegistry extensionRegistry;

    private Switch wifiSwitch;
    private Switch sdCardSwitch;
    private LinearLayout sdCardSettingsLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        environment.getAnalyticsRegistry().trackScreenView(Analytics.Screens.SETTINGS);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View layout = inflater.inflate(R.layout.fragment_settings, container, false);
        wifiSwitch = (Switch) layout.findViewById(R.id.wifi_setting);
        sdCardSwitch = (Switch) layout.findViewById(R.id.download_location_switch);
        sdCardSettingsLayout = (LinearLayout) layout.findViewById(R.id.sd_card_setting_layout);
        updateWifiSwitch();
        updateSDCardSwitch();
        final LinearLayout settingsLayout = (LinearLayout) layout.findViewById(R.id.settings_layout);
        for (SettingsExtension extension : extensionRegistry.forType(SettingsExtension.class)) {
            extension.onCreateSettingsView(settingsLayout);
        }
        return layout;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    private void updateWifiSwitch() {
        final PrefManager wifiPrefManager = new PrefManager(
                getActivity().getBaseContext(), PrefManager.Pref.WIFI);

        wifiSwitch.setOnCheckedChangeListener(null);
        wifiSwitch.setChecked(wifiPrefManager.getBoolean(PrefManager.Key.DOWNLOAD_ONLY_ON_WIFI, false));
        wifiSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    wifiPrefManager.put(PrefManager.Key.DOWNLOAD_ONLY_ON_WIFI, false);
                    wifiPrefManager.put(PrefManager.Key.DOWNLOAD_OFF_WIFI_SHOW_DIALOG_FLAG, false);
                } else {
                    showWifiDialog();
                }
            }
        });
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(MediaStatusChangeEvent event) {
        sdCardSwitch.setEnabled(event.isSdCardAvailable());
    }

    private void updateSDCardSwitch() {
        final PrefManager prefManager =
                new PrefManager(getActivity().getBaseContext(), PrefManager.Pref.USER_PREF);
        if (!environment.getConfig().isDownloadToSDCardEnabled() || Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            sdCardSettingsLayout.setVisibility(View.GONE);
        } else {
            if (!EventBus.getDefault().isRegistered(this)) {
                EventBus.getDefault().registerSticky(this);
            }
            sdCardSwitch.setOnCheckedChangeListener(null);
            sdCardSwitch.setChecked(prefManager.getBoolean(PrefManager.Key.DOWNLOAD_TO_SDCARD, false));
            sdCardSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    prefManager.put(PrefManager.Key.DOWNLOAD_TO_SDCARD, isChecked);
                }
            });
            sdCardSwitch.setEnabled(FileUtil.isRemovableStorageAvailable(getActivity()));
        }
    }

    protected void showWifiDialog() {
        final NetworkCheckDialogFragment newFragment = NetworkCheckDialogFragment.newInstance(getString(R.string.wifi_dialog_title_help),
                getString(R.string.wifi_dialog_message_help),
                new IDialogCallback() {
                    @Override
                    public void onPositiveClicked() {
                        try {
                            PrefManager wifiPrefManager = new PrefManager
                                    (getActivity().getBaseContext(), PrefManager.Pref.WIFI);
                            wifiPrefManager.put(PrefManager.Key.DOWNLOAD_ONLY_ON_WIFI, true);
                            updateWifiSwitch();
                        } catch (Exception ex) {
                            logger.error(ex);
                        }
                    }

                    @Override
                    public void onNegativeClicked() {
                        try {
                            PrefManager wifiPrefManager = new PrefManager(
                                    getActivity().getBaseContext(), PrefManager.Pref.WIFI);
                            wifiPrefManager.put(PrefManager.Key.DOWNLOAD_ONLY_ON_WIFI, false);
                            wifiPrefManager.put(PrefManager.Key.DOWNLOAD_OFF_WIFI_SHOW_DIALOG_FLAG, false);

                            updateWifiSwitch();
                        } catch (Exception ex) {
                            logger.error(ex);
                        }
                    }
                });

        newFragment.setCancelable(false);
        newFragment.show(getActivity().getSupportFragmentManager(), "dialog");
    }
}