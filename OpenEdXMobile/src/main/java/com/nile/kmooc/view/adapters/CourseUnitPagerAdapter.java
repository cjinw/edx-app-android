package com.nile.kmooc.view.adapters;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.nile.kmooc.model.api.EnrolledCoursesResponse;
import com.nile.kmooc.model.course.BlockType;
import com.nile.kmooc.model.course.CourseComponent;
import com.nile.kmooc.model.course.DiscussionBlockModel;
import com.nile.kmooc.model.course.HtmlBlockModel;
import com.nile.kmooc.model.course.VideoBlockModel;
import com.nile.kmooc.util.Config;
import com.nile.kmooc.view.CourseUnitDiscussionFragment;
import com.nile.kmooc.view.CourseUnitEmptyFragment;
import com.nile.kmooc.view.CourseUnitFragment;
import com.nile.kmooc.view.CourseUnitMobileNotSupportedFragment;
import com.nile.kmooc.view.CourseUnitOnlyOnYoutubeFragment;
import com.nile.kmooc.view.CourseUnitVideoFragment;
import com.nile.kmooc.view.CourseUnitWebViewFragment;

import java.util.List;

public class CourseUnitPagerAdapter extends FragmentStatePagerAdapter {
    private Config config;
    private List<CourseComponent> unitList;
    private EnrolledCoursesResponse courseData;
    private CourseUnitFragment.HasComponent callback;

    public CourseUnitPagerAdapter(FragmentManager manager,
                                  Config config,
                                  List<CourseComponent> unitList,
                                  EnrolledCoursesResponse courseData,
                                  CourseUnitFragment.HasComponent callback) {
        super(manager);
        this.config = config;
        this.unitList = unitList;
        this.courseData = courseData;
        this.callback = callback;
    }

    public CourseComponent getUnit(int pos) {
        if (pos >= unitList.size())
            pos = unitList.size() - 1;
        if (pos < 0)
            pos = 0;
        return unitList.get(pos);
    }

    /**
     * @return True if given unit is a video unit (not an only on YouTube unit)
     */
    public static boolean isCourseUnitVideo(CourseComponent unit) {
        return (unit instanceof VideoBlockModel && ((VideoBlockModel) unit).getData().encodedVideos.getPreferredVideoInfo() != null);
    }

    @Override
    public Fragment getItem(int pos) {
        final CourseComponent unit = getUnit(pos);
        // FIXME: Remove this code once LEARNER-6713 is merged
        final CourseComponent minifiedUnit;
        {
            // Create a deep copy of original CourseComponent object with `root` and `parent` objects
            // removed to save memory.
            if (unit instanceof VideoBlockModel) {
                minifiedUnit = new VideoBlockModel((VideoBlockModel) unit);
            } else if (unit instanceof DiscussionBlockModel) {
                minifiedUnit = new DiscussionBlockModel((DiscussionBlockModel) unit);
            } else if (unit instanceof HtmlBlockModel) {
                minifiedUnit = new HtmlBlockModel((HtmlBlockModel) unit);
            } else minifiedUnit = new CourseComponent(unit);
        }
        CourseUnitFragment unitFragment;
        //FIXME - for the video, let's ignore studentViewMultiDevice for now
        if (isCourseUnitVideo(minifiedUnit)) {
            unitFragment = CourseUnitVideoFragment.newInstance((VideoBlockModel) minifiedUnit, (pos < unitList.size()), (pos > 0));
        } else if (minifiedUnit instanceof VideoBlockModel && ((VideoBlockModel) minifiedUnit).getData().encodedVideos.getYoutubeVideoInfo() != null) {
            unitFragment = CourseUnitOnlyOnYoutubeFragment.newInstance(minifiedUnit);
        } else if (config.isDiscussionsEnabled() && minifiedUnit instanceof DiscussionBlockModel) {
            unitFragment = CourseUnitDiscussionFragment.newInstance(minifiedUnit, courseData);
        } else if (!minifiedUnit.isMultiDevice()) {
            unitFragment = CourseUnitMobileNotSupportedFragment.newInstance(minifiedUnit);
        } else if (minifiedUnit.getType() != BlockType.VIDEO &&
                minifiedUnit.getType() != BlockType.HTML &&
                minifiedUnit.getType() != BlockType.OTHERS &&
                minifiedUnit.getType() != BlockType.DISCUSSION &&
                minifiedUnit.getType() != BlockType.PROBLEM) {
            unitFragment = CourseUnitEmptyFragment.newInstance(minifiedUnit);
        } else if (minifiedUnit instanceof HtmlBlockModel) {
            unitFragment = CourseUnitWebViewFragment.newInstance((HtmlBlockModel) minifiedUnit);
        }

        //fallback
        else {
            unitFragment = CourseUnitMobileNotSupportedFragment.newInstance(minifiedUnit);
        }

        unitFragment.setHasComponentCallback(callback);

        return unitFragment;
    }

    @Override
    public int getCount() {
        return unitList.size();
    }
}
