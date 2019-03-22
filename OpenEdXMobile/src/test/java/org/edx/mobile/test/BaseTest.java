package com.nile.kmooc.test;

import android.support.annotation.NonNull;

import com.nile.kmooc.CustomRobolectricTestRunner;
import com.nile.kmooc.util.observer.MainThreadObservable;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.Executor;

@Ignore
@RunWith(CustomRobolectricTestRunner.class)
public class BaseTest {
    @Before
    public final void beforeBaseTest() {
        MainThreadObservable.EXECUTOR = new Executor() {
            @Override
            public void execute(@NonNull Runnable command) {
                command.run();
            }
        };
        MockitoAnnotations.initMocks(this);
    }
}
