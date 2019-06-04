package com.nile.kmooc.view;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;

import com.nile.kmooc.R;
import com.nile.kmooc.base.BaseFragment;

public class GroupDiscussionFragment extends BaseFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_group_discussion, container, false);
    }
}
