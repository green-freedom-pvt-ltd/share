package com.sharesmile.share.gps;

import com.google.android.gms.maps.model.LatLng;
import com.sharesmile.share.analytics.events.Properties;
import com.sharesmile.share.gps.models.Calorie;
import com.sharesmile.share.gps.models.DistRecord;
import com.sharesmile.share.gps.models.WorkoutData;

/**
 * Created by ankitm on 25/03/16.
 */
public interface WorkoutDataStore {

	void addRecord(DistRecord record);

	/**
	 * @return Avg speed of the workout in meter/sec
	 */
	float getAvgSpeed();

	/**
	 * gets total distance ran in workout in meters
	 * @return
	 */
	float getTotalDistance();

	/**
	 * @return Calorie object which holds calories burned since the beginning of the batch
	 */
	Calorie getCalories();

	long getBeginTimeStamp();

	void addSteps(int numSteps);

	/**
	 * @return total number of steps in the entire workout session
	 */
	int getTotalSteps();

	float getDistanceCoveredSinceLastResume();

	long getLastResumeTimeStamp();

	/**
	 * @return elapsed time interval in secs for which the workout was in running state( i.e. not paused)
	 * since the beginning
	 *
	 */
	float getElapsedTime();

	/**
	 * @return time interval in secs for which distance has been recorded
	 */
	float getRecordedTime();

	boolean coldStartAfterResume();

	void workoutPause();

	void workoutResume();

	void incrementUsainBoltCounter();

	boolean isWorkoutRunning();

	void approveWorkoutData();

	void discardApprovalQueue();

	LatLng getStartPoint();

	String getWorkoutId();

	Properties getWorkoutBundle();

	WorkoutData clear();

	int getUsainBoltCount();

	void setMockLocationDetected(boolean detected);

	boolean isMockLocationDetected();
}
