package com.nile.kmooc.test;

import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

import com.nile.kmooc.view.Presenter;

@VisibleForTesting
public interface PresenterInjector {
    @Nullable
    Presenter<?> getPresenter();
}
