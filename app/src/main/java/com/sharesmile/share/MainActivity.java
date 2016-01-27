package com.sharesmile.share;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.sharesmile.share.core.BaseFragment;
import com.sharesmile.share.core.IFragmentController;
import com.sharesmile.share.drawer.DrawerMenuAdapter;
import com.sharesmile.share.events.EventsFragment;
import com.sharesmile.share.news.NewsFragment;
import com.sharesmile.share.orgs.RunFragment;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements IFragmentController{

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    private DrawerLayout drawerLayout;
    private ListView drawerList;

    private static final ArrayList<String> MENU_ITEMS = new ArrayList<String>(){
        {
            add("Home");
            add("Profile");
            add("Feedback");
            add("Logout");
        }
    };

    private static final int HOME = 0;
    private static final int PROFILE = 1;
    private static final int FEEDBACK = 2;
    private static final int LOGOUT = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        drawerList = (ListView) findViewById(R.id.drawer_list_view);
        drawerList.setAdapter(new DrawerMenuAdapter(this, R.layout.drawer_list_item, MENU_ITEMS));
        drawerList.setOnItemClickListener(new DrawerItemClickListener());
    }

    private void setupViewPager(ViewPager viewPager) {
        TabViewPagerAdapter adapter = new TabViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(EventsFragment.newInstance("Participate in Events"), "EVENTS");
        adapter.addFragment(NewsFragment.newInstance("Read good stuff"), "NEWS");
        adapter.addFragment(RunFragment.newInstance("Run for a cause"), "RUN");
        viewPager.setOffscreenPageLimit(2);
        viewPager.setAdapter(adapter);
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void addFragmentInDefaultLayout(BaseFragment fragmentToBeLoaded) {
        // Allow state loss by default
        addFragmentInDefaultLayout(fragmentToBeLoaded, true, true);
    }

    public void addFragmentInDefaultLayout(BaseFragment fragmentToBeLoaded,
                                           boolean addToBackStack) {
        // Allow state loss by default
        addFragmentInDefaultLayout(fragmentToBeLoaded, addToBackStack, true);
    }

    public void addFragmentInDefaultLayout(BaseFragment fragmentToBeLoaded, boolean addToBackStack,
                                           boolean allowStateLoss) {
        if (!getSupportFragmentManager().isDestroyed()) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction
                    .add(R.id.mainFrameLayout, fragmentToBeLoaded, fragmentToBeLoaded.getName());
            if (addToBackStack) {
                fragmentTransaction.addToBackStack(fragmentToBeLoaded.getName());
            }

            if (allowStateLoss) {
                fragmentTransaction.commitAllowingStateLoss();
            } else {
                fragmentTransaction.commit();
            }
        } else {
            Log.e(TAG, "addFragmentInDefaultLayout: Actvity Destroyed, won't perform FT to load" +
                    " Fragment " + fragmentToBeLoaded.getName());
        }
    }

    public void replaceFragmentInDefaultLayout(BaseFragment fragmentToBeLoaded) {
        // Allow state loss by default
        replaceFragmentInDefaultLayout(fragmentToBeLoaded, true, true);
    }

    public void replaceFragmentInDefaultLayout(BaseFragment fragmentToBeLoaded,
                                               boolean addToBackStack) {
        // Allow state loss by default
        replaceFragmentInDefaultLayout(fragmentToBeLoaded, addToBackStack, true);
    }

    public void replaceFragmentInDefaultLayout(BaseFragment fragmentToBeLoaded,
                                               boolean addToBackStack, boolean allowStateLoss) {
        if (!getSupportFragmentManager().isDestroyed()) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.mainFrameLayout, fragmentToBeLoaded,
                    fragmentToBeLoaded.getName());
            if (addToBackStack) {
                fragmentTransaction.addToBackStack(fragmentToBeLoaded.getName());
            }
            if (allowStateLoss) {
                fragmentTransaction.commitAllowingStateLoss();
            } else {
                fragmentTransaction.commit();
            }
        } else {
            Log.e(TAG, "replaceFragmentInDefaultLayout: Actvity Destroyed, won't perform FT to load" +
                    " Fragment " + fragmentToBeLoaded.getName());
        }
    }

    @Override
    public void loadFragment(BaseFragment fragment) {

    }

    public class DrawerItemClickListener implements ListView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            handleItemClick(position);
        }

        public void handleItemClick(int position){
            drawerLayout.closeDrawer(Gravity.LEFT);
            switch (position){
                case HOME:
                    MainApplication.showToast("HOME");
                    break;
                case PROFILE:
                    MainApplication.showToast("PROFILE");
                    break;
                case FEEDBACK:
                    MainApplication.showToast("FEEDBACK");
                    break;
                case LOGOUT:
                    MainApplication.showToast("LOGOUT");
                    break;

            }
        }
    }
}
