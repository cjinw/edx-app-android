package org.edx.mobile.authentication;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.edx.mobile.http.HttpStatus;
import org.edx.mobile.http.HttpStatusException;
import org.edx.mobile.http.constants.ApiConstants;
import org.edx.mobile.model.api.FormFieldMessageBody;
import org.edx.mobile.model.api.ProfileModel;
import org.edx.mobile.module.analytics.AnalyticsRegistry;
import org.edx.mobile.module.notification.NotificationDelegate;
import org.edx.mobile.module.prefs.LoginPrefs;
import org.edx.mobile.util.Config;
import org.edx.mobile.util.observer.BasicObservable;
import org.edx.mobile.util.observer.Observable;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Response;

import static org.edx.mobile.http.util.CallUtil.executeStrict;

@Singleton
public class LoginAPI {

    @NonNull
    private final LoginService loginService;

    @NonNull
    private final Config config;

    @NonNull
    private final LoginPrefs loginPrefs;

    @NonNull
    private final AnalyticsRegistry analyticsRegistry;

    @NonNull
    private final NotificationDelegate notificationDelegate;

    @NonNull
    private final BasicObservable<LogInEvent> logInEvents = new BasicObservable<>();

    @NonNull
    private final Gson gson;

    @Inject
    public LoginAPI(@NonNull LoginService loginService,
                    @NonNull Config config,
                    @NonNull LoginPrefs loginPrefs,
                    @NonNull AnalyticsRegistry analyticsRegistry,
                    @NonNull NotificationDelegate notificationDelegate,
                    @NonNull Gson gson) {
        this.loginService = loginService;
        this.config = config;
        this.loginPrefs = loginPrefs;
        this.analyticsRegistry = analyticsRegistry;
        this.notificationDelegate = notificationDelegate;
        this.gson = gson;
    }

    @NonNull
    public Response<AuthResponse> getAccessToken(@NonNull String username,
                                       @NonNull String password) throws IOException {
        String grantType = "password";
        String clientID = config.getOAuthClientId();
        return loginService.getAccessToken(grantType, clientID, username, password).execute();
    }

    @NonNull
    public AuthResponse logInUsingEmail(@NonNull String email, @NonNull String password) throws Exception {
        final Response<AuthResponse> response = getAccessToken(email, password);
        if (!response.isSuccessful()) {
            throw new HttpStatusException(response);
        }
        finishLogIn(response.body(), LoginPrefs.AuthBackend.PASSWORD, email.trim());
        return response.body();
    }

    @NonNull
    public AuthResponse logInUsingFacebook(String accessToken) throws Exception {
        return finishSocialLogIn(accessToken, LoginPrefs.AuthBackend.FACEBOOK);
    }

    @NonNull
    public AuthResponse logInUsingGoogle(String accessToken) throws Exception {
        return finishSocialLogIn(accessToken, LoginPrefs.AuthBackend.GOOGLE);
    }

    @NonNull
    public AuthResponse logInUsingNaver(String accessToken) throws Exception {
        return finishSocialLogIn(accessToken, LoginPrefs.AuthBackend.NAVER);
    }

    @NonNull
    public AuthResponse logInUsingKakao(String accessToken) throws Exception {
        return finishSocialLogIn(accessToken, LoginPrefs.AuthBackend.KAKAO);
    }

    @NonNull
    private AuthResponse finishSocialLogIn(@NonNull String accessToken, @NonNull LoginPrefs.AuthBackend authBackend) throws Exception {
        final String backend = ApiConstants.getOAuthGroupIdForAuthBackend(authBackend);
        final Response<AuthResponse> response = loginService.exchangeAccessToken(accessToken, config.getOAuthClientId(), backend).execute();
        if (response.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
            // TODO: Introduce a more explicit error code to indicate that an account is not linked.
            throw new AccountNotLinkedException();
        }
        if (!response.isSuccessful()) {
            throw new HttpStatusException(response);
        }
        final AuthResponse data = response.body();
        if (data.error != null && data.error.equals(Integer.toString(HttpURLConnection.HTTP_UNAUTHORIZED))) {
            throw new AccountNotLinkedException();
        }
        finishLogIn(data, authBackend, "");
        return data;
    }

    private void finishLogIn(@NonNull AuthResponse response, @NonNull LoginPrefs.AuthBackend authBackend, @NonNull String usernameUsedToLogIn) throws Exception {
        loginPrefs.storeAuthTokenResponse(response, authBackend);
        try {
            response.profile = getProfile();
        } catch (Throwable e) {
            // The app doesn't properly handle the scenario that we are logged in but we don't have
            // a cached profile. So if we fail to fetch the profile, let's erase the stored token.
            // TODO: A better approach might be to fetch the profile *before* storing the token.
            loginPrefs.clearAuthTokenResponse();
            throw e;
        }
        loginPrefs.setLastAuthenticatedEmail(usernameUsedToLogIn);
        analyticsRegistry.identifyUser(
                response.profile.id.toString(),
                response.profile.email,
                usernameUsedToLogIn);
        final String backendKey = loginPrefs.getAuthBackendKeyForSegment();
        if (backendKey != null) {
            analyticsRegistry.trackUserLogin(backendKey);
        }
        notificationDelegate.resubscribeAll();
        logInEvents.sendData(new LogInEvent());
    }

    public void logOut() {
        final AuthResponse currentAuth = loginPrefs.getCurrentAuth();
        if (currentAuth != null && currentAuth.refresh_token != null) {
            loginService.revokeAccessToken(config.getOAuthClientId(),
                    currentAuth.refresh_token, ApiConstants.TOKEN_TYPE_REFRESH);
        }
    }

    @NonNull
    public AuthResponse registerUsingEmail(@NonNull Bundle parameters) throws Exception {
        register(parameters);
        return logInUsingEmail(parameters.getString("username"), parameters.getString("password"));
    }

    @NonNull
    public AuthResponse registerUsingGoogle(@NonNull Bundle parameters, @NonNull String accessToken) throws Exception {
        register(parameters);
        return logInUsingGoogle(accessToken);
    }

    @NonNull
    public AuthResponse registerUsingFacebook(@NonNull Bundle parameters, @NonNull String accessToken) throws Exception {
        register(parameters);
        return logInUsingFacebook(accessToken);
    }

    @NonNull
    public Observable<LogInEvent> getLogInEvents() {
        return logInEvents;
    }

    @NonNull
    private void register(Bundle parameters) throws Exception {
        final Map<String, String> parameterMap = new HashMap<>();
        for (String key : parameters.keySet()) {
            parameterMap.put(key, parameters.getString(key));
        }
        Response<ResponseBody> response = loginService.register(parameterMap).execute();
        if (!response.isSuccessful()) {
            final int errorCode = response.code();
            final String errorBody = response.errorBody().string();
            if ((errorCode == HttpStatus.BAD_REQUEST || errorCode == HttpStatus.CONFLICT) && !android.text.TextUtils.isEmpty(errorBody)) {
                try {
                    final FormFieldMessageBody body = gson.fromJson(errorBody, FormFieldMessageBody.class);
                    if (body != null && body.size() > 0) {
                        throw new RegistrationException(body);
                    }
                } catch (JsonSyntaxException ex) {
                    // Looks like the response does not contain form validation errors.
                }
            }
            throw new HttpStatusException(response);
        }
    }

    @NonNull
    public ProfileModel getProfile() throws Exception {
        ProfileModel data = executeStrict(loginService.getProfile());
        loginPrefs.storeUserProfile(data);
        return data;
    }

    public static class AccountNotLinkedException extends Exception {
    }

    public static class RegistrationException extends Exception {
        @NonNull
        private final FormFieldMessageBody formErrorBody;

        public RegistrationException(@NonNull FormFieldMessageBody formErrorBody) {
            this.formErrorBody = formErrorBody;
        }

        @NonNull
        public FormFieldMessageBody getFormErrorBody() {
            return formErrorBody;
        }
    }
}
