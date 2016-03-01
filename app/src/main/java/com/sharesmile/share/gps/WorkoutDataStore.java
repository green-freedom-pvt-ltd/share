package com.sharesmile.share.gps;

import android.location.Location;
import android.text.TextUtils;

import com.sharesmile.share.core.Constants;
import com.sharesmile.share.gps.models.DistRecord;
import com.sharesmile.share.gps.models.WorkoutData;
import com.sharesmile.share.utils.SharedPrefsManager;
import com.sharesmile.share.utils.Utils;

/**
 * Created by ankitmaheshwari1 on 21/02/16.
 */
public class WorkoutDataStore {

    private static final String TAG = "WorkoutDataStore";

    private long beginTimeStamp;
    private DistRecord source;
    private DistRecord lastRecord;
    private int recordsCount;
    private WorkoutData workoutData;

    public WorkoutDataStore(){
        this.beginTimeStamp = SharedPrefsManager.getInstance().getLong(Constants.PREF_RUN_BEGIN_TIMESTAMP);
        String sourceAsString = SharedPrefsManager.getInstance().getString(Constants.PREF_RUN_SOURCE);
        String prevRecordAsString = SharedPrefsManager.getInstance().getString(Constants.PREF_PREV_DIST_RECORD);
        String workoutDataAsString = SharedPrefsManager.getInstance().getString(Constants.PREF_WORKOUT_DATA);
        if (!TextUtils.isEmpty(sourceAsString)){
            source = Utils.createObjectFromJSONString(sourceAsString, DistRecord.class);
        }
        if (!TextUtils.isEmpty(prevRecordAsString)){
            lastRecord = Utils.createObjectFromJSONString(prevRecordAsString, DistRecord.class);
        }
        if (!TextUtils.isEmpty(workoutDataAsString)){
            workoutData = Utils.createObjectFromJSONString(workoutDataAsString, WorkoutData.class);
        }else{
            workoutData = new WorkoutData(beginTimeStamp);
        }
        recordsCount = SharedPrefsManager.getInstance().getInt(Constants.PREF_NUM_RECORDS);
    }

    public WorkoutDataStore(long beginTimeStamp){
        this.beginTimeStamp = beginTimeStamp;
        workoutData = new WorkoutData(beginTimeStamp);
        SharedPrefsManager.getInstance().setLong(Constants.PREF_RUN_BEGIN_TIMESTAMP, beginTimeStamp);
    }

    public float getTotalDistance(){
        return workoutData.getDistance();
    }

    public long getBeginTimeStamp() {
        return beginTimeStamp;
    }

    public DistRecord getSource() {
        return source;
    }

    public void setSource(Location point) {
        this.source = new DistRecord(point);
        this.lastRecord = source;
        workoutData.setSource(point);
        recordsCount++;
        SharedPrefsManager.getInstance().setObject(Constants.PREF_PREV_DIST_RECORD, lastRecord);
        SharedPrefsManager.getInstance().setObject(Constants.PREF_RUN_SOURCE, source);
        SharedPrefsManager.getInstance().setInt(Constants.PREF_NUM_RECORDS, recordsCount);
    }

    public DistRecord getLastRecord() {
        return lastRecord;
    }

    public int getRecordsCount(){
        return recordsCount;
    }

    public void addRecord(DistRecord record) {
        if (recordsCount == 1){
            // Very first record after source
            float speed = record.getSpeed();
            // Need to extrapolate the distance for time elapsed since begin run and source detection
            float timeToFetchSource = ((float) (source.getLocation().getTime() - beginTimeStamp)) / 1000;
            float speedForExtrapolation = record.getSpeed();
            float extraPolatedDistance = timeToFetchSource * speedForExtrapolation;
            workoutData.addDistance(extraPolatedDistance);
        }
        workoutData.addRecord(record);
        float totalTime = ((float) (record.getLocation().getTime() - beginTimeStamp)) / 1000;
        workoutData.setRecordedTime(totalTime);
        this.lastRecord = record;
        recordsCount++;
        SharedPrefsManager.getInstance().setObject(Constants.PREF_PREV_DIST_RECORD, record);
        SharedPrefsManager.getInstance().setInt(Constants.PREF_NUM_RECORDS, recordsCount);
        SharedPrefsManager.getInstance().setObject(Constants.PREF_WORKOUT_DATA, workoutData);
    }

    public void addSteps(int numSteps){
        workoutData.addSteps(numSteps);
        SharedPrefsManager.getInstance().setObject(Constants.PREF_WORKOUT_DATA, workoutData);
    }

    public int getTotalSteps(){
        return workoutData.getTotalSteps();
    }


    public WorkoutData clear(){
        SharedPrefsManager.getInstance().removeKey(Constants.PREF_PREV_DIST_RECORD);
        SharedPrefsManager.getInstance().removeKey(Constants.PREF_RUN_SOURCE);
        SharedPrefsManager.getInstance().removeKey(Constants.PREF_NUM_RECORDS);
        SharedPrefsManager.getInstance().removeKey(Constants.PREF_WORKOUT_DATA);
        SharedPrefsManager.getInstance().removeKey(Constants.PREF_RUN_BEGIN_TIMESTAMP);
        return workoutData;
    }
}
