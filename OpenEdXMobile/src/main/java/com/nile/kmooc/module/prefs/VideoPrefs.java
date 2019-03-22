package com.nile.kmooc.module.prefs;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.nile.kmooc.authentication.AuthResponse;
import com.nile.kmooc.base.MainApplication;
import com.nile.kmooc.model.api.ProfileModel;
import com.nile.kmooc.module.analytics.Analytics;
import com.nile.kmooc.services.EdxCookieManager;
import com.nile.kmooc.user.ProfileImage;
import com.nile.kmooc.view.BulkDownloadFragment;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class VideoPrefs {
    @NonNull
    private final PrefManager pref;

    @Inject
    public VideoPrefs(@NonNull Context context) {
        pref = new PrefManager(context, PrefManager.Pref.VIDEOS);
    }


    public BulkDownloadFragment.SwitchState getBulkDownloadSwitchState(@NonNull String courseId) {
        final int ordinal = pref.getInt(String.format(PrefManager.Key.BULK_DOWNLOAD_FOR_COURSE_ID, courseId));
        return (ordinal == -1 ?
                BulkDownloadFragment.SwitchState.DEFAULT :
                BulkDownloadFragment.SwitchState.values()[ordinal]);
    }

    public void setBulkDownloadSwitchState(@NonNull BulkDownloadFragment.SwitchState state,
                                           @NonNull String courseId) {
        pref.put(String.format(PrefManager.Key.BULK_DOWNLOAD_FOR_COURSE_ID, courseId), state.ordinal());
    }
}
