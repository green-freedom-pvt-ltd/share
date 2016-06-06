package com.sharesmile.share.gps.models;

import android.location.Location;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;
import com.sharesmile.share.core.UnObfuscable;

import java.util.List;

/**
 * Created by ankitm on 25/03/16.
 */
public interface WorkoutData extends UnObfuscable, Parcelable, Cloneable{

	/**
	 * gets total distance ran in workout in meters
	 * @return
	 */
	float getDistance();

	/**
	 * @return time interval in secs for which distance has been recorded
	 */
	float getRecordedTime();

	/**
	 * @return elapsed time interval in secs for which the workout was in running state( i.e. not paused)
	 * since the beginning
	 *
	 */
	float getElapsedTime();

	/**
	 * @return Avg speed of the workout in meter/sec
	 */
	float getAvgSpeed();

	/**
	 * @return total number of steps in the entire workout session
	 */
	int getTotalSteps();

	/**
	 * @return All the location points marked for this workout session
	 */
	List<LatLng> getPoints();

	/**
	 * @return all the batches of this workout session
	 */
	List<WorkoutBatchImpl> getBatches();

	/**
	 * just adds the given distance to the workout and attributes it to the current session
	 * @param distanceToAdd
	 */
	void addDistance(float distanceToAdd);

	/**
	 * add given num steps to the workout
	 * @param stepsToAdd
	 */
	void addSteps(int stepsToAdd);

	/**
	 * adds given distance record
	 * @param recordToAdd
	 */
	void addRecord(DistRecord recordToAdd);

	/**
	 * @return epoch at which workout began
	 */
	long getBeginTimeStamp();

	/**
	 * @return index of the batch which is currently running OR
	 * the index of the last batch if the workout is paused
	 */
	int getCurrentBatchIndex();

	/**
	 * @return the batch which is currently running OR
	 * the last batch if the workout is paused
	 */
	WorkoutBatch getCurrentBatch();

	/**
	 * @return true iff workout is paused
	 */
	boolean isPaused();

	/**
	 * @return true iff workout is running, i.e. active and not paused
	 */
	boolean isRunning();

	/**
	 * completes the currently runnning batch
	 */
	void workoutPause();

	/**
	 * invokes new batch and adds it to the list of batches
	 */
	void workoutResume();

	/**
	 * @return true if start point has not yet been detected after workout started/resumed
	 */
	boolean coldStartAfterResume();

	/**
	 * completes workout session and returns this after whatever post processing is required
	 */
	WorkoutData close();

	/**
	 * Creates a Deep copy of this WorkoutData instance
	 */
	WorkoutData copy();

}
