package com.nile.kmooc.core;


import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.nile.kmooc.module.analytics.AnalyticsRegistry;
import com.nile.kmooc.module.db.IDatabase;
import com.nile.kmooc.module.download.IDownloadManager;
import com.nile.kmooc.module.notification.NotificationDelegate;
import com.nile.kmooc.module.prefs.LoginPrefs;
import com.nile.kmooc.module.prefs.UserPrefs;
import com.nile.kmooc.module.storage.IStorage;
import com.nile.kmooc.util.Config;
import com.nile.kmooc.view.Router;

import de.greenrobot.event.EventBus;

@Singleton
public class EdxEnvironment implements IEdxEnvironment {

    @Inject
    IDatabase database;

    @Inject
    IStorage storage;

    @Inject
    IDownloadManager downloadManager;

    @Inject
    UserPrefs userPrefs;

    @Inject
    LoginPrefs loginPrefs;

    @Inject
    AnalyticsRegistry analyticsRegistry;

    @Inject
    NotificationDelegate notificationDelegate;

    @Inject
    Router router;

    @Inject
    Config config;

    @Inject
    EventBus eventBus;

    @Override
    public IDatabase getDatabase() {
        return database;
    }

    @Override
    public IDownloadManager getDownloadManager() {
        return downloadManager;
    }

    @Override
    public UserPrefs getUserPrefs() {
        return userPrefs;
    }

    @Override
    public LoginPrefs getLoginPrefs() {
        return loginPrefs;
    }

    public AnalyticsRegistry getAnalyticsRegistry() {
        return analyticsRegistry;
    }

    @Override
    public NotificationDelegate getNotificationDelegate() {
        return notificationDelegate;
    }

    @Override
    public Router getRouter() {
        return router;
    }

    @Override
    public Config getConfig() {
        return config;
    }

    @Override
    public IStorage getStorage() {
        return storage;
    }

    public EventBus getEventBus() {
        return eventBus;
    }
}
