package com.sharesmile.share.rfac.models;

import com.sharesmile.share.Workout;

/**
 * Created by ankitmaheshwari on 6/15/17.
 */

public class RunHistoryDetailsItem extends RunHistoryItem {

    private Workout workout;

    public RunHistoryDetailsItem(Workout workout){
        this.workout = workout;
    }

    public Workout getWorkout() {
        return workout;
    }

    @Override
    public int getType() {
        return RUN_ITEM;
    }
}
