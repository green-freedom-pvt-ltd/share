package com.sharesmile.share;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.sharesmile.share.core.BaseActivity;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.drawer.DrawerMenuAdapter;
import com.sharesmile.share.gps.LocationService;
import com.sharesmile.share.gps.RunTracker;
import com.sharesmile.share.gps.models.WorkoutData;
import com.sharesmile.share.orgs.RunFragment;
import com.sharesmile.share.utils.Logger;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends BaseActivity {


    private static final String TAG = "MainActivity";
    private DrawerLayout drawerLayout;
    private ListView drawerList;
    private LocationService locationService;
    private RunFragment runFragment;

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


//        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_toolbar);
//        setSupportActionBar(toolbar);
//
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//
//        viewPager = (ViewPager) findViewById(R.id.viewpager);
//        setupViewPager(viewPager);
//
//        tabLayout = (TabLayout) findViewById(R.id.tabs);
//        tabLayout.setupWithViewPager(viewPager);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        drawerList = (ListView) findViewById(R.id.drawer_list_view);
        drawerList.setAdapter(new DrawerMenuAdapter(this, R.layout.drawer_list_item,
                R.id.list_item_text_view, MENU_ITEMS));
        drawerList.setOnItemClickListener(new DrawerItemClickListener());

        loadInitialFragment();

        LocalBroadcastManager.getInstance(this).registerReceiver(locationServiceReceiver,
                new IntentFilter(Constants.LOCATION_SERVICE_BROADCAST_ACTION));

        if (isWorkoutActive()){
            // If workout sesion going on then bind to service
            invokeLocationService();
        }
    }

    public boolean isWorkoutActive(){
        return RunTracker.isActive();
    }

    @Override
    public void loadInitialFragment(){
        runFragment = RunFragment.newInstance();
        addFragment(runFragment);
    }

    @Override
    public int getFrameLayoutId() {
        return R.id.mainFrameLayout;
    }

    @Override
    public String getName() {
        return TAG;
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (isBoundToLocationService()) {
            locationService.onActivityResult(requestCode, resultCode, data);

        }
    }

    public File writeLogsToFile(Context context){

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED){
            // All required permissions available
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("MMM");
            String month = sdf.format(cal.getTime());

            int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
            int hourOfDay = cal.get(Calendar.HOUR_OF_DAY);
            int minsOfHour = cal.get(Calendar.MINUTE);

            String fileName = "RFAC_log_" + dayOfMonth + "_" + month
                    + "_" + hourOfDay + ":" + minsOfHour + ".log";

            Logger.d(TAG, "writeLogsToFile " + fileName);

            File outputFile = new File(getLogsFileDir(),fileName);
            String filePath = outputFile.getAbsolutePath();
            try {
                Logger.d(TAG, "filePath " + filePath);
                @SuppressWarnings("unused")
                Process process = Runtime.getRuntime().exec("logcat -v -f "+ filePath);
            }catch (IOException ioe){
                Logger.e(TAG, "IOException while writing logs to file", ioe);
            }
            return outputFile;

        }else{
            //Need to get permissions
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    Constants.CODE_REQUEST_WRITE_PERMISSION);
        }

        return null;

    }

    public static File getLogsFileDir() {
        File root = android.os.Environment.getExternalStorageDirectory();
        File dir = new File (root.getAbsolutePath() + "/data/share");
        dir.mkdirs();
        return dir;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isBoundToLocationService()){
            unbindLocationService();
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(locationServiceReceiver);
    }

    public void beginLocationTracking(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED){
            // All required permissions available
            invokeLocationService();
        }else{
            //Need to get permissions
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    Constants.CODE_REQUEST_LOCATION_PERMISSION);
        }
    }

    public void endLocationTracking(){
        if (isBoundToLocationService()){
            locationService.stopLocationUpdates();
        }
    }

    private void unbindLocationService(){
        unbindService(locationServiceConnection);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == Constants.CODE_REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length == 1
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                invokeLocationService();
            } else {
                // Permission was denied or request was cancelled
                Logger.i(TAG, "Location Permission denied, could'nt update the UI");
                Toast.makeText(this, "Please give permission to get Weather", Toast.LENGTH_LONG).show();
            }
        }

        if (requestCode == Constants.CODE_REQUEST_WRITE_PERMISSION) {
            if (grantResults.length == 1
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Can re-capture logs
            } else {
                // Permission was denied or request was cancelled
                Logger.i(TAG, "Location Permission denied, could'nt update the UI");
                Toast.makeText(this, "Please give permission to get Weather", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void invokeLocationService(){
        Log.d(TAG, "invokeLocationService");
        Bundle bundle = new Bundle();
        Intent intent = new Intent(this, LocationService.class);
        intent.putExtras(bundle);
        startService(intent);
        bindService(intent, locationServiceConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection locationServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocationService, cast the IBinder and get LocationService instance
            LocationService.MyBinder binder = (LocationService.MyBinder) service;
            locationService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            locationService = null;
        }
    };

    public boolean isBoundToLocationService(){
        return (locationService != null);
    }


    private BroadcastReceiver locationServiceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                int broadcastCategory = bundle
                        .getInt(Constants.LOCATION_SERVICE_BROADCAST_CATEGORY);
                switch (broadcastCategory){
                    case Constants.BROADCAST_FIX_LOCATION_SETTINGS_CODE:
                        Status status = (Status) bundle.getParcelable(Constants.KEY_LOCATION_SETTINGS_PARCELABLE);
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(MainActivity.this,
                                    Constants.CODE_LOCATION_SETTINGS_RESOLUTION);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;

                    case Constants.BROADCAST_WORKOUT_RESULT_CODE:
                        Logger.i(TAG, "onReceive of locationServiceReceiver,  BROADCAST_WORKOUT_RESULT_CODE");
                        WorkoutData result = bundle.getParcelable(Constants.KEY_WORKOUT_RESULT);
                        //TODO: Display Result on UI
                        runFragment.showRunData(result);
                        break;

                    case Constants.BROADCAST_WORKOUT_UPDATE_CODE:
                        float currentSpeed = bundle.getFloat(Constants.KEY_WORKOUT_UPDATE_SPEED);
                        float currentTotalDistanceCovered = bundle.getFloat(Constants.KEY_WORKOUT_UPDATE_TOTAL_DISTANCE);
                        //TODO: Display Result on UI
                        runFragment.showUpdate(currentSpeed, currentTotalDistanceCovered);
                        break;

                    case Constants.BROADCAST_STEPS_UPDATE_CODE:
                        int totalSteps = bundle.getInt(Constants.KEY_WORKOUT_UPDATE_STEPS);
                        //TODO: Display Result on UI
                        runFragment.showSteps(totalSteps);
                        break;

                    case Constants.BROADCAST_UNBIND_SERVICE_CODE:
                        if (isBoundToLocationService()){
                            unbindLocationService();
                            locationService = null;
                        }
                        break;
                    case Constants.BROADCAST_STOP_WORKOUT_CODE:
                        Logger.i(TAG, "onReceive of locationServiceReceiver,  BROADCAST_STOP_WORKOUT_CODE");
                        if (runFragment != null && runFragment.isRunActive()){
                            int problem = bundle.getInt(Constants.KEY_WORKOUT_STOP_PROBLEM);
                            switch (problem){
                                case Constants.PROBELM_TOO_FAST:
                                    Toast.makeText(getApplicationContext(), "Oops! Looks like you are Usain Bolt, will stop workout",
                                            Toast.LENGTH_LONG).show();
                                    break;
                                case Constants.PROBELM_TOO_SLOW:
                                    Toast.makeText(getApplicationContext(), "You need to be a little more faster",
                                            Toast.LENGTH_LONG).show();
                                    break;
                                case Constants.PROBELM_NOT_MOVING:
                                    Toast.makeText(getApplicationContext(), "Don't be too lazy, move your ass!",
                                            Toast.LENGTH_LONG).show();
                                    break;
                            }
                            runFragment.endRun();
                        }
                        break;
                }
            }
        }
    };

}
