package com.nile.kmooc.social.naver;

import android.content.Context;
import android.os.Bundle;


import com.google.inject.Singleton;

import com.nile.kmooc.logger.Logger;
import com.nile.kmooc.social.SocialFactory;
import com.nile.kmooc.social.SocialLoginDelegate;
import com.nile.kmooc.social.SocialMember;
import com.nile.kmooc.social.SocialProvider;

@Singleton
public class NaverProvider implements SocialProvider {

    protected final Logger logger = new Logger(getClass().getName());

    private SocialMember userProfile;

    private boolean notifyIfNotLoggedIn(Callback callback) {
        if (!isLoggedIn()) {
            callback.onError(new SocialError(null));
            return true;
        }
        return false;
    }



    @Override
    public boolean isLoggedIn() {
        throw new UnsupportedOperationException("Not implemented / Not supported");
    }

    @Override
    public void login(Context context, Callback<Void> callback) {
        throw new UnsupportedOperationException("Not implemented / Not supported");
    }

    public void getUserInfo(Context context,
                            SocialFactory.SOCIAL_SOURCE_TYPE socialType, String accessToken,
                            final SocialLoginDelegate.SocialUserInfoCallback userInfoCallback) {
        getUser(new Callback<SocialMember>() {
            @Override
            public void onSuccess(SocialMember response) {
                userInfoCallback.setSocialUserInfo(response.getEmail(), response.getFullName());
            }

            @Override
            public void onError(SocialError err) {
                logger.warn(err.toString());
            }
        });
    }


    @Override
    public void getUser(final Callback<SocialMember> callback) {
        throw new UnsupportedOperationException("Not implemented / Not supported");
    }
}
