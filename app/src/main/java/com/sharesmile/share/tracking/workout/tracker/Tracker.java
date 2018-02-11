package com.sharesmile.share.tracking.workout.tracker;

import android.location.Location;

import com.sharesmile.share.tracking.workout.WorkoutSingleton;
import com.sharesmile.share.tracking.models.Calorie;
import com.sharesmile.share.tracking.models.DistRecord;

/**
 * Created by ankitm on 22/03/16.
 */
public interface Tracker {

	void endRun();
	void pauseRun(String reason);
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
	void incrementNumUpdateEvents();

}
