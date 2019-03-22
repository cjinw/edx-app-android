package com.nile.kmooc.view;

import android.view.View;
import android.webkit.WebView;

import com.nile.kmooc.R;
import com.nile.kmooc.course.CourseAPI;
import com.nile.kmooc.exception.CourseContentNotValidException;
import com.nile.kmooc.model.api.EnrolledCoursesResponse;
import com.nile.kmooc.model.course.BlockType;
import com.nile.kmooc.model.course.CourseComponent;
import com.nile.kmooc.model.course.CourseStructureV1Model;
import com.nile.kmooc.model.course.HtmlBlockModel;
import org.junit.Test;
import org.robolectric.shadows.support.v4.SupportFragmentTestUtil;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static com.nile.kmooc.http.util.CallUtil.executeStrict;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class CourseUnitWebViewFragmentTest extends UiTest {
    /**
     * Method for iterating through the mock course response data, and
     * returning the first video block leaf.
     *
     * @return The first {@link HtmlBlockModel} leaf in the mock course data
     */
    private HtmlBlockModel getHtmlUnit() throws CourseContentNotValidException {
        EnrolledCoursesResponse courseData;
        try {
            courseData = executeStrict(courseAPI.getEnrolledCourses()).get(0);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        String courseId = courseData.getCourse().getId();
        CourseStructureV1Model model;
        try {
            model = executeStrict(courseAPI.getCourseStructure(courseId));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        CourseComponent courseComponent = (CourseComponent)
                CourseAPI.normalizeCourseStructure(model, courseId);
        List<CourseComponent> htmlBlockUnits = new ArrayList<>();
        courseComponent.fetchAllLeafComponents(htmlBlockUnits,
                EnumSet.of(BlockType.HTML));
        return (HtmlBlockModel) htmlBlockUnits.get(0);
    }

    /**
     * Testing initialization
     */
    @Test
    public void initializeTest() throws CourseContentNotValidException {
        CourseUnitWebViewFragment fragment = CourseUnitWebViewFragment.newInstance(getHtmlUnit());
        SupportFragmentTestUtil.startVisibleFragment(fragment);
        View view = fragment.getView();
        assertNotNull(view);

        View courseUnitWebView = view.findViewById(R.id.webview);
        assertNotNull(courseUnitWebView);
        assertThat(courseUnitWebView).isInstanceOf(WebView.class);
        WebView webView = (WebView) courseUnitWebView;
        assertTrue(webView.getSettings().getJavaScriptEnabled());
    }
}
