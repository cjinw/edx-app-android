package com.nile.kmooc.view.adapters;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.joanzapata.iconify.widget.IconImageView;

import com.nile.kmooc.R;

public class DiscussionReportViewHolder {

    ViewGroup reportLayout;
    private IconImageView reportIconImageView;
    private TextView reportTextView;

    public DiscussionReportViewHolder(View itemView) {
        reportLayout = (ViewGroup) itemView.
                findViewById(R.id.discussion_responses_action_bar_report_container);
        reportIconImageView = (IconImageView) itemView.
                findViewById(R.id.discussion_responses_action_bar_report_icon_view);
        reportTextView = (TextView) itemView.
                findViewById(R.id.discussion_responses_action_bar_report_text_view);
    }


    public void setReported(boolean isReported) {
        reportLayout.setSelected(isReported);
        int reportStringResId = isReported ? R.string.discussion_responses_reported_label :
                R.string.discussion_responses_report_label;
        reportTextView.setText(reportTextView.getResources().getString(reportStringResId));

        int iconColor = isReported ? R.color.edx_brand_primary_base : R.color.edx_brand_gray_base;
        reportIconImageView.setIconColorResource(iconColor);
    }

    public boolean toggleReported() {
        setReported(!reportLayout.isSelected());
        return reportLayout.isSelected();
    }
}