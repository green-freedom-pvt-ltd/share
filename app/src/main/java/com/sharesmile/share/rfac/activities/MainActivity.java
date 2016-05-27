package com.sharesmile.share.rfac.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.sharesmile.share.R;
import com.sharesmile.share.core.BaseActivity;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.core.PermissionCallback;
import com.sharesmile.share.rfac.fragments.AboutUsFragment;
import com.sharesmile.share.rfac.fragments.FeedbackFragment;
import com.sharesmile.share.rfac.fragments.OnScreenFragment;
import com.sharesmile.share.rfac.fragments.ProfileFragment;
import com.sharesmile.share.rfac.fragments.SettingsFragment;
import com.sharesmile.share.utils.Logger;
import com.sharesmile.share.utils.SharedPrefsManager;

public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener, SettingsFragment.FragmentInterface {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_CODE_LOGIN = 1001;
    DrawerLayout mDrawerLayout;
    NavigationView mNavigationView;
    Toolbar toolbar;
    private ActionBarDrawerToggle mDrawerToggle;
    private InputMethodManager inputMethodManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        mNavigationView = (NavigationView) findViewById(R.id.shitstuff);
        if (savedInstanceState == null) {
            loadInitialFragment();
        }

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.app_name,
                R.string.app_name);

        mDrawerToggle.syncState();
        mNavigationView.setNavigationItemSelectedListener(this);
        updateNavigationMenu();
    }

    public void updateNavigationMenu() {
        Menu menu = mNavigationView.getMenu();
        MenuItem loginMenu = menu.findItem(R.id.nav_item_login);
        MenuItem profileMenu = menu.findItem(R.id.nav_item_profile);
        if (SharedPrefsManager.getInstance().getBoolean(Constants.PREF_IS_LOGIN)) {
            loginMenu.setVisible(false);
            profileMenu.setVisible(true);
        } else {
            loginMenu.setVisible(true);
            profileMenu.setVisible(false);
        }
    }

  /*  @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }*/

   /* @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        //  if (id == R.id.action_settings) {
        return true;
        // }

        //return super.onOptionsItemSelected(item);
    }*/

    @Override
    public void loadInitialFragment() {
        addFragment(new OnScreenFragment(), false);
    }

    @Override
    public int getFrameLayoutId() {
        return R.id.containerView;
    }

    @Override
    public String getName() {
        return TAG;
    }

    @Override
    public void performOperation(int operationId, Object input) {
        super.performOperation(operationId, input);
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
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
        }
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
            replaceFragment(new ProfileFragment(), true);
        }

        if (menuItem.getItemId() == R.id.nav_item_aboutUs) {
            replaceFragment(new AboutUsFragment(), true);
        }

        if (menuItem.getItemId() == R.id.nav_item_feedback) {
            Logger.d(TAG, "feedback clicked");
            replaceFragment(new FeedbackFragment(), true);
        }

        if (menuItem.getItemId() == R.id.nav_item_settings) {
            Logger.d(TAG, "settings clicked");
            replaceFragment(new SettingsFragment(), true);
        } else if (menuItem.getItemId() == R.id.nav_item_home) {
            showHome();
        } else if (menuItem.getItemId() == R.id.nav_item_login) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.putExtra(LoginActivity.BUNDLE_FROM_MAINACTIVITY, true);
            startActivityForResult(intent, REQUEST_CODE_LOGIN);
        }


        mDrawerLayout.closeDrawers();

        return false;
    }

    public void showHome() {
        replaceFragment(new OnScreenFragment(), true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_LOGIN) {
            updateNavigationMenu();
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
}
