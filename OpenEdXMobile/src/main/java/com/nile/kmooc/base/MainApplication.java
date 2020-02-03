package com.nile.kmooc.base;


import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.multidex.MultiDexApplication;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.model.GlideUrl;
import com.crashlytics.android.core.CrashlyticsCore;
import com.facebook.FacebookSdk;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.FontAwesomeModule;
import com.newrelic.agent.android.NewRelic;

import com.nile.kmooc.BuildConfig;
import com.nile.kmooc.R;
import com.nile.kmooc.core.EdxDefaultModule;
import com.nile.kmooc.core.IEdxEnvironment;
import com.nile.kmooc.event.AppUpdatedEvent;
import com.nile.kmooc.event.NewRelicEvent;
import com.nile.kmooc.http.provider.OkHttpClientProvider;
import com.nile.kmooc.logger.Logger;
import com.nile.kmooc.model.VideoModel;
import com.nile.kmooc.model.api.ProfileModel;
import com.nile.kmooc.module.analytics.AnalyticsRegistry;
import com.nile.kmooc.module.analytics.AnswersAnalytics;
import com.nile.kmooc.module.analytics.FirebaseAnalytics;
import com.nile.kmooc.module.analytics.SegmentAnalytics;
import com.nile.kmooc.module.db.DataCallback;
import com.nile.kmooc.module.db.IDatabase;
import com.nile.kmooc.module.prefs.PrefManager;
import com.nile.kmooc.module.storage.IStorage;
import com.nile.kmooc.receivers.NetworkConnectivityReceiver;
import com.nile.kmooc.util.Config;
import com.nile.kmooc.util.FileUtil;
import com.nile.kmooc.util.NetworkUtil;
import com.nile.kmooc.util.NotificationUtil;
import com.nile.kmooc.util.PermissionsUtil;
import com.nile.kmooc.util.Sha1Util;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import io.branch.referral.Branch;
import io.fabric.sdk.android.Fabric;
import roboguice.RoboGuice;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * This class initializes the modules of the app based on the configuration.
 */
public abstract class MainApplication extends MultiDexApplication {

    protected final Logger logger = new Logger(getClass().getName());

    public static MainApplication application;

    public static final MainApplication instance() {
        return application;
    }

    private Injector injector;

    @Inject
    protected Config config;

    @Inject
    protected AnalyticsRegistry analyticsRegistry;

    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    /**
     * Initializes the request manager, image cache,
     * all third party integrations and shared components.
     */
    private void init() {
        application = this;
        // FIXME: Disable RoboBlender to avoid annotation processor issues for now, as we already have plans to move to some other DI framework. See LEARNER-1687.
        // ref: https://github.com/roboguice/roboguice/wiki/RoboBlender-wiki#disabling-roboblender
        // ref: https://developer.android.com/studio/build/gradle-plugin-3-0-0-migration
        RoboGuice.setUseAnnotationDatabases(false);
        injector = RoboGuice.getOrCreateBaseApplicationInjector((Application) this, RoboGuice.DEFAULT_STAGE,
                (Module) RoboGuice.newDefaultRoboModule(this), (Module) new EdxDefaultModule(this));

        injector.injectMembers(this);

        // initialize Fabric
        if (config.getFabricConfig().isEnabled() && !BuildConfig.DEBUG) {
            Fabric.with(this, config.getFabricConfig().getKitsConfig().getEnabledKits());

            if (config.getFabricConfig().getKitsConfig().isCrashlyticsEnabled()) {
                EventBus.getDefault().register(new CrashlyticsCrashReportObserver());
            }

            if (config.getFabricConfig().getKitsConfig().isAnswersEnabled()) {
                analyticsRegistry.addAnalyticsProvider(injector.getInstance(AnswersAnalytics.class));
            }
        }

        if (config.getNewRelicConfig().isEnabled()) {
            EventBus.getDefault().register(new NewRelicObserver());
        }

        // initialize NewRelic with crash reporting disabled
        if (config.getNewRelicConfig().isEnabled()) {
            //Crash reporting for new relic has been disabled
            NewRelic.withApplicationToken(config.getNewRelicConfig().getNewRelicKey())
                    .withCrashReportingEnabled(false)
                    .start(this);
        }

        // Add Segment as an analytics provider if enabled in the config
        if (config.getSegmentConfig().isEnabled()) {
            analyticsRegistry.addAnalyticsProvider(injector.getInstance(SegmentAnalytics.class));
        }

        // Add Firebase as an analytics provider if enabled in the config
        if (config.getFirebaseConfig().isAnalyticsEnabled()) {
            analyticsRegistry.addAnalyticsProvider(injector.getInstance(FirebaseAnalytics.class));
        }

        if (config.getFirebaseConfig().areNotificationsEnabled()) {
            NotificationUtil.subscribeToTopics(config);
        } else if (!config.getFirebaseConfig().areNotificationsEnabled() &&
                config.getFirebaseConfig().isEnabled()) {
            NotificationUtil.unsubscribeFromTopics(config);
        }

        registerReceiver(new NetworkConnectivityReceiver(), new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        registerReceiver(new NetworkConnectivityReceiver(), new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));

        checkIfAppVersionUpgraded(this);

        // Register Font Awesome module in android-iconify library
        Iconify.with(new FontAwesomeModule());

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/OpenSans-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        // Init Branch
        if (Config.FabricBranchConfig.isBranchEnabled(config.getFabricConfig())) {
            Branch.getAutoInstance(this);
        }

        // Force Glide to use our version of OkHttp which now supports TLS 1.2 out-of-the-box for
        // Pre-Lollipop devices
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Glide.get(this).register(GlideUrl.class, InputStream.class,
                    new OkHttpUrlLoader.Factory(injector.getInstance(OkHttpClientProvider.class).get()));
        }

        // Initialize Facebook SDK
        boolean isOnZeroRatedNetwork = NetworkUtil.isOnZeroRatedNetwork(getApplicationContext(), config);
        if (!isOnZeroRatedNetwork && config.getFacebookConfig().isEnabled()) {
            // Facebook sdk should be initialized through AndroidManifest meta data declaration but
            // we are generating the meta data through gradle script due to which it is necessary
            // to manually initialize the sdk here.
            FacebookSdk.setApplicationId(config.getFacebookConfig().getFacebookAppId());
            FacebookSdk.sdkInitialize(getApplicationContext());
        }

        if (config.getNaverConfig().isEnabled()) {
            com.nile.kmooc.social.naver.NaverAuth.setClientKeys(config.getNaverConfig().getClientId(), config.getNaverConfig().getClientSecret());
        }

        if (PermissionsUtil.checkPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, this)) {
            deleteExtraDownloadedFiles();
        }
    }

    private void checkIfAppVersionUpgraded(Context context) {
        PrefManager.AppInfoPrefManager prefManager = new PrefManager.AppInfoPrefManager(context);
        long previousVersionCode = prefManager.getAppVersionCode();
        final long curVersionCode = BuildConfig.VERSION_CODE;
        if (previousVersionCode < 0) {
            // App opened first time after installation
            // Save version code and name in preferences
            prefManager.setAppVersionCode(curVersionCode);
            prefManager.setAppVersionName(BuildConfig.VERSION_NAME);
            logger.debug("App opened first time, VersionCode:" + curVersionCode);
        } else if (previousVersionCode < curVersionCode) {
            final String previousVersionName = prefManager.getAppVersionName();
            // Update version code and name in preferences
            prefManager.setAppVersionCode(curVersionCode);
            prefManager.setAppVersionName(BuildConfig.VERSION_NAME);
            logger.debug("App updated, VersionCode:" + previousVersionCode + "->" + curVersionCode);
            // App updated
            onAppUpdated(previousVersionCode, curVersionCode, previousVersionName, BuildConfig.VERSION_NAME);
        }
    }

    private void onAppUpdated(final long previousVersionCode, final long curVersionCode,
                              final String previousVersionName, final String curVersionName) {
        // Try repair of download data on updating of app version
        injector.getInstance(IStorage.class).repairDownloadCompletionData();
        // Fire app updated event
        EventBus.getDefault().postSticky(new AppUpdatedEvent(previousVersionCode, curVersionCode,
                previousVersionName, curVersionName));
    }

    public static class CrashlyticsCrashReportObserver {
        @SuppressWarnings("unused")
        public void onEventMainThread(Logger.CrashReportEvent e) {
            CrashlyticsCore.getInstance().logException(e.getError());
        }
    }

    public static class NewRelicObserver {
        @SuppressWarnings("unused")
        public void onEventMainThread(NewRelicEvent e) {
            NewRelic.setInteractionName("Display " + e.getScreenName());
        }
    }

    public Injector getInjector() {
        return injector;
    }

    @NonNull
    public static IEdxEnvironment getEnvironment(@NonNull Context context) {
        return RoboGuice.getInjector(context.getApplicationContext()).getInstance(IEdxEnvironment.class);
    }

    /**
     * Utility function to delete the all extra files (unused files e.g user eject SD-card while
     * video downloading in SD-Card, video downloading stops but android OS unable to delete the
     * created file) from the downloads directory in background.
     */
    public void deleteExtraDownloadedFiles() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final ProfileModel profile = getEnvironment(MainApplication.this).getUserPrefs().getProfile();
                final IDatabase db = getEnvironment(MainApplication.this).getDatabase();
                if (profile != null) {
                    db.getAllVideos(Sha1Util.SHA1(profile.username), new DataCallback<List<VideoModel>>() {
                        @Override
                        public void onResult(List<VideoModel> result) {
                            ArrayList<File> extraFiles = FileUtil.getAllFileFromExternalStorage(MainApplication.this, profile);
                            FileUtil.deleteExtraFilesNotInDatabase(result, extraFiles);
                        }

                        @Override
                        public void onFail(Exception ex) {
                            Log.e(this.getClass().getSimpleName(),
                                    "Unable to get to get list of Videos"
                            );
                        }
                    });
                }
            }
        }).start();
    }
}