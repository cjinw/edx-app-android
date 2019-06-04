package com.nile.kmooc.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.inject.Inject;

import com.nile.kmooc.R;
import com.nile.kmooc.base.BaseFragmentActivity;
import com.nile.kmooc.course.CourseAPI;
import com.nile.kmooc.http.notifications.FullScreenErrorNotification;
import com.nile.kmooc.http.notifications.SnackbarErrorNotification;
import com.nile.kmooc.interfaces.RefreshListener;
import com.nile.kmooc.model.api.EnrolledCoursesResponse;
import com.nile.kmooc.model.course.BlockPath;
import com.nile.kmooc.model.course.CourseComponent;
import com.nile.kmooc.model.course.CourseStructureV1Model;
import com.nile.kmooc.services.CourseManager;
import com.nile.kmooc.util.NetworkUtil;
import com.nile.kmooc.view.common.MessageType;
import com.nile.kmooc.view.common.TaskProcessCallback;

import retrofit2.Call;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

/**
 *  A base class to handle some common task
 *  NOTE - in the layout file,  these should be defined
 *  1. content_error_root (The layout that contains all of the following)
 *  2. content_error (The layout having the views that'll be used to show error)
 *  3. content_area (The layout having the views that'll be used to present data on screen)
 *  4. loading_indicator (A view or layout to show loading while data loads)
 */
@ContentView(R.layout.activity_course_base)
public abstract  class CourseBaseActivity  extends BaseFragmentActivity
        implements TaskProcessCallback, RefreshListener{

    @InjectView(R.id.loading_indicator)
    ProgressBar progressWheel;

    @InjectView(R.id.content_area)
    ViewGroup contentLayout;

    @Inject
    CourseAPI courseApi;

    @Inject
    CourseManager courseManager;

    protected EnrolledCoursesResponse courseData;
    protected String courseComponentId;

    private Call<CourseStructureV1Model> getHierarchyCall;

    protected abstract void onLoadData();

    private FullScreenErrorNotification errorNotification;

    private SnackbarErrorNotification snackbarErrorNotification;

    // Reason of usage: Helps in deciding if we want to show a full screen error or a SnackBar.
    private boolean isInitialServerCallDone = false;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        super.setToolbarAsActionBar();
        errorNotification = new FullScreenErrorNotification(contentLayout);
        snackbarErrorNotification = new SnackbarErrorNotification(contentLayout);

        Bundle bundle = arg0;
        if ( bundle == null ) {
            if ( getIntent() != null )
                bundle = getIntent().getBundleExtra(Router.EXTRA_BUNDLE);
        }
        restore(bundle);
    }

    @Override
    protected void onResume() {
        super.onResume();
        invalidateOptionsMenu();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (getHierarchyCall != null) {
            getHierarchyCall.cancel();
            getHierarchyCall = null;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(Router.EXTRA_COURSE_DATA, courseData);
        outState.putString(Router.EXTRA_COURSE_COMPONENT_ID, courseComponentId);
    }

    protected void restore(Bundle savedInstanceState) {
        courseData = (EnrolledCoursesResponse) savedInstanceState.getSerializable(Router.EXTRA_COURSE_DATA);
        courseComponentId = savedInstanceState.getString(Router.EXTRA_COURSE_COMPONENT_ID);

        if (courseComponentId == null) {
            final String courseId = courseData.getCourse().getId();
            getHierarchyCall = courseApi.getCourseStructure(courseId);
            getHierarchyCall.enqueue(new CourseAPI.GetCourseStructureCallback(this, courseId,
                    new ProgressViewController(progressWheel), errorNotification,
                    snackbarErrorNotification, this) {
                @Override
                protected void onResponse(@NonNull final CourseComponent courseComponent) {
                    courseComponentId = courseComponent.getId();
                    invalidateOptionsMenu();
                    onLoadData();
                }

                @Override
                protected void onFinish() {
                    isInitialServerCallDone = true;
                }
            });
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // If the data is available then trigger the callback
        // after basic initialization
        if (courseComponentId != null) {
            onLoadData();
            isInitialServerCallDone = true;
        }
    }

    @Override
    protected void onOffline() {
        hideLoadingProgress();
        if (isInitialServerCallDone && !errorNotification.isShowing()) {
            snackbarErrorNotification.showOfflineError(CourseBaseActivity.this);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    /**
     * This function shows the loading progress wheel
     * Show progress wheel while loading the web page
     */
    private void showLoadingProgress(){
        if(progressWheel!=null){
            progressWheel.setVisibility(View.VISIBLE);
        }
    }

    /**
     * This function hides the loading progress wheel
     * Hide progress wheel after the web page completes loading
     */
    private void hideLoadingProgress(){
        if(progressWheel!=null){
            progressWheel.setVisibility(View.GONE);
        }
    }

    /**
     * implements TaskProcessCallback
     */
    public void startProcess(){
        showLoadingProgress();
    }
    /**
     * implements TaskProcessCallback
     */
    public void finishProcess(){
        hideLoadingProgress();
    }

    public void onMessage(@NonNull MessageType messageType, @NonNull String message){
        showErrorMessage("", message);
    }

    protected boolean isOnCourseOutline(){
        if (courseComponentId == null) return true;
        CourseComponent outlineComp = courseManager.getComponentById(
                courseData.getCourse().getId(), courseComponentId);
        BlockPath outlinePath = outlineComp.getPath();
        int outlinePathSize = outlinePath.getPath().size();

        return outlinePathSize <= 1;
    }

    @Override
    public void onRefresh() {
        errorNotification.hideError();
        if (isOnCourseOutline()) {
            if (getIntent() != null) {
                restore(getIntent().getBundleExtra(Router.EXTRA_BUNDLE));
            }
        } else {
            onLoadData();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (NetworkUtil.isConnected(this)) {
            snackbarErrorNotification.hideError();
        }
    }
}

