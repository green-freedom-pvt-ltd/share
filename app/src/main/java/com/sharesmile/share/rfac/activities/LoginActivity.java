package com.sharesmile.share.rfac.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.gcm.SyncService;
import com.sharesmile.share.gcm.TaskConstants;
import com.sharesmile.share.utils.Logger;
import com.sharesmile.share.utils.SharedPrefsManager;
import com.sharesmile.share.views.MRTextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by apurvgandhwani on 4/5/2016.
 */
public class LoginActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {

    private static final int REQUEST_GOOGLE_SIGN_IN = 1001;
    public static final String BUNDLE_FROM_MAINACTIVITY = "is_from_mainactivity";
    @BindView(R.id.btn_login_fb)
    LinearLayout mFbLoginButton;

    @BindView(R.id.btn_login_google)
    LinearLayout mGoogleLoginButton;

    @BindView(R.id.tv_welcome_skip)
    MRTextView tv_skip;
    private CallbackManager callbackManager;
    private GoogleApiClient mGoogleApiClient;
    private boolean isFromMainActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        isFromMainActivity = getIntent().getBooleanExtra(BUNDLE_FROM_MAINACTIVITY, false);

        if (!SharedPrefsManager.getInstance().getBoolean(Constants.PREF_IS_LOGIN) && (isFromMainActivity || !SharedPrefsManager.getInstance().getBoolean(Constants.PREF_LOGIN_SKIP, false))) {
            initializeFbLogin();
            initializeGoogleLogin();
            setContentView(R.layout.welcome_screen);
            ButterKnife.bind(this);
            initUi();
        } /*else if(RunTracker.isWorkoutActive()) {
            Intent intent = new Intent(this, TrackerActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
           // intent.putExtra(TrackerActivity.RUN_IN_TEST_MODE, (Boolean) input);
            startActivity(intent);
        }*/ else {
            startMainActivity();
        }
    }

    private void initializeGoogleLogin() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

    }

    private void initializeFbLogin() {
        FacebookSdk.sdkInitialize(getApplicationContext());
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

    private void initUi() {
        tv_skip.setOnClickListener(this);
        mFbLoginButton.setOnClickListener(this);
        mGoogleLoginButton.setOnClickListener(this);

        //init fb login
        TextView mFbText = (TextView) mFbLoginButton.findViewById(R.id.title);
        mFbText.setText(getString(R.string.logn_with_fb));
        mFbText.setTextColor(getResources().getColor(R.color.denim_blue));

        ImageView mFbImage = (ImageView) mFbLoginButton.findViewById(R.id.login_image);
        mFbImage.setImageResource(R.drawable.logo_fb);


        //init Google login
        TextView mGText = (TextView) mGoogleLoginButton.findViewById(R.id.title);
        mGText.setText(getString(R.string.logn_with_google));
        mGText.setTextColor(getResources().getColor(R.color.pale_red));

        ImageView mGImage = (ImageView) mGoogleLoginButton.findViewById(R.id.login_image);
        mGImage.setImageResource(R.drawable.login_google);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login_fb:
                performFbLogin();
                break;
            case R.id.tv_welcome_skip:
                SharedPrefsManager prefsManager = SharedPrefsManager.getInstance();
                prefsManager.setBoolean(Constants.PREF_LOGIN_SKIP, true);
                startMainActivity();
                break;
            case R.id.btn_login_google:
                performGoogleLogin();
                break;

        }
    }

    private void performGoogleLogin() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, REQUEST_GOOGLE_SIGN_IN);
    }

    private void performFbLogin() {

        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile", "email"));

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
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

        startMainActivity();
    }

    private void syncRunData() {
        OneoffTask task = new OneoffTask.Builder()
                .setService(SyncService.class)
                .setTag(TaskConstants.UPDATE_WORKOUT_DATA)
                .setExecutionWindow(0L, 1L)
                .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED).setPersisted(true)
                .build();

        GcmNetworkManager mGcmNetworkManager = GcmNetworkManager.getInstance(getApplicationContext());
        mGcmNetworkManager.schedule(task);
    }

    private void startMainActivity() {
        if (!isFromMainActivity) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else {
            finish();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
