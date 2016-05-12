package com.sharesmile.share.gps;

import android.hardware.SensorEvent;
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
	int getTotalSteps();
	float getTotalDistanceCovered();
	float getDistanceCoveredSinceLastResume();
	DistRecord getLastRecord();
	State getState();

	enum State{
		IDLE,
		RUNNING,
		PAUSED;
	}

}
