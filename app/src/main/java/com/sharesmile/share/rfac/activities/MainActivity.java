package com.sharesmile.share.rfac.activities;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sharesmile.share.BuildConfig;
import com.sharesmile.share.CauseDataStore;
import com.sharesmile.share.Events.DBEvent;
import com.sharesmile.share.Events.LeagueBoardDataUpdated;
import com.sharesmile.share.LeaderBoardDataStore;
import com.sharesmile.share.MainApplication;
import com.sharesmile.share.R;
import com.sharesmile.share.TrackerActivity;
import com.sharesmile.share.analytics.Analytics;
import com.sharesmile.share.analytics.events.AnalyticsEvent;
import com.sharesmile.share.analytics.events.Event;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.core.IFragmentController;
import com.sharesmile.share.core.PermissionCallback;
import com.sharesmile.share.core.ToolbarActivity;
import com.sharesmile.share.gps.WorkoutSingleton;
import com.sharesmile.share.gps.models.WorkoutDataImpl;
import com.sharesmile.share.pushNotification.NotificationConsts;
import com.sharesmile.share.rfac.fragments.GlobalLeaderBoardFragment;
import com.sharesmile.share.rfac.fragments.HomeScreenFragment;
import com.sharesmile.share.rfac.fragments.LeagueBoardFragment;
import com.sharesmile.share.rfac.fragments.ProfileStatsFragment;
import com.sharesmile.share.rfac.fragments.SettingsFragment;
import com.sharesmile.share.rfac.fragments.ShareFragment;
import com.sharesmile.share.rfac.fragments.WebViewFragment;
import com.sharesmile.share.sync.SyncHelper;
import com.sharesmile.share.utils.CustomTypefaceSpan;
import com.sharesmile.share.utils.Logger;
import com.sharesmile.share.utils.ShareImageLoader;
import com.sharesmile.share.utils.ShareUtils;
import com.sharesmile.share.utils.SharedPrefsManager;
import com.sharesmile.share.utils.Utils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import Models.CampaignList;
import butterknife.ButterKnife;

import static com.sharesmile.share.MainApplication.getContext;
import static com.sharesmile.share.core.Constants.REQUEST_CODE_LOGIN;


public class MainActivity extends ToolbarActivity implements NavigationView.OnNavigationItemSelectedListener, SettingsFragment.FragmentInterface {

    private static final String TAG = "MainActivity";
    public static final String INTENT_STOP_RUN = "intent_stop_run";

    DrawerLayout mDrawerLayout;
    NavigationView mNavigationView;

    private ActionBarDrawerToggle mDrawerToggle;

    private InputMethodManager inputMethodManager;
    private boolean isAppUpdateDialogShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Logger.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        boolean isFirstTimeUser = SharedPrefsManager.getInstance().getBoolean(Constants.PREF_FIRST_TIME_USER, true);

        if (isFirstTimeUser){
            // Very first launch of app, show onboarding
            startOnboardingActivity();
            finish();
        }else {
            Boolean userLogin = SharedPrefsManager.getInstance().getBoolean(Constants.PREF_IS_LOGIN, false);
            Boolean isLoginSkip = SharedPrefsManager.getInstance().getBoolean(Constants.PREF_LOGIN_SKIP, false);
            Logger.d(TAG, "userLogin = " + userLogin + ", isLoginSkip = " + isLoginSkip);
            if (!userLogin && !isLoginSkip) {
                startLoginActivity();
            } else if (WorkoutSingleton.getInstance().isWorkoutActive()) {
                boolean intentStopRun = getIntent().getBooleanExtra(INTENT_STOP_RUN, false);
                if (intentStopRun){
                    WorkoutSingleton.getInstance().setToShowEndRunDialog(true);
                }
                startTrackingActivity();
            } else {
                Logger.d(TAG, "render MainActivity UI");
                // Normal launch of MainActivity, render its layout
                EventBus.getDefault().register(this);
                ButterKnife.bind(this);
                mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
                mNavigationView = (NavigationView) findViewById(R.id.shitstuff);
                if (savedInstanceState == null) {
                    loadInitialFragment();
                }

                mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, getToolbar(), R.string.app_name,
                        R.string.app_name);

                mDrawerToggle.syncState();
                Logger.d(TAG, "Will setNavigationItemSelectedListener on mNavigationView");
                mNavigationView.setNavigationItemSelectedListener(this);
                updateNavigationMenu();
                checkAppVersionAndShowUpdatePopupIfRequired();
                SyncHelper.syncCampaignData(getApplicationContext());
                handleNotificationIntent();
                Analytics.getInstance().setUserProperties();
            }
        }
    }

    private void startTrackingActivity(){
        Intent intent = new Intent(this, TrackerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void startLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        SharedPrefsManager.getInstance().setBoolean(Constants.PREF_FIRST_TIME_USER, false);
        startActivity(intent);
        finish();
    }

    private void startOnboardingActivity() {
        Intent intent = new Intent(this, OnBoardingActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateNavigationMenu();
    }

    private void handleNotificationIntent() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String screen = bundle.getString(NotificationConsts.KEY_SCREEN);
            if (!TextUtils.isEmpty(screen)) {
                if (screen.equals(NotificationConsts.Screen.MESSAGE_CENTER)) {
                    showMessageCenter();
                } else if (screen.equals(NotificationConsts.Screen.PROFILE)) {
                    if (!MainApplication.isLogin()) {
                        showLoginActivity();
                    } else {
                        replaceFragment(new ProfileStatsFragment(), true);
                    }
                } else if (screen.equals(NotificationConsts.Screen.LEADERBOARD)) {
                    if (!MainApplication.isLogin()) {
                        showLoginActivity();
                    } else {
                        showLeaderBoard();
                    }
                }
            }
        }
    }

    private void checkAppVersionAndShowUpdatePopupIfRequired() {

        int latestAppVersion = SharedPrefsManager.getInstance().getInt(Constants.PREF_LATEST_APP_VERSION, 0);
        final boolean forceUpdate = SharedPrefsManager.getInstance().getBoolean(Constants.PREF_FORCE_UPDATE, false);

        boolean showAppUpdateDialog = SharedPrefsManager.getInstance().getBoolean(Constants.PREF_SHOW_APP_UPDATE_DIALOG, false);
        String message = SharedPrefsManager.getInstance().getString(Constants.PREF_APP_UPDATE_MESSAGE);

        if (!showAppUpdateDialog || latestAppVersion <= BuildConfig.VERSION_CODE) {
            return;
        }

        isAppUpdateDialogShown = true;
        AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle(getString(R.string.title_app_update))
                .setMessage(message).
                        setPositiveButton(getString(R.string.update), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Utils.redirectToPlayStore(MainActivity.this);
                                AnalyticsEvent.create(Event.ON_CLICK_APP_UPDATE_NOW).buildAndDispatch();
                            }
                        });

        if (forceUpdate) {
            builder.setCancelable(false);
        } else {
            SharedPrefsManager.getInstance().setBoolean(Constants.PREF_SHOW_APP_UPDATE_DIALOG, false);
            builder.setNegativeButton(getString(R.string.later), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
        }

        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                AnalyticsEvent.create(Event.ON_DISMISS_APP_UPDATE_POPUP).buildAndDispatch();
                if (forceUpdate){
                    finish();
                }
            }
        });
        builder.show();
        AnalyticsEvent.create(Event.ON_LOAD_APP_UPDATE_POPUP).buildAndDispatch();
    }

    public void updateNavigationMenu() {
        Menu menu = mNavigationView.getMenu();
        MenuItem loginMenu = menu.findItem(R.id.nav_item_login);
        MenuItem profileMenu = menu.findItem(R.id.nav_item_profile);
        MenuItem leaderboardMenu = menu.findItem(R.id.nav_item_leaderboard);
        MenuItem impactLeagueMenu = menu.findItem(R.id.nav_item_impact_league);
        if (SharedPrefsManager.getInstance().getBoolean(Constants.PREF_IS_LOGIN)) {
            loginMenu.setVisible(false);
            profileMenu.setVisible(true);
            leaderboardMenu.setVisible(true);
            impactLeagueMenu.setVisible(true);
//            if (LeaderBoardDataStore.getInstance().toShowLeague()){
//                impactLeagueMenu.setVisible(true);
//            }else {
//                impactLeagueMenu.setVisible(false);
//            }
        } else {
            loginMenu.setVisible(true);
            profileMenu.setVisible(false);
            leaderboardMenu.setVisible(false);
            impactLeagueMenu.setVisible(false);
        }

        for (int i = 0; i < menu.size(); i++) {
            MenuItem mi = menu.getItem(i);
            applyFontToMenuItem(mi);
        }
    }

    private void applyFontToMenuItem(MenuItem mi) {
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/Montserrat-Light.otf");
        SpannableString mNewTitle = new SpannableString(mi.getTitle());
        mNewTitle.setSpan(new CustomTypefaceSpan("", font), 0, mNewTitle.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        mi.setTitle(mNewTitle);
    }

    private void loadInitialFragment() {
        addFragment(new HomeScreenFragment(), false);
        boolean showProfile = getIntent().getBooleanExtra(Constants.BUNDLE_SHOW_RUN_STATS, false);
        if (showProfile && MainApplication.isLogin()) {
            replaceFragment(new ProfileStatsFragment(), true);
        }
    }

    private void showLeagueBoard(){
        LeagueBoardFragment leageBoardFragment = LeagueBoardFragment.getInstance();
        replaceFragment(leageBoardFragment , true);
    }

    @Override
    public int getFrameLayoutId() {
        return R.id.main_frame_layout;
    }

    @Override
    public String getName() {
        return TAG;
    }

    @Override
    public void exit() {
        finish();
    }

    @Override
    public void requestPermission(int requestCode, PermissionCallback permissionsCallback) {

    }

    @Override
    public void unregisterForPermissionRequest(int requestCode) {

    }

    @Override
    public void updateToolBar(String title, boolean showAsUpEnable) {
        super.updateToolBar(title, showAsUpEnable);
        showHomeAsUpEnable(showAsUpEnable);
    }

    public void showHomeAsUpEnable(boolean showUp) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            if (showUp) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setHomeButtonEnabled(true);
                actionBar.setDisplayShowHomeEnabled(true);
                mDrawerToggle.setDrawerIndicatorEnabled(false);
                mDrawerToggle.setToolbarNavigationClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onBackPressed();
                    }
                });
            } else {
                mDrawerToggle.setDrawerIndicatorEnabled(true);
                mDrawerLayout.addDrawerListener(mDrawerToggle);
            }
            mDrawerToggle.syncState();
        }
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
            mDrawerLayout.closeDrawer(Gravity.LEFT);
            return;
        }

        hideKeyboard(null);
        if (getFragmentManager().getBackStackEntryCount() == 1) {
            ActivityCompat.finishAffinity(this);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {

        Logger.d(TAG, "onNavigationItemSelected");

        if (menuItem.getItemId() == R.id.nav_item_profile) {
            replaceFragment(new ProfileStatsFragment(), true);
            AnalyticsEvent.create(Event.ON_SELECT_PROFILE_MENU)
                    .buildAndDispatch();
        }

        if (menuItem.getItemId() == R.id.nav_item_aboutUs) {
            replaceFragment(WebViewFragment.getInstance(WebViewFragment.DISPLAY_ABOUT_US), true);
        }

        if (menuItem.getItemId() == R.id.nav_item_settings) {
            Logger.d(TAG, "settings clicked");
            replaceFragment(new SettingsFragment(), true);
        } else if (menuItem.getItemId() == R.id.nav_item_home) {
            showHome();
            AnalyticsEvent.create(Event.ON_SELECT_HOME_MENU)
                    .buildAndDispatch();
        } else if (menuItem.getItemId() == R.id.nav_item_login) {
            showLoginActivity();
        } else if (menuItem.getItemId() == R.id.nav_item_help) {
            performOperation(OPEN_HELP_CENTER,false);
            AnalyticsEvent.create(Event.ON_SELECT_HELP_MENU)
                    .buildAndDispatch();
        } else if (menuItem.getItemId() == R.id.nav_item_share) {
            replaceFragment(ShareFragment.newInstance(WorkoutDataImpl.getDummyWorkoutData(), CauseDataStore.getInstance().getCausesToShow().get(0)), true);
//            share();
            AnalyticsEvent.create(Event.ON_SELECT_SHARE_MENU)
                    .buildAndDispatch();
        } else if (menuItem.getItemId() == R.id.nav_item_leaderboard) {
            showLeaderBoard();
            AnalyticsEvent.create(Event.ON_SELECT_LEADERBOARD_MENU)
                    .buildAndDispatch();
        } else if (menuItem.getItemId() == R.id.nav_item_impact_league) {
            if (LeaderBoardDataStore.getInstance().toShowLeague()){
                showLeagueBoard();
            }else {
                performOperation(IFragmentController.SHOW_LEAGUE_ACTIVITY, null);
            }
            AnalyticsEvent.create(Event.ON_CLICK_IMPACT_LEAGUE_NAVIGATION_MENU)
                    .put("team_id", LeaderBoardDataStore.getInstance().getMyTeamId())
                    .put("league_name", LeaderBoardDataStore.getInstance().getLeagueName())
                    .buildAndDispatch();
        }

        mDrawerLayout.closeDrawers();

        return false;
    }

    private void showLeaderBoard() {
        replaceFragment(GlobalLeaderBoardFragment.getInstance(), true);
    }

    private void showLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivityForResult(intent, REQUEST_CODE_LOGIN);
    }

    private void share() {
        Utils.share(this, getString(R.string.share_msg));
    }

    public void showHome() {
        replaceFragment(new HomeScreenFragment(), true);
    }

    @Override
    public void performOperation(int operationId, Object input) {
        switch (operationId) {
            case OPEN_DRAWER:
                mDrawerLayout.openDrawer(Gravity.LEFT);
                break;
            default:
                super.performOperation(operationId, input);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_LOGIN) {
            Logger.d(TAG, "onActivityResult with REQUEST_CODE_LOGIN");
            updateNavigationMenu();
        } else if (requestCode == REQUEST_LEAGUE_REGISTRATION) {
            if (resultCode == RESULT_OK) {
                replaceFragment(LeagueBoardFragment.getInstance(), true);
            }
        }
    }

    protected void hideKeyboard(View view) {
        if (inputMethodManager == null) {
            inputMethodManager = (InputMethodManager) getSystemService(
                    Context.INPUT_METHOD_SERVICE);
        }


        if (view == null) {
            view = this.getCurrentFocus();
        }
        if (view != null) {
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

    }

    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(DBEvent.CampaignDataUpdated campaignDataUpdated) {
        showPromoModal(campaignDataUpdated.getCampaign());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(LeagueBoardDataUpdated dataUpdated) {
        if (dataUpdated.isSuccess()){
            updateNavigationMenu();
        }
    }

    public void showPromoModal(final CampaignList.Campaign campaign) {

        if (campaign == null) {
            return;
        }
        //check for eligibility of campaign;
        boolean needToShowCampaign = true;

        Boolean isCampaignAlreadyShown = SharedPrefsManager.getInstance().getBoolean(Constants.PREF_CAMPAIGN_SHOWN_ONCE, false);
        if (!campaign.isAlways()) {
            needToShowCampaign = needToShowCampaign && !isCampaignAlreadyShown;
        }

        if (campaign.getShowOnSignUp()) {
            Boolean signUpUser = SharedPrefsManager.getInstance().getBoolean(Constants.PREF_IS_SIGN_UP_USER, false);
            needToShowCampaign = needToShowCampaign && signUpUser;
        }

        needToShowCampaign = needToShowCampaign && !isAppUpdateDialogShown;


        boolean isModelAlreadyShown = MainApplication.getInstance().isModelShown();
        needToShowCampaign = needToShowCampaign && !isModelAlreadyShown;
        if (!needToShowCampaign) {
            return;
        }
        MainApplication.getInstance().setModelShown();

        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_promotion);
        Button share = (Button) dialog.findViewById(R.id.share);
        TextView title = (TextView) dialog.findViewById(R.id.title);
        TextView message = (TextView) dialog.findViewById(R.id.description);
        final LinearLayout progressView = (LinearLayout) dialog.findViewById(R.id.progress_view);

        ImageView image = (ImageView) dialog.findViewById(R.id.image_run);
        TextView skip = (TextView) dialog.findViewById(R.id.skip);
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        ShareImageLoader.getInstance().loadImage(campaign.getImageUrl(), image,
                ContextCompat.getDrawable(getContext(), R.drawable.cause_image_placeholder));
        share.setText(campaign.getButtonText());
        title.setText(campaign.getTitle());
        message.setText(campaign.getDescritption());
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                progressView.setVisibility(View.VISIBLE);
                Picasso.with(MainActivity.this).load(campaign.getImageUrl()).into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        progressView.setVisibility(View.GONE);

                        showBottomDialog(campaign.getShareTemplate(), Utils.getLocalBitmapUri(bitmap, MainActivity.this), Uri.parse(campaign.getImageUrl()));
                    }

                    @Override
                    public void onBitmapFailed(Drawable errorDrawable) {
                        progressView.setVisibility(View.GONE);
                        showBottomDialog(campaign.getShareTemplate(), Uri.parse(campaign.getImageUrl()), Uri.parse(campaign.getImageUrl()));

                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                    }
                });
            }
        });

        SharedPrefsManager.getInstance().setBoolean(Constants.PREF_CAMPAIGN_SHOWN_ONCE, true);
        dialog.show();

    }

    private void showBottomDialog(final String message, final Uri pathUrl, final Uri imageNetworkUrl) {

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().from(this).inflate(R.layout.share_message_layout, null);
        View fbView = view.findViewById(R.id.fb);
        View whatsAppView = view.findViewById(R.id.whatsapp);
        bindShareData(fbView, R.drawable.facebook, getString(R.string.facebook));
        bindShareData(whatsAppView, R.drawable.whatsapp, getString(R.string.whatsapp));
        dialog.setContentView(view);

        fbView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShareUtils.shareOnFb(MainActivity.this, message, imageNetworkUrl);
            }
        });

        whatsAppView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = ShareUtils.shareOnWhatsAppIntent(MainActivity.this, message, pathUrl);
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(MainActivity.this, "Whats app not installed ", Toast.LENGTH_LONG).show();
                }
            }
        });
        dialog.show();
    }

    private void bindShareData(View view, int imgRes, String appName) {
        ImageView image = (ImageView) view.findViewById(R.id.logo);
        image.setImageResource(imgRes);
        TextView textView = (TextView) view.findViewById(R.id.app_name);
        textView.setText(appName);
    }


}
