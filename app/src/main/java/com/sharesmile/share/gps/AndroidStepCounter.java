package com.sharesmile.share.gps;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.sharesmile.share.core.config.Config;
import com.sharesmile.share.utils.DateUtil;
import com.sharesmile.share.utils.Logger;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by ankitm on 12/05/16.
 */
public class AndroidStepCounter implements StepCounter, SensorEventListener {

    private static final String TAG = "AndroidStepCounter";

    private Context context;
    private Listener listener;
    private SensorManager sensorManager;
    private volatile int stepsSinceReboot = 0;
    private volatile long lastStepsRecordTs = 0;
    private LinkedHashMap historyQueue = new LinkedHashMap<Long, Long>()
    {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Long, Long> eldest) {
            return this.size() > NUM_ELEMS_IN_HISTORY_QUEUE;
        }
    };

    public AndroidStepCounter(Context context, Listener listener){
        this.context = context;
        this.listener = listener;
    }

    @Override
    public void start() {
        resetCounters();
        registerStepDetector();
    }

    @Override
    public void stop() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
        resetCounters();
    }

    @Override
    public void pause() {
        resetCounters();
    }

    @Override
    public void resume() {

    }

    @Override
    public float getMovingAverageOfStepsPerSec() {
        if (historyQueue.isEmpty() || historyQueue.size() == 1){
            return -1;
        }else {
            synchronized (historyQueue){
                Iterator iterator = historyQueue.entrySet().iterator();
                Map.Entry<Long, Long> first = null;
                Map.Entry<Long, Long> last = null;
                while (iterator.hasNext()){
                    Map.Entry<Long, Long> thisEntry = (Map.Entry<Long, Long>) iterator.next();

                    if ( ((DateUtil.getServerTimeInMillis() / 1000) - thisEntry.getKey())
                            > STEP_COUNT_READING_VALID_INTERVAL){
                        // This entry is too old to be considered in calculation
                        continue;
                    }

                    if (first == null){
                        first = thisEntry;
                        last = thisEntry;
                    }else {
                        if (thisEntry.getKey() < first.getKey()){
                            first = thisEntry;
                        }
                        if (thisEntry.getKey() > last.getKey()){
                            last = thisEntry;
                        }
                    }
                }

                if (first == null){
                    // No entry picked for calculation
                    return 0;
                }

                Long numSteps = last.getValue() - first.getValue();
                //In a rare scenario when queue has just two entries with same keys (i.e. epoch in secs) we are considering delta as 1
                Long deltaTime = (last.getKey() - first.getKey()) > 0
                        ? last.getKey() - first.getKey() : 1;
                return (numSteps.floatValue() / deltaTime);
            }
        }

    }

    @TargetApi(19)
    private void registerStepDetector(){
        // Get the default sensor for the sensor type from the SenorManager
        sensorManager = (SensorManager) context.getSystemService(Activity.SENSOR_SERVICE);
        // sensorType is either Sensor.TYPE_STEP_COUNTER or Sensor.TYPE_STEP_DETECTOR
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        // Register the listener for this sensor in batch mode.
        // If the max delay is 0, events will be delivered in continuous mode without batching.
        final boolean batchMode = sensorManager.registerListener(this, sensor,
                SensorManager.SENSOR_DELAY_NORMAL, Config.STEP_THRESHOLD_INTERVAL);

        if (batchMode) {
            // batchMode was enabled successfully
            Logger.d(TAG, "Step Detector registered successfully");
            listener.stepCounterReady();
            return;
        }
        listener.stepCounterNotAvailable(STEP_DETECTOR_NOT_SUPPORTED);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //Need to calculate delta steps
        if (stepsSinceReboot < 1){
            //i.e. fresh reading after creation of runtracker
            lastStepsRecordTs = DateUtil.getServerTimeInMillis();
            stepsSinceReboot = Math.round(event.values[0]);
            synchronized (historyQueue){
                historyQueue.put(Long.valueOf(DateUtil.getServerTimeInMillis() / 1000), Long.valueOf(stepsSinceReboot));
            }
            Logger.d(TAG, "Setting stepsSinceReboot for first time, stepsSinceReboot = "
                    + stepsSinceReboot);
            return;
        }
        int deltaSteps = Math.round(event.values[0]) - stepsSinceReboot;
        long deltaTimeMillis = DateUtil.getServerTimeInMillis() - lastStepsRecordTs;

        // Filtering on deltaSteps value
        float deltaCadence = deltaSteps / (deltaTimeMillis / 1000f); // num of steps per sec
        if ( deltaSteps < 0 || Math.abs(deltaCadence) > 10f){
            // 10 step per sec is our upper threshold, above which we will not accept step_detector reading
            Logger.i(TAG, "Detected absurdly high number of steps (" + deltaSteps + ") in "
                    + (deltaTimeMillis/1000) + " secs, wont feed to the listener");
            resetCounters();
            return;
        }
        stepsSinceReboot = Math.round(event.values[0]);
        lastStepsRecordTs = DateUtil.getServerTimeInMillis();
        synchronized (historyQueue){
            historyQueue.put(Long.valueOf(DateUtil.getServerTimeInMillis() / 1000), Long.valueOf(stepsSinceReboot));
        }
        listener.onStepCount(deltaSteps);
    }

    private void resetCounters(){
        stepsSinceReboot = 0;
        synchronized (historyQueue){
            historyQueue.clear();
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
