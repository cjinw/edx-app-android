package com.nile.kmooc.test;

import android.support.annotation.NonNull;

import com.nile.kmooc.view.Presenter;
import org.junit.After;

import static org.mockito.Mockito.mock;

public abstract class PresenterTest<P extends Presenter<V>, V> extends BaseTest {
    protected P presenter;
    protected V view;

    protected final void startPresenter(@NonNull P p) {
        presenter = p;
        view = mock(getViewType());
        presenter.attachView(view);
    }

    @After
    public final void after() {
        if (null != view && null != presenter) {
            presenter.detachView();
            presenter.destroy();
        }
    }

    @SuppressWarnings("unchecked")
    private Class<V> getViewType() {
        return (Class<V>) GenericSuperclassUtils.getTypeArguments(getClass(), PresenterTest.class)[1];
    }
}
