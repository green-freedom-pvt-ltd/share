package com.sharesmile.share.tracking.workout.service;

import com.sharesmile.share.analytics.events.Properties;
import com.sharesmile.share.tracking.workout.tracker.Tracker;

/**
 * Created by ankitm on 22/03/16.
 */
public interface IWorkoutService {

	void startWorkout();
	void stopWorkout();
	void pause(String reason);
	boolean resume();
	void workoutVigilanceSessiondefaulted(int problem);
	void workoutVigilanceSessionApproved(long sessionStartTime, long sessionEndTime);
	boolean isCountingSteps();
	float getTotalDistanceCoveredInMeters();
	long getWorkoutElapsedTimeInSecs();
	int getTotalStepsInWorkout();
	float getCurrentSpeed();
	float getAvgSpeed();
	Tracker getTracker();
	Properties getWorkoutBundle();
	float getMovingAverageOfStepsPerSec();
	void notifyUserAboutBadGps();
	void cancelBadGpsNotification();

}
