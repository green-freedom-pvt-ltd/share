package com.sharesmile.share.gps;

import android.location.Location;

import com.sharesmile.share.gps.models.Calorie;
import com.sharesmile.share.gps.models.DistRecord;
import com.sharesmile.share.gps.models.WorkoutData;

/**
 * Created by ankitm on 22/03/16.
 */
public interface Tracker {

	WorkoutData endRun();
	void pauseRun();
	void resumeRun();
	boolean isActive();
	boolean isPaused();
	boolean isRunning();
	void feedLocation(Location point);
	void feedSteps(int cumulativeSteps);
	void approveWorkoutData();
	float discardApprovalQueue();
	long getBeginTimeStamp();
	long getLastResumeTimeStamp();
	int getElapsedTimeInSecs();
	float getRecordedTimeInSecs();
	int getTotalSteps();
	float getTotalDistanceCovered();
	Calorie getCalories();
	float getDistanceCoveredSinceLastResume();
	float getAvgSpeed();
	float getCurrentSpeed();
	DistRecord getLastRecord();
	String getCurrentWorkoutId();
	WorkoutSingleton.State getState();
	void incrementGpsSpike();

}
