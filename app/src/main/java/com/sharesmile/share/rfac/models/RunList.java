package com.sharesmile.share.rfac.models;

import com.google.gson.annotations.SerializedName;
import com.sharesmile.share.Workout;
import com.sharesmile.share.core.UnObfuscable;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Created by Shine on 15/05/16.
 */
public class RunList implements UnObfuscable, Serializable, Iterable {


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


  /*  @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public Workout next() {
        return null;
    }

    @Override
    public void remove() {

    }*/

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

        Workout workout = new Workout(run.getId());
        workout.setWorkoutId(run.getClientRunId());
        workout.setIs_sync(true);
        workout.setDate(run.getDate());
        workout.setCauseBrief(run.getCauseName());
        workout.setAvgSpeed(run.getAvgSpeed());
        workout.setDistance(run.getDistance());
        workout.setRunAmount(run.getRunAmount());
        workout.setElapsedTime(run.getRunDuration());
        workout.setIsValidRun(!run.isFlag());
        workout.setStartPointLatitude(run.getStartLocationLat());
        workout.setStartPointLongitude(run.getStartLocationLong());
        workout.setEndPointLatitude(run.getEndLocationLat());
        workout.setEndPointLongitude(run.getEndLocationLong());
        return workout;
    }
}
