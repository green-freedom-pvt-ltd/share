package com.sharesmile.share.core;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.OneoffTask;
import com.google.android.gms.gcm.Task;
import com.sharesmile.share.MainApplication;
import com.sharesmile.share.R;
import com.sharesmile.share.User;
import com.sharesmile.share.UserDao;
import com.sharesmile.share.gcm.SyncService;
import com.sharesmile.share.gcm.TaskConstants;
import com.sharesmile.share.utils.Logger;
import com.sharesmile.share.utils.SharedPrefsManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.Arrays;

/**
 * Created by Shine on 19/05/16.
 */
public class LoginImpl {

    private static final int REQUEST_GOOGLE_SIGN_IN = 1001;

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
                .requestEmail()
                .build();

        Context context = getContext();
        if (context != null) {
            mGoogleApiClient = new GoogleApiClient.Builder(context)
                    .enableAutoManage(activityWeakReference != null ? activityWeakReference.get() : fragmentWeakReference.get().getActivity(), mConnectionFailedListener)
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
                    Logger.d("facebook", "success");

                    GraphRequest request = GraphRequest.newMeRequest(
                            loginResult.getAccessToken(),
                            new GraphRequest.GraphJSONObjectCallback() {
                                @Override
                                public void onCompleted(
                                        JSONObject object,
                                        GraphResponse response) {
                                    // Application code
                                    String userEmail = "";
                                    try {
                                        userEmail = object.getString("email");
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    if (!TextUtils.isEmpty(userEmail)) {
                                        Profile profile = Profile.getCurrentProfile();
                                        userLoginSuccess(profile.getName(), userEmail, profile.getProfilePictureUri(320, 320));

                                    } else {
                                        MainApplication.getInstance().showToast(R.string.email_id_required);
                                    }
                                }
                            });
                    Bundle parameters = new Bundle();
                    parameters.putString("fields", "email");
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
                    MainApplication.getInstance().showToast(R.string.login_error);
                }
            });
        }
    }

    private void userLoginSuccess(String name, String userEmail, Uri profilePictureUri) {
        SharedPrefsManager prefsManager = SharedPrefsManager.getInstance();
        prefsManager.setString(Constants.PREF_USER_EMAIL, userEmail);
        prefsManager.setString(Constants.PREF_USER_NAME, name);

        prefsManager.setBoolean(Constants.PREF_IS_LOGIN, true);
        prefsManager.setInt(Constants.PREF_USER_ID, 1);
        User user = new User(1L);
        user.setName(name);
        user.setEmailId(userEmail);
        if (profilePictureUri != null) {
            prefsManager.setString(Constants.PREF_USER_IMAGE, profilePictureUri.toString());
            user.setProfileImageUrl(profilePictureUri.toString());
        }

        UserDao userDao = MainApplication.getInstance().getDbWrapper().getDaoSession().getUserDao();
        userDao.insertOrReplace(user);

        //Sync run data;
        syncRunData();

        mListener.onLoginSuccess();
    }

    private void syncRunData() {
        OneoffTask task = new OneoffTask.Builder()
                .setService(SyncService.class)
                .setTag(TaskConstants.UPDATE_WORKOUT_DATA)
                .setExecutionWindow(0L, 1L)
                .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED).setPersisted(true)
                .build();

        GcmNetworkManager mGcmNetworkManager = GcmNetworkManager.getInstance(getContext().getApplicationContext());
        mGcmNetworkManager.schedule(task);
    }

    private GoogleApiClient.OnConnectionFailedListener mConnectionFailedListener = new GoogleApiClient.OnConnectionFailedListener() {
        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {

        }
    };

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
        //   LoginManager.getInstance().logInWithReadPermissions(fragmentWeakReference.get(), Arrays.asList("public_profile", "email"));
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
            Logger.d("google", "email: " + acct.getEmail() + " Name : " + acct.getDisplayName());
            userLoginSuccess(acct.getDisplayName(), acct.getEmail(), acct.getPhotoUrl());
        } else {
            Logger.d("google", "failed");
            MainApplication.getInstance().showToast(R.string.login_error);
        }
    }

    public interface LoginListener {

        void onLoginSuccess();
    }
}
