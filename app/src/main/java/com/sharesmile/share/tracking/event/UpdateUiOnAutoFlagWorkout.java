package com.sharesmile.share.tracking.event;

/**
 * Created by ankitmaheshwari on 12/17/17.
 */

public class UpdateUiOnAutoFlagWorkout {

    private float avgSpeed;
    private int recordedTime;

    public UpdateUiOnAutoFlagWorkout(float avgSpeed, int recordedTime) {
        this.avgSpeed = avgSpeed;
        this.recordedTime = recordedTime;
    }

    public float getAvgSpeed() {
        return avgSpeed;
    }

    public int getRecordedTime() {
        return recordedTime;
    }
}
