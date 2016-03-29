package com.sharesmile.share.rfac.activities;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.sharesmile.share.R;

public class ShareMainActivity extends AppCompatActivity {
    DrawerLayout mDrawerLayout;
    NavigationView mNavigationView;
    FragmentManager mFragmentManager;
    FragmentTransaction mFragmentTransaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.share_activity_main);

        /**
         *Setup the DrawerLayout and NavigationView
         */

        mDrawerLayout = (DrawerLayout) findViewById(R.id.shareDrawerLayout);
        mNavigationView = (NavigationView) findViewById(R.id.shitstuff) ;

       /**
        * Inflate the first frag*/

            mFragmentManager = getSupportFragmentManager();
            mFragmentTransaction = mFragmentManager.beginTransaction();
            mFragmentTransaction.replace(R.id.containerView,new ShareTabFragment()).commit();

        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                mDrawerLayout.closeDrawers();



                if (menuItem.getItemId() == R.id.nav_item_home) {
                    FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.shareDrawerLayout,new ShareTabFragment()).commit();

                }

                if (menuItem.getItemId() == R.id.nav_item_profile) {
                    FragmentTransaction xfragmentTransaction = mFragmentManager.beginTransaction();
                    xfragmentTransaction.replace(R.id.shareDrawerLayout,new ProfileFragment()).addToBackStack("tag").commit();
                }

                if (menuItem.getItemId() == R.id.nav_item_aboutUs) {
                    FragmentTransaction xfragmentTransaction = mFragmentManager.beginTransaction();
                    xfragmentTransaction.replace(R.id.shareDrawerLayout,new AboutUsFragment()).addToBackStack("tag").commit();
                }

                if (menuItem.getItemId() == R.id.nav_item_policies) {
                    FragmentTransaction xfragmentTransaction = mFragmentManager.beginTransaction();
                    xfragmentTransaction.replace(R.id.shareDrawerLayout,new PoliciesFragment()).addToBackStack("tag").commit();
                }

                if (menuItem.getItemId() == R.id.nav_item_feedback) {
                    FragmentTransaction xfragmentTransaction = mFragmentManager.beginTransaction();
                    xfragmentTransaction.replace(R.id.shareDrawerLayout,new FeedbackFragment()).addToBackStack("tag").commit();
                }

                if (menuItem.getItemId() == R.id.nav_item_settings) {
                    FragmentTransaction xfragmentTransaction = mFragmentManager.beginTransaction();
                    xfragmentTransaction.replace(R.id.shareDrawerLayout,new SettingsFragment()).addToBackStack("tag").commit();
                }

                if (menuItem.getItemId() == R.id.nav_item_logout) {
                    FragmentTransaction xfragmentTransaction = mFragmentManager.beginTransaction();
                    xfragmentTransaction.replace(R.id.shareDrawerLayout,new LogoutFragment()).addToBackStack("tag").commit();
                }


                return false;
            }

        });



        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this,mDrawerLayout, toolbar,R.string.app_name,
                R.string.app_name);

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mDrawerToggle.syncState();

    }
}