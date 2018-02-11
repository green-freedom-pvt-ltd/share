package com.sharesmile.share.tracking.stepcount;

import android.content.Context;

import com.google.android.gms.fitness.data.DataType;
import com.sharesmile.share.tracking.google.tracker.GoogleFitSensorTracker;
import com.sharesmile.share.utils.DateUtil;
import com.sharesmile.share.core.Logger;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by ankitm on 09/05/16.
 */
public class GoogleFitStepCounter implements StepCounter, GoogleFitSensorTracker.Listener {

    private static final String TAG = "GoogleFitStepCounter";

    public static final int REQUEST_OAUTH = 1;
    private static final String AUTH_PENDING = "auth_state_pending";

    private Listener listener;
    private GoogleFitSensorTracker tracker;

    private LinkedHashMap historyQueue = new LinkedHashMap<Long, Long>()
    {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Long, Long> eldest) {
            return this.size() > NUM_ELEMS_IN_HISTORY_QUEUE + 1;
        }
    };


    public GoogleFitStepCounter(Context context, Listener listener) {
        this.listener = listener;
        tracker = new GoogleFitSensorTracker(context, DataType.TYPE_STEP_COUNT_DELTA, this);
    }

    @Override
    public void start() {
        Logger.d(TAG, "start");
        tracker.start();
        synchronized (historyQueue){
            historyQueue.clear();
        }
    }

    @Override
    public void stop() {
        Logger.d(TAG, "stop");
        tracker.stop();
        synchronized (historyQueue){
            historyQueue.clear();
        }
    }

    @Override
    public void pause() {
        tracker.pause();
        synchronized (historyQueue){
            historyQueue.clear();
        }
    }

    @Override
    public void resume() {
        tracker.resume();
        synchronized (historyQueue){
            historyQueue.clear();
        }
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
                Long numSteps = 0L;
                while (iterator.hasNext()){
                    Map.Entry<Long, Long> thisEntry = (Map.Entry<Long, Long>) iterator.next();

                    if ( ((DateUtil.getServerTimeInMillis() / 1000) - thisEntry.getKey())
                            > STEP_COUNT_READING_VALID_INTERVAL){
                        // This entry is too old to be considered in calculation
                        continue;
                    }

                    numSteps += thisEntry.getValue();
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

                Long numStepsInFirst = first.getValue();
                numSteps = numSteps - numStepsInFirst;
                //In a rare scenario when queue has just two entries with same keys (i.e. epoch in secs) we are considering delta as 1
                Long deltaTime = (last.getKey() - first.getKey()) > 0
                        ? last.getKey() - first.getKey() : 1;
                return  numSteps.floatValue()  / deltaTime;
            }
        }
    }



    @Override
    public void isTrackerReady() {
        listener.stepCounterReady();
    }

    @Override
    public void trackerNotAvailable() {
        listener.stepCounterNotAvailable(PERMISSION_NOT_GRANTED_BY_USER);
    }

    @Override
    public void onDeltaCount(long beginTs, long endTs, float deltaIncrement) {
        Logger.d(TAG, "onDeltaCount, beginTs = " + beginTs + ", endTs = " + endTs
                + ", delta in steps = " + deltaIncrement);
        listener.onStepCount((int) deltaIncrement);
        synchronized (historyQueue){
            historyQueue.put(endTs, (long) deltaIncrement);
        }
    }
}
