package com.sharesmile.share.login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sharesmile.share.core.application.MainApplication;
import com.sharesmile.share.R;
import com.sharesmile.share.analytics.events.AnalyticsEvent;
import com.sharesmile.share.analytics.events.Event;
import com.sharesmile.share.network.NetworkAsyncCallback;
import com.sharesmile.share.network.NetworkDataProvider;
import com.sharesmile.share.network.NetworkException;
import com.sharesmile.share.core.sync.SyncHelper;
import com.sharesmile.share.network.BasicNameValuePair;
import com.sharesmile.share.utils.JsonHelper;
import com.sharesmile.share.core.Logger;
import com.sharesmile.share.network.NameValuePair;
import com.sharesmile.share.core.config.Urls;
import com.sharesmile.share.utils.Utils;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Shine on 19/05/16.
 */
public class LoginImpl {

    private static final int REQUEST_GOOGLE_SIGN_IN = 1001;
    private static final String TAG = LoginImpl.class.getSimpleName();

    private final LoginListener mListener;
    private WeakReference<AppCompatActivity> activityWeakReference = null;
    private WeakReference<Fragment> fragmentWeakReference = null;
    private CallbackManager callbackManager;
    private GoogleApiClient mGoogleApiClient;

    public LoginImpl(AppCompatActivity activity, LoginListener listener) {
        activityWeakReference = new WeakReference<AppCompatActivity>(activity);
        mListener = listener;
        init();
    }

    public LoginImpl(Fragment fragment, LoginListener listener) {
        fragmentWeakReference = new WeakReference<Fragment>(fragment);
        mListener = listener;
        init();
    }

    private void init() {
        initializeFbLogin();
        initializeGoogleLogin();
    }

    private void initializeGoogleLogin() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail().requestIdToken(MainApplication.getContext().getString(R.string.server_client_id))
                .requestServerAuthCode(MainApplication.getContext().getString(R.string.server_client_id), false)
                .build();

        Context context = getContext();
        if (context != null) {
            mGoogleApiClient = new GoogleApiClient.Builder(context)
                    .enableAutoManage(activityWeakReference != null ? activityWeakReference.get()
                            : fragmentWeakReference.get().getActivity(), mConnectionFailedListener)
                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                    .build();
        }

    }

    private void initializeFbLogin() {
        Context context = getContext();
        if (context != null) {
            FacebookSdk.sdkInitialize(context.getApplicationContext());
            callbackManager = CallbackManager.Factory.create();
            LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    Logger.d("facebook", "success " + loginResult.getAccessToken().getToken());
                    final String token = loginResult.getAccessToken().getToken();

                    GraphRequest request = GraphRequest.newMeRequest(
                            loginResult.getAccessToken(),
                            new GraphRequest.GraphJSONObjectCallback() {
                                @Override
                                public void onCompleted(
                                        JSONObject object,
                                        GraphResponse response) {
                                    verifyUserDetails(token, true);
                                }
                            });
                    Bundle parameters = new Bundle();
                    parameters.putString("fields", "id,name,email");
                    request.setParameters(parameters);
                    request.executeAsync();
                }

                @Override
                public void onCancel() {
                    Logger.d("facebook", "cancel");
                }

                @Override
                public void onError(FacebookException error) {
                    Logger.d("facebook", "Error");
                    error.printStackTrace();
                    MainApplication.getInstance().showToast(R.string.login_error);
                    sendLoginFailureEvent(-1, error.getMessage(), true);
                }
            });
        }
    }

    private void verifyUserDetails(String token, final boolean isFbLogin) {

        mListener.showHideProgress(true, MainApplication.getContext().getString(R.string.login));
        Map<String, String> header = new HashMap<>();
        if (isFbLogin) {
//            Logger.d(TAG, "Putting Facebook accessToken: " + token);
            header.put("Authorization", "Bearer facebook " + token);
        } else {
//            Logger.d(TAG, "Putting google accessToken: " + token);
            header.put("Authorization", "Bearer google-oauth2 " + token);
        }

        NetworkDataProvider.doGetCallAsync(Urls.getLoginUrl(), header, new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.i(TAG, "Login error, Api failed");
                MainApplication.getInstance().getMainThreadHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        mListener.showHideProgress(false, null);
                    }
                });
                sendLoginFailureEvent(-1, "Request Failed", isFbLogin);
            }

            @Override
            public void onResponse(Response response) throws IOException {
                String responseString = response.body().string();
                Logger.d("LoginImpl", "onResponse: " + responseString);
                JsonArray array = JsonHelper.StringToJsonArray(responseString);
                if (array == null) {
                    Crashlytics.logException(new Throwable("Login Response error. Server response : " + responseString));
                    MainApplication.getInstance().getMainThreadHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            mListener.showHideProgress(false, null);
                        }
                    });
                    sendLoginFailureEvent(response.code(), responseString, isFbLogin);
                    return;
                }

                final JsonObject element = array.get(0).getAsJsonObject();
                Log.i("LoginImpl", "element: " + element.toString());
                MainApplication.getInstance().getMainThreadHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        userLoginSuccess(element, isFbLogin);
                    }
                });

            }
        });

    }

    private void sendLoginFailureEvent(int responseCode, String optionalErrorMessage,
                                       final boolean isFbLogin){
        String medium = isFbLogin ? "fb" : "google";
        AnalyticsEvent.create(Event.ON_LOGIN_FAILED)
                .put("error", optionalErrorMessage)
                .put("response_code", responseCode)
                .put("medium", medium)
                .buildAndDispatch();
    }

    private void userLoginSuccess(JsonObject response, final boolean isFbLogin) {


        Gson gson = new Gson();
        UserDetails userDetails = gson.fromJson(response, UserDetails.class);
        MainApplication.getInstance().setUserDetails(userDetails);
        if(userDetails.getBodyHeight()>0)
        {
            Utils.setOnboardingShown();
        }
        SyncHelper.getStreak();
        //show Toast confirmation
        Toast.makeText(MainApplication.getContext(), "Logged in as " + userDetails.getFirstName(), Toast.LENGTH_SHORT).show();

        String medium = isFbLogin ? "fb" : "google";
        AnalyticsEvent.create(Event.ON_LOGIN_SUCCESS)
                .put("user_id", userDetails.getUserId())
                .put("user_name", userDetails.getFullName())
                .put("user_email", userDetails.getEmail())
                .put("is_sign_up_user", userDetails.isSignUp())
                .put("medium", medium)
                .buildAndDispatch();

        //Pull historical run data;
        SyncHelper.forceRefreshEntireWorkoutHistory();

        mListener.onLoginSuccess();

    }


    private GoogleApiClient.OnConnectionFailedListener mConnectionFailedListener = new GoogleApiClient.OnConnectionFailedListener() {
        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {

        }
    };

    /**
     * To be call when lifecycle of caller activity/fragment ends
     */
    public void disconnect(){
        if (mGoogleApiClient != null){
            FragmentActivity activity =  activityWeakReference != null ? activityWeakReference.get()
                    : fragmentWeakReference.get().getActivity();
            if (activity != null){
                mGoogleApiClient.stopAutoManage(activity);
            }
            if (mGoogleApiClient.isConnected()){
                mGoogleApiClient.disconnect();
            }
        }
    }

    private Context getContext() {

        if (activityWeakReference != null && !activityWeakReference.get().isFinishing()) {
            return activityWeakReference.get();
        } else if (fragmentWeakReference.get() != null) {
            return fragmentWeakReference.get().getContext();
        }
        return null;

    }

    public void performGoogleLogin() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        if (activityWeakReference != null) {
            activityWeakReference.get().startActivityForResult(signInIntent, REQUEST_GOOGLE_SIGN_IN);
        } else {
            fragmentWeakReference.get().startActivityForResult(signInIntent, REQUEST_GOOGLE_SIGN_IN);
        }

    }

    public void performFbLogin() {
        if (activityWeakReference != null) {
            LoginManager.getInstance().logInWithReadPermissions(activityWeakReference.get(), Arrays.asList("public_profile", "email"));
        } else {
            LoginManager.getInstance().logInWithReadPermissions(fragmentWeakReference.get(), Arrays.asList("public_profile", "email"));
        }

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_GOOGLE_SIGN_IN:
                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                handleGoogleSignInResult(result);
                break;
            default:
                callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void handleGoogleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            GoogleSignInAccount acct = result.getSignInAccount();
            Logger.d(TAG, "email: " + acct.getEmail() + " Name : " + acct.getDisplayName() + " token " + acct.getIdToken() + "auth :  " + acct.getServerAuthCode());
            get_google_access_token(acct.getServerAuthCode());
        } else {
            Logger.d(TAG, "failed");
            MainApplication.getInstance().showToast(R.string.login_error);
            sendLoginFailureEvent(result.getStatus().getStatusCode(),
                    result.getStatus().getStatusMessage(), false);
        }
    }

    private String get_google_access_token(String token) {

        List<NameValuePair> data = new ArrayList<>();
        data.add(new BasicNameValuePair("code", token));
        data.add(new BasicNameValuePair("client_id", MainApplication.getContext().getString(R.string.server_client_id)));
        data.add(new BasicNameValuePair("client_secret", MainApplication.getContext().getString(R.string.server_secret_id)));
        data.add(new BasicNameValuePair("access_type", "offline"));
        data.add(new BasicNameValuePair("grant_type", "authorization_code"));

        NetworkDataProvider.doPostCallAsyncWithUrlEncodedData(Urls.getGoogleConvertTokenUrl(), data, new NetworkAsyncCallback<GoogleOauthResponse>() {
                    @Override
                    public void onNetworkSuccess(final GoogleOauthResponse googleOauthResponse) {
                        MainApplication.getInstance().getMainThreadHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                verifyUserDetails(googleOauthResponse.access_token, false);
                            }
                        });

                    }

                    @Override
                    public void onNetworkFailure(NetworkException ne) {
                        MainApplication.getInstance().getMainThreadHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                MainApplication.getInstance().showToast(R.string.login_error);
                            }
                        });
                        sendLoginFailureEvent(ne.getHttpStatusCode(), ne.getMessage(), false);
                    }


                }
        );


        return token;
    }

    public interface LoginListener {

        void onLoginSuccess();

        void showHideProgress(boolean show, String title);
    }
}
