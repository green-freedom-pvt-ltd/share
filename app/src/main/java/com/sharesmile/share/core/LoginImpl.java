package com.sharesmile.share.core;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

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
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sharesmile.share.MainApplication;
import com.sharesmile.share.R;
import com.sharesmile.share.User;
import com.sharesmile.share.UserDao;
import com.sharesmile.share.network.NetworkAsyncCallback;
import com.sharesmile.share.network.NetworkDataProvider;
import com.sharesmile.share.network.NetworkException;
import com.sharesmile.share.rfac.models.GoogleOauthResponse;
import com.sharesmile.share.sync.SyncHelper;
import com.sharesmile.share.utils.BasicNameValuePair;
import com.sharesmile.share.utils.JsonHelper;
import com.sharesmile.share.utils.Logger;
import com.sharesmile.share.utils.NameValuePair;
import com.sharesmile.share.utils.SharedPrefsManager;
import com.sharesmile.share.utils.Urls;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.mixpanel.android.mpmetrics.MixpanelAPI;


import org.json.JSONException;
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
    MixpanelAPI mMixpanel;

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
                .requestEmail().requestIdToken(getContext().getString(R.string.server_client_id))
                .requestServerAuthCode(getContext().getString(R.string.server_client_id), false)
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
                    Logger.d("facebook", "success " + loginResult.getAccessToken().getToken());
                    final String token = loginResult.getAccessToken().getToken();

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
                                        verifyUserDetails(userEmail, token, true);
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
                    error.printStackTrace();
                    MainApplication.getInstance().showToast(R.string.login_error);
                }
            });
        }
    }

    private void verifyUserDetails(String userEmail, String token, boolean isFbLogin) {

        mListener.showHideProgress(true, getContext().getString(R.string.login));
        Map<String, String> header = new HashMap<>();
        if (isFbLogin) {
            header.put("Authorization", "Bearer facebook " + token);
        } else {
            header.put("Authorization", "Bearer google-oauth2 " + token);
        }

        NetworkDataProvider.doGetCallAsync(Urls.getLoginUrl(userEmail), header, new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.i("TAG", "Login error ");
                MainApplication.getInstance().getMainThreadHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        mListener.showHideProgress(false, null);
                    }
                });
            }

            @Override
            public void onResponse(Response response) throws IOException {
                String responseString = response.body().string();
                Logger.d("LoginImpl", "onResponse: " + responseString);
                JsonArray array = JsonHelper.StringToJsonArray(responseString);
                final JsonObject element = array.get(0).getAsJsonObject();
                Log.i("LoginImpl", "element: " + element.toString());
                MainApplication.getInstance().getMainThreadHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        userLoginSuccess(element);
                    }
                });

            }
        });

    }

    private void userLoginSuccess(JsonObject response) {
        SharedPrefsManager prefsManager = SharedPrefsManager.getInstance();
        String name = "";
        if (response.has("first_name")) {
            name = response.get("first_name").getAsString();
            prefsManager.setString(Constants.PREF_USER_NAME, name);
        }
        String userEmail = response.get("email").getAsString();
        prefsManager.setString(Constants.PREF_USER_EMAIL, userEmail);

        prefsManager.setBoolean(Constants.PREF_IS_LOGIN, true);
        int user_id = response.get("user_id").getAsInt();
        prefsManager.setInt(Constants.PREF_USER_ID, user_id);

        String token = response.get("auth_token").getAsString();
        prefsManager.setString(Constants.PREF_AUTH_TOKEN, token);
        Logger.i(TAG, "token : " + token);

        String mobile_number = "";
        if (response.has("phone_number")) {
            mobile_number = JsonHelper.getValueOrNone(response, "phone_number");
        }
        String gender = "";
        if (response.has("gender_user")) {
            gender = JsonHelper.getValueOrNone(response, "gender_user");
        }
        if (response.has("sign_up")) {
            Boolean isSignUpUser = false;
            isSignUpUser = response.get("sign_up").getAsBoolean();
            SharedPrefsManager.getInstance().setBoolean(Constants.PREF_IS_SIGN_UP_USER, isSignUpUser);
        }

        User user = new User((long) user_id);
        user.setName(name);
        user.setEmailId(userEmail);
        user.setMobileNO(mobile_number);
        if (!TextUtils.isEmpty(gender)) {
            user.setGender(gender.equalsIgnoreCase("male") ? "m" : "f");
        }

        String profilePictureUri = JsonHelper.getValueOrNone(response, "social_thumb");
        if (profilePictureUri != null) {
            prefsManager.setString(Constants.PREF_USER_IMAGE, profilePictureUri.toString());
            user.setProfileImageUrl(profilePictureUri.toString());
        }

        UserDao userDao = MainApplication.getInstance().getDbWrapper().getDaoSession().getUserDao();
        userDao.insertOrReplace(user);

        //show Toast confirmation
        Toast.makeText(MainApplication.getContext(), "Logged in as " + name, Toast.LENGTH_SHORT).show();

        //Sync run data;
        SyncHelper.fetchRunData();

        mListener.onLoginSuccess();


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
            Logger.d("google", "email: " + acct.getEmail() + " Name : " + acct.getDisplayName() + " token " + acct.getIdToken() + "auth :  " + acct.getServerAuthCode());
            get_google_access_token(acct.getEmail(), acct.getServerAuthCode());
        } else {
            Logger.d("google", "failed");
            MainApplication.getInstance().showToast(R.string.login_error);
        }
    }

    private String get_google_access_token(final String email, String token) {

        List<NameValuePair> data = new ArrayList<>();
        data.add(new BasicNameValuePair("code", token));
        data.add(new BasicNameValuePair("client_id", getContext().getString(R.string.server_client_id)));
        data.add(new BasicNameValuePair("client_secret", getContext().getString(R.string.server_secret_id)));
        data.add(new BasicNameValuePair("access_type", "offline"));
        data.add(new BasicNameValuePair("grant_type", "authorization_code"));

        NetworkDataProvider.doPostCallAsync(Urls.getGoogleConvertTokenUrl(), data, new NetworkAsyncCallback<GoogleOauthResponse>() {
                    @Override
                    public void onNetworkSuccess(final GoogleOauthResponse googleOauthResponse) {
                        MainApplication.getInstance().getMainThreadHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                verifyUserDetails(email, googleOauthResponse.access_token, false);
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
