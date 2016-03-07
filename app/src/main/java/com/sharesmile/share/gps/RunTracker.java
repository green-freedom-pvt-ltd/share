package com.sharesmile.share.gps;

import android.hardware.SensorEvent;
import android.location.Location;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import com.sharesmile.share.core.Config;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.gps.models.DistRecord;
import com.sharesmile.share.gps.models.WorkoutData;
import com.sharesmile.share.utils.Logger;
import com.sharesmile.share.utils.SharedPrefsManager;

/**
 * Created by ankitmaheshwari1 on 21/02/16.
 */
public class RunTracker {

    private static final String TAG = "RunTracker";

    private static final String WORKER_THREAD_NAME = "LocationPersistorWorkerHandler";
    private final HandlerThread sWorkerThread = new HandlerThread(WORKER_THREAD_NAME);
    private static WorkerHandler mWorkerHandler;

    public static final int MSG_PROCESS_LOCATION = 1;
    public static final int MSG_PROCESS_STEPS_EVENT = 2;
    private int stepsSinceReboot = 0;
    private WorkoutDataStore dataStore;
    private UpdateListner listener;

    public RunTracker(UpdateListner listener){
        sWorkerThread.start();
        mWorkerHandler = new WorkerHandler(this, sWorkerThread.getLooper());
        this.listener = listener;
        stepsSinceReboot = 0;
        if (isActive()){
            dataStore = new WorkoutDataStore();
        }
        // Else wait for begin run;
    }

    public synchronized void beginRun(){
        if (!isActive()){
            SharedPrefsManager.getInstance().setBoolean(Constants.PREF_IS_WORKOUT_ACTIVE, true);
            dataStore = new WorkoutDataStore(System.currentTimeMillis());
            stepsSinceReboot = 0;
        }else{
            throw new IllegalStateException("Can't begin run when one is already active");
        }
    }

    public synchronized WorkoutData endRun(){
        WorkoutData workoutData = dataStore.clear();
        dataStore = null;
        stepsSinceReboot = 0;
        SharedPrefsManager.getInstance().setBoolean(Constants.PREF_IS_WORKOUT_ACTIVE, false);
        listener = null;
        mWorkerHandler.stopHandling();
        mWorkerHandler = null;
        boolean isQuit = sWorkerThread.quit();
        Logger.d(TAG, "Thread Quit Success = " + isQuit);
        return workoutData;
    }

    public long getBeginTimeStamp(){
        if (isActive() && dataStore != null){
            return dataStore.getBeginTimeStamp();
        }
        return 0;
    }

    public int getTotalSteps(){
        if (isActive() && dataStore != null){
            return dataStore.getTotalSteps();
        }
        return 0;
    }

    public DistRecord getLastRecord(){
        if (isActive() && dataStore != null){
            return dataStore.getLastRecord();
        }
        return null;
    }

    public float getDistanceCovered(){
        if (isActive() && dataStore != null){
            return dataStore.getTotalDistance();
        }
        return 0;
    }


    public void feedLocation(Location point){
        if (!isActive()){
            throw new IllegalStateException("Can't feed locations without beginning run");
        }
        mWorkerHandler.obtainMessage(MSG_PROCESS_LOCATION, point).sendToTarget();
    }


    public void feedSteps(SensorEvent event){
        mWorkerHandler.obtainMessage(MSG_PROCESS_STEPS_EVENT, event).sendToTarget();
    }

    /**
     * Feed step counter event, the first index of values array contains the total number of steps since reboot
     * @param event
     */
    private void processStepsEvent(SensorEvent event){
        if (stepsSinceReboot < 1){
            //i.e. fresh reading after creation of runtracker
            stepsSinceReboot = (int) event.values[0];
            Logger.d(TAG, "Setting stepsSinceReboot for first time = " + stepsSinceReboot);
        }
        int numSteps = (int) event.values[0] - stepsSinceReboot;
        stepsSinceReboot = (int) event.values[0];
        long reportimeStamp = System.currentTimeMillis();
        Logger.d(TAG, "Adding " + numSteps + "steps.");
        dataStore.addSteps(numSteps);
        listener.updateStepsRecord(reportimeStamp, numSteps);
    }

    private void processLocation(Location point){
        if (dataStore.getRecordsCount() <= 0){
            // This is the source location
            Logger.d(TAG, "Checking for source, accuracy = " + point.getAccuracy());
            if (point.getAccuracy() < Config.SOURCE_ACCEPTABLE_ACCURACY){
                // Set has source only when it has acceptable accuracy
                Logger.d(TAG,"Source Location with good accuracy fetched:\n " + point.toString());
                dataStore.setSource(point);
            }
        }else{
            Logger.d(TAG,"Processing Location:\n " + point.toString());
            long ts = point.getTime();
            DistRecord prevRecord = dataStore.getLastRecord();
            Location prevLocation = prevRecord.getLocation();
            long prevTs = prevLocation.getTime();
            float interval = ((float) (ts - prevTs)) / 1000;
            float dist = prevLocation.distanceTo(point);

            // Step 1: Check whether threshold interval for recording has elapsed
            if (interval > Config.THRESHOLD_INTEVAL){

                if (Config.SPEED_TRACKING){
                    DistRecord record = new DistRecord(point, prevLocation);
                    Logger.d(TAG,"Speed Recording: " + record.toString());
                    dataStore.addRecord(record);
                    listener.updateWorkoutRecord(dataStore.getTotalDistance(), record.getSpeed());
                }else{
                    boolean toRecord = false;
                    float accuracy = point.getAccuracy();
                    /*
                     Step 2: Record if point is accurate, i.e. accuracy better/lower than our threshold
                             Else
                             Apply formula to check whether to record the point or not
                      */
                    if (accuracy < Config.THRESHOLD_ACCURACY){
                        Logger.d(TAG, "Accuracy Wins");
                        toRecord = true;
                    }else{
                        toRecord = checkUsingFormula(dist, point.getAccuracy());
                    }
                    // Step 3: Record if needed, else wait for next location
                    if (toRecord){
                        DistRecord record = new DistRecord(point, prevLocation, dist);
                        Logger.d(TAG,"Distance Recording: " + record.toString());
                        dataStore.addRecord(record);
                        listener.updateWorkoutRecord(dataStore.getTotalDistance(), record.getSpeed());
                    }
                }
            }
        }
    }

    private boolean checkUsingFormula(float dist, float accuracy){
        float deltaAccuracy = accuracy - (Config.THRESHOLD_ACCURACY - Config.THRESHOLD_ACCURACY_OFFSET);
        float value = (dist / deltaAccuracy);
        Logger.d(TAG, "Applying formula, dist = " + dist + " accuracy = " + accuracy + " value = " + value);
        if ( value > Config.THRESHOLD_FACTOR){
            return true;
        }
        return false;
    }


    public static boolean isActive(){
        return SharedPrefsManager.getInstance().getBoolean(Constants.PREF_IS_WORKOUT_ACTIVE);
    }

    public static class WorkerHandler extends Handler {

        private RunTracker mTracker;

        public WorkerHandler(RunTracker persistor, Looper looper) {
            super(looper);
            this.mTracker = persistor;
        }

        @Override
        public void handleMessage(Message msg) {
            synchronized (this){
                super.handleMessage(msg);
                Object object = msg.obj;
                switch (msg.what) {
                    case MSG_PROCESS_LOCATION:
                        if (mTracker != null && object instanceof Location){
                            mTracker.processLocation((Location) object);
                        }
                        break;
                    case MSG_PROCESS_STEPS_EVENT:
                        if (mTracker != null && object instanceof SensorEvent){
                            mTracker.processStepsEvent((SensorEvent) object);
                        }
                        break;
                }
            }
        }

        public void stopHandling(){
            mTracker = null;
        }
    }

    interface UpdateListner {

        void updateWorkoutRecord(float totalDistance, float currentSpeed);

        void updateStepsRecord(long timeStampMillis, int numSteps);

    }

}
