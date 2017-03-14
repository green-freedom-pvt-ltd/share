package com.sharesmile.share.gps;

import android.location.Location;

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
	void discardApprovalQueue();
	long getBeginTimeStamp();
	long getLastResumeTimeStamp();
	int getElapsedTimeInSecs();
	float getRecordedTimeInSecs();
	int getTotalSteps();
	float getTotalDistanceCovered();
	float getDistanceCoveredSinceLastResume();
	float getAvgSpeed();
	float getCurrentSpeed();
	DistRecord getLastRecord();
	String getCurrentWorkoutId();
	State getState();

	enum State{
		IDLE,
		RUNNING,
		PAUSED;
	}

}
