package com.sharesmile.share.rfac.activities;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.sharesmile.share.R;
import com.sharesmile.share.rfac.fragments.AboutUsFragment;
import com.sharesmile.share.rfac.fragments.FeedbackFragment;
import com.sharesmile.share.rfac.fragments.LogoutFragment;
import com.sharesmile.share.rfac.fragments.OnScreenFragment;
import com.sharesmile.share.rfac.fragments.ProfileFragment;
import com.sharesmile.share.rfac.fragments.SettingsFragment;
import com.sharesmile.share.utils.Logger;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    DrawerLayout mDrawerLayout;
    NavigationView mNavigationView;
    FragmentManager mFragmentManager;
    FragmentTransaction mFragmentTransaction;
    Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        mNavigationView = (NavigationView) findViewById(R.id.shitstuff);
        mFragmentManager = getSupportFragmentManager();
        mFragmentTransaction = mFragmentManager.beginTransaction();
        mFragmentTransaction.replace(R.id.containerView, new OnScreenFragment()).commit();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        final ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.app_name,
                R.string.app_name);
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mDrawerToggle.syncState();

        //getSupportActionBar().setElevation(0);


        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {

                Logger.d(TAG, "onNavigationItemSelected");

                if (menuItem.getItemId() == R.id.nav_item_profile) {
                    FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.drawerLayout, new ProfileFragment()).addToBackStack("tag").commit();

                }

                if (menuItem.getItemId() == R.id.nav_item_aboutUs) {
                    FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.drawerLayout, new AboutUsFragment()).addToBackStack("tag").commit();
                }

                if (menuItem.getItemId() == R.id.nav_item_feedback) {
                    Logger.d(TAG, "feedback clicked");
                    FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.drawerLayout, new FeedbackFragment()).addToBackStack("tag").commit();
                }

                if (menuItem.getItemId() == R.id.nav_item_settings) {
                    Logger.d(TAG, "settings clicked");
                    FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.drawerLayout, new SettingsFragment()).addToBackStack("tag").commit();
                }

                if (menuItem.getItemId() == R.id.nav_item_logout) {
                    FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.drawerLayout, new LogoutFragment()).addToBackStack("tag").commit();
                }
                mDrawerLayout.closeDrawers();


                return false;
            }

        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
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
    }
}
