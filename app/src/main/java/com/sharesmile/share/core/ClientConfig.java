package com.sharesmile.share.core;

import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sharesmile.share.MainApplication;
import com.sharesmile.share.network.FailureType;
import com.sharesmile.share.network.NetworkDataProvider;
import com.sharesmile.share.network.NetworkException;
import com.sharesmile.share.utils.Logger;
import com.sharesmile.share.utils.SharedPrefsManager;
import com.sharesmile.share.utils.Urls;

import java.util.List;

import static com.sharesmile.share.core.Constants.PREF_CLIENT_CONFIG;

/**
 * Created by ankitmaheshwari on 8/3/17.
 */

public class ClientConfig implements UnObfuscable{

    private static final String TAG = "ClientConfig";

    public float MIN_CADENCE_FOR_WALK = 0.68f; // in steps per sec

    public float SPIKE_FILTER_SPEED_THRESHOLD_IN_VEHICLE = 52f; // in m/s, i.e. 187 km/hr

    public  float SPIKE_FILTER_SPEED_THRESHOLD_DEFAULT = 32f; // in m/s, i.e. 115.2 km/hr

    public  float SECONDARY_SPIKE_FILTER_SPEED_THRESHOLD_DEFAULT = 20.83f; // in m/s, i.e. 75 km/hr

    public  float SPIKE_FILTER_SPEED_THRESHOLD_ON_FOOT = 9.03f; // in m/s, i.e. 32.5 km/hr

    public  float USAIN_BOLT_UPPER_SPEED_LIMIT = 7f; // in m/s, i.e. 25.2 km/hr

    public  float USAIN_BOLT_UPPER_SPEED_LIMIT_ON_FOOT = 12.5f; // in m/s, i.e. 45 km/hr

    public  float USAIN_BOLT_RECENT_SPEED_LOWER_BOUND = 4.1f; // in m/s, i.e. 14.8 km/hr

    public  float USAIN_BOLT_GPS_SPEED_LIMIT = 5.83f; // in m/s, i.e. 21 km/hr

    public  float USAIN_BOLT_WAIVER_STEPS_RATIO = 0.38f;

    public  long VIGILANCE_TIMER_INTERVAL = 17000; // in millisecs

    public  float SOURCE_ACCEPTABLE_ACCURACY = 40; // in m

    public  float THRESHOLD_ACCURACY = 12; // in m

    public  float THRESHOLD_FACTOR = 4.75f;

    public  long GPS_INACTIVITY_NOTIFICATION_DELAY = 50000; // in Millisecs

    // Activity Detector Config
    public  int CONFIDENCE_THRESHOLD_VEHICLE = 74;
    public  int CONFIDENCE_THRESHOLD_ON_FOOT = 75;
    public  int CONFIDENCE_UPPER_THRESHOLD_STILL = 85;
    public  int CONFIDENCE_LOWER_THRESHOLD_STILL = 20;
    // Minimum ON_FOOT confidence required for walk engagement notif
    public  int CONFIDENCE_THRESHOLD_WALK_ENGAGEMENT = 52;

    // Used maintain history of activityRecognitionResult recent history
    public  int ACTIVITY_RECOGNITION_RESULT_HISTORY_QUEUE_MAX_SIZE = 5;

    // ActivityRecognition updates request interval
    public  long DETECTED_INTERVAL_IDLE = 15000; // in millisecs
    public  long DETECTED_INTERVAL_ACTIVE = 2000; // in millisecs

    // If user remains continuously still for this much time then Still notification is shown
    public  long STILL_NOTIFICATION_DISPLAY_INTERVAL = 25000; // in millisecs

    // Walk engagement counter is invoked periodically after this interval
    public  long WALK_ENGAGEMENT_COUNTER_INTERVAL = 15000; // in millisecs

    // Show walk engagement notif if user has been on foot continuously for this much amount of time
    public  long WALK_ENGAGEMENT_NOTIFICATION_INTERVAL = 90000;// in millisecs

    public  long WALK_ENGAGEMENT_NOTIFICATION_THROTTLE_PERIOD = 86400000;// in millisecs, i.e. 24 hours

    // During tracking If GPS behaves bad continuously for this much amount of time, then BAD_GPS_NOTIF is shown
    public  long BAD_GPS_NOTIF_THRESHOLD_INTERVAL = 60000;

    // In a single DistRecord update delta_distance cannot be more than this value
    public  float DIST_INC_IN_SINGLE_GPS_UPDATE_UPPER_LIMIT = 5000f;// in meters

    // ON_WORKOUT_UPDATE analytics event occurrs after this much distance
    public  float MIN_DISPLACEMENT_FOR_WORKOUT_UPDATE_EVENT = 0.1f;// in Kms

    // Sync Config
    // All the client data is synced periodically after this interval
    public  long DATA_SYNC_INTERVAL = 10800L;// in secs, i.e. every 3 hours
    public  long DATA_SYNC_INTERVAL_FLEX = 5400L;// in secs, i.e. every 1.5 hours

    private static ClientConfig instance;

    private ClientConfig(){
    }

    /**
     Throws IllegalStateException if this class is not initialized

     @return unique WorkoutSingleton instance
     */
    public static ClientConfig getInstance(){
        if (instance == null) {
            synchronized (ClientConfig.class) {
                if (instance == null) {
                    ClientConfig storedConfig = SharedPrefsManager.getInstance().getObject(PREF_CLIENT_CONFIG, ClientConfig.class);
                    if (storedConfig == null){
                        storedConfig = new ClientConfig();
                    }
                    instance = storedConfig;
                }
            }
        };
        return instance;
    }

    private static synchronized void resetConfig(ClientConfig freshConfig){
        Logger.d(TAG, "ResetConfig with " + (new Gson().toJson(freshConfig)));
        instance = freshConfig;
        SharedPrefsManager.getInstance().setObject(PREF_CLIENT_CONFIG, freshConfig);
    }

    public static void sync(){
        new ExpoBackoffTask() {
            @Override
            public int performtask() {
                return fetchClientConfig();
            }
        }.run();
    }

    private static int fetchClientConfig(){
        Logger.d(TAG, "fetchClientConfig");
        try {
            List<ClientConfig> list = NetworkDataProvider.doGetCall(Urls.getClientConfigUrl(),
                            new TypeToken<List<ClientConfig>>(){}.getType());
            if (list.size() > 0){
                resetConfig(list.get(0));
                return ExpoBackoffTask.RESULT_SUCCESS;
            }else {
                Logger.e(TAG, "Failure fetching ClientConfig, response list empty");
                return ExpoBackoffTask.RESULT_FAILURE;
            }
        }catch (NetworkException ne){
            Logger.d(TAG, "NetworkException in ClientConfig sync: " + ne);
            ne.printStackTrace();
            String message = "";
            int userId = 0;
            if (MainApplication.getInstance().isLogin() && MainApplication.getInstance().getUserDetails() != null){
                userId = MainApplication.getInstance().getUserDetails().getUserId();
                message = "ClientConfig sync failed due to networkException for user_id ("
                        + userId +"), messageFromServer: " + ne;
            }else {
                message = "ClientConfig sync failed due to networkException, messageFromServer: " + ne;
            }
            Crashlytics.log(message);
            Crashlytics.logException(ne);
            if (ne.getFailureType() == FailureType.REQUEST_FAILURE){
                return ExpoBackoffTask.RESULT_RESCHEDULE;
            }else {
                return ExpoBackoffTask.RESULT_FAILURE;
            }
        }catch (Exception e){
            Logger.d(TAG, "Exception while fetching ClientConfig: " + e.getMessage());
            e.printStackTrace();
            return ExpoBackoffTask.RESULT_FAILURE;
        }
    }
}
