package com.sharesmile.share.gps;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.sharesmile.share.core.Config;
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
        startCounting();
    }

    @Override
    public void startCounting() {
        stepsSinceReboot = 0;
        historyQueue.clear();
        registerStepDetector();
    }

    @Override
    public void stopCounting() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
        stepsSinceReboot = 0;
        historyQueue.clear();
    }

    @Override
    public void pauseCounting() {
        stepsSinceReboot = 0;
        historyQueue.clear();
    }

    @Override
    public void resumeCounting() {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

    }

    @Override
    public float getMovingAverageOfStepsPerSec() {
        if (historyQueue.isEmpty() || historyQueue.size() == 1){
            return -1;
        }else {
            Iterator iterator = historyQueue.entrySet().iterator();
            Map.Entry<Long, Long> first = null;
            Map.Entry<Long, Long> last = null;
            while (iterator.hasNext()){
                Map.Entry<Long, Long> thisEntry = (Map.Entry<Long, Long>) iterator.next();
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
            Long numSteps = last.getValue() - first.getValue();
            return (numSteps.floatValue() / (last.getKey() - first.getKey()));
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
            listener.isReady();
            return;
        }
        listener.notAvailable(STEP_DETECTOR_NOT_SUPPORTED);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //Need to calculate delta steps
        if (stepsSinceReboot < 1){
            //i.e. fresh reading after creation of runtracker
            stepsSinceReboot = (int) event.values[0];
            historyQueue.put(Long.valueOf(System.currentTimeMillis() / 1000), Long.valueOf(stepsSinceReboot));
            Logger.d(TAG, "Setting stepsSinceReboot for first time, stepsSinceReboot = "
                    + stepsSinceReboot);
            return;
        }
        int deltaSteps = (int) event.values[0] - stepsSinceReboot;
        stepsSinceReboot = (int) event.values[0];
        historyQueue.put(Long.valueOf(System.currentTimeMillis() / 1000), Long.valueOf(stepsSinceReboot));
        listener.onStepCount(deltaSteps);
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
