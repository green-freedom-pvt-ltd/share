package com.sharesmile.share.gps;

import com.google.android.gms.maps.model.LatLng;
import com.sharesmile.share.gps.models.DistRecord;
import com.sharesmile.share.gps.models.WorkoutData;

/**
 * Created by ankitm on 25/03/16.
 */
public interface WorkoutDataStore {

	void addRecord(DistRecord record);

	float getAvgSpeed();

	float getTotalDistance();

	long getBeginTimeStamp();

	void addSteps(int numSteps);

	int getTotalSteps();

	float getDistanceCoveredSinceLastResume();

	long getLastResumeTimeStamp();

	float getElapsedTime();

	float getRecordedTime();

	boolean coldStartAfterResume();

	void workoutPause();

	void workoutResume();

	boolean isWorkoutRunning();

	void approveWorkoutData();

	void discardApprovalQueue();

	LatLng getStartPoint();

	String getWorkoutId();

	WorkoutData clear();
}
