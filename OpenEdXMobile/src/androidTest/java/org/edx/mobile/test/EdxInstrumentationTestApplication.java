package com.nile.kmooc.test;

import android.support.annotation.Nullable;

import com.nile.kmooc.base.MainApplication;
import com.nile.kmooc.view.Presenter;

public class EdxInstrumentationTestApplication extends MainApplication implements PresenterInjector {

    @Nullable
    private Presenter<?> nextPresenter = null;

    @Nullable
    @Override
    public Presenter<?> getPresenter() {
        try {
            return nextPresenter;
        } finally {
            nextPresenter = null;
        }
    }

    public void setNextPresenter(@Nullable Presenter<?> nextPresenter) {
        this.nextPresenter = nextPresenter;
    }
}
