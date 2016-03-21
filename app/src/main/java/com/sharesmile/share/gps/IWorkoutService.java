package com.sharesmile.share.gps;

/**
 * Created by ankitm on 22/03/16.
 */
public interface IWorkoutService {

	void startWorkout();
	void stopWorkout();
	void pause();
	void resume();

	void sendStopWorkoutBroadcast(int problem);
	boolean isCountingSteps();

}
