package com.sharesmile.share.gps.models;

import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;
import com.sharesmile.share.analytics.events.Properties;
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
	 * @return Calorie object which holds calories burned since the beginning of the batch
	 */
	Calorie getCalories();

	/**
	 * Sets the calories object for this WorkoutData
	 */
	void setCalories(Calorie calories);

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
	 * @param persistPoints If true location from this record is persisted in file belonging to the current batch
	 */
	void addRecord(DistRecord recordToAdd, boolean persistPoints);

	/**
	 * @return epoch (millis) at which workout began
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
	void workoutPause(String reason);

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

	/**
	 * Construct a Properties bundle object for analytics events
	 * @return
	 */
	Properties getWorkoutBundle();

	/**
	 * Fetches the start point (LatLng) of the workout
	 * @return
     */
	LatLng getStartPoint();

	/**
	 * Fetches the end point (LatLng) of the workout
	 * @return
	 */
	LatLng getLatestPoint();

	/**
	 * Sets the start point (LatLng) of the workout
	 * @return
	 */
	void setStartPoint(LatLng latLng);

	/**
	 * Gets the universally unique identifier of workout
	 * @return
     */
	String getWorkoutId();

	/**
	 * Gets the number of times Usain Bolt ocurred for this workout
	 * @return
	 */
	int getUsainBoltCount();

	/**
	 * Increments the count of Usain Bolts in this workout
	 */
	void incrementUsainBoltCounter();

	/**
	 * Sets mockLocation boolean for this workout
	 */
	void setMockLocationDetected(boolean detected);

	/**
	 * @return true if mock location was detected in this workout, false otherwise
	 */
	boolean isMockLocationDetected();

	/**
	 * @return Number of GPS spikes which occurred during the run
	 */
	int getNumGpsSpikes();

	/**
	 * Increments the number of GPS spikes in this run
	 * @return
	 */
	void incrementGpsSpike();

	/**
	 * @return Number of WORKOUT_UPDATE events which were sent during the run
	 */
	int getNumUpdateEvents();

	/**
	 * Increments the number of WORKOUT_UPDATE events in this run
	 * @return
	 */
	void incrementNumUpdates();

	/**
	 * Converts this object to string
	 * @return
	 */
	String toString();

}
