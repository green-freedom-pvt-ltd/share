package com.sharesmile.share.gps;

import com.sharesmile.share.analytics.events.Properties;

/**
 * Created by ankitm on 22/03/16.
 */
public interface IWorkoutService {

	void startWorkout();
	void stopWorkout();
	void pause(String reason);
	void resume();
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
//	DetectedActivity getDetectedActivity();

}
