package com.sharesmile.share.gps;

import com.google.android.gms.location.DetectedActivity;

/**
 * Created by ankitm on 22/03/16.
 */
public interface IWorkoutService {

	void startWorkout();
	void stopWorkout();
	void pause();
	void resume();
	void workoutVigilanceSessiondefaulted(int problem);
	void workoutVigilanceSessionApproved(long sessionStartTime, long sessionEndTime);
	boolean isCountingSteps();
	float getTotalDistanceCoveredInMeters();
	long getWorkoutElapsedTimeInSecs();
	int getTotalStepsInWorkout();
	float getCurrentSpeed();
	Tracker getTracker();
//	DetectedActivity getDetectedActivity();

}
