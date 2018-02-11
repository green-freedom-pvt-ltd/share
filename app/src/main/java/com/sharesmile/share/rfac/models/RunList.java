package com.sharesmile.share.rfac.models;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;
import com.sharesmile.share.Workout;
import com.sharesmile.share.core.base.UnObfuscable;
import com.sharesmile.share.utils.DateUtil;
import com.sharesmile.share.core.Logger;
import com.sharesmile.share.utils.Utils;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Created by Shine on 15/05/16.
 */
public class RunList implements UnObfuscable, Serializable, Iterable {

    private static final String TAG = "RunList";


    @SerializedName("count")
    long totalRunCount;

    @SerializedName("next")
    String nextUrl;

    @SerializedName("previous")
    String previousUrl;

    @SerializedName("results")
    List<Run> runList;

    public long getTotalRunCount() {
        return totalRunCount;
    }

    public void setTotalRunCount(long totalRunCount) {
        this.totalRunCount = totalRunCount;
    }

    public String getNextUrl() {
        return nextUrl;
    }

    public void setNextUrl(String nextUrl) {
        this.nextUrl = nextUrl;
    }

    public String getPreviousUrl() {
        return previousUrl;
    }

    public void setPreviousUrl(String previousUrl) {
        this.previousUrl = previousUrl;
    }

    public List<Run> getRunList() {
        return runList;
    }

    public void setRunList(List<Run> runList) {
        this.runList = runList;
    }

    @Override
    public Iterator iterator() {
        return new ArrayListIterator();
    }

    private class ArrayListIterator implements Iterator<Workout> {

        private long remaining = getRunList().size();

        /**
         * Index of element that remove() would remove, or -1 if no such elt
         */
        private long removalIndex = -1;

        @Override
        public boolean hasNext() {
            return remaining != 0;
        }

        @Override
        public Workout next() {
            long rem = remaining;
            if (rem == 0) {
                throw new NoSuchElementException();
            }
            remaining = rem - 1;
            removalIndex = getRunList().size() - rem;
            Run run = getRunList().get((int) remaining);


            return getWorkoutData(run);
        }

        @Override
        public void remove() {
        }
    }

    private Workout getWorkoutData(Run run) {
        Workout workout = new Workout(run.getId()); //
        workout.setWorkoutId(run.getClientRunId()); //
        workout.setSteps(Math.round(run.getNumSteps()));
        workout.setIs_sync(true); // Setting is_sync as true because this run is fetched from server, so it must be already synced
        // Not setting shouldSyncLocationData boolean on purpose
        workout.setDate(run.getDate());
        workout.setCauseBrief(run.getCauseName());
        workout.setCauseId(run.getCauseId());
        workout.setAvgSpeed(run.getAvgSpeed());
        workout.setDistance(run.getDistance());
        workout.setRunAmount(run.getRunAmount());
        if (!TextUtils.isEmpty(run.getRunDuration())){
            workout.setElapsedTime(run.getRunDuration()); // Set proper ElapsedTime
            workout.setRecordedTime((float) Utils.hhmmssToSecs(run.getRunDuration()));
        }
        workout.setIsValidRun(!run.isFlag());
        workout.setStartPointLatitude(run.getStartLocationLat());
        workout.setStartPointLongitude(run.getStartLocationLong());
        workout.setEndPointLatitude(run.getEndLocationLat());
        workout.setEndPointLongitude(run.getEndLocationLong());
        workout.setBeginTimeStamp(extractStartTimeEpoch(run));
        if (extractEndTimeEpoch(run) != null){
            workout.setEndTimeStamp(extractEndTimeEpoch(run));
        }
        workout.setVersion(run.getVersion());
        workout.setCalories(run.getCalories());
        if (run.getTeamId() > 0){
            workout.setTeamId(run.getTeamId());
        }
        workout.setNumSpikes(run.getNumSpikes());
        workout.setNumUpdates(run.getNumUpdates());
        workout.setAppVersion(run.getAppVersion());
        workout.setOsVersion(run.getOsVersion());
        workout.setDeviceId(run.getDeviceId());
        workout.setDeviceName(run.getDeviceName());
        workout.setEstimatedSteps(run.getEstimatedSteps());
        workout.setEstimatedDistance(run.getEstimatedDistance());
        workout.setEstimatedCalories(run.getEstimatedCalories());
        workout.setGoogleFitStepCount(run.getGoogleFitSteps());
        workout.setGoogleFitDistance(run.getGoogleFitDistance());
        workout.setUsainBoltCount(run.getUsainBoltCount());
        workout.setShouldSyncLocationData(false);
        return workout;
    }

    private Long extractStartTimeEpoch(Run run){
        if (run.getStartTimeEpoch() != null && run.getStartTimeEpoch() > 0){
            // start_time_epoch received from server
            return run.getStartTimeEpoch();
        }else if (!TextUtils.isEmpty(run.getStartTime())){
            try {
                return DateUtil.getDefaultFormattedDate(run.getStartTime()).getTime();
            }catch (Exception e){
                Logger.e(TAG, "Exception while formatting: " + run.getStartTime() + " message: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return run.getDate().getTime();
    }

    private Long extractEndTimeEpoch(Run run){
        if (run.getEndTimeEpoch() != null && run.getEndTimeEpoch() > 0){
            // end_time_epoch received from server
            return run.getEndTimeEpoch();
        }else if (!TextUtils.isEmpty(run.getEndTime())){
            try {
                return DateUtil.getDefaultFormattedDate(run.getEndTime()).getTime();
            }catch (Exception e){
                Logger.e(TAG, "Exception while formatting: " + run.getEndTime() + " message: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return null;
    }
}
