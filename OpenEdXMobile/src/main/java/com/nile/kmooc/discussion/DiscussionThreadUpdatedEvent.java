package com.nile.kmooc.discussion;

import android.support.annotation.NonNull;

import com.nile.kmooc.discussion.DiscussionThread;

public class DiscussionThreadUpdatedEvent {

    @NonNull
    private final DiscussionThread discussionThread;

    public DiscussionThreadUpdatedEvent(@NonNull DiscussionThread discussionThread) {
        this.discussionThread = discussionThread;
    }

    @NonNull
    public DiscussionThread getDiscussionThread() {
        return discussionThread;
    }
}
