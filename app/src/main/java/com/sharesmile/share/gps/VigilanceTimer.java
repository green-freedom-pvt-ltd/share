package com.sharesmile.share.gps;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.location.DetectedActivity;
import com.sharesmile.share.core.Config;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.gps.models.DistRecord;
import com.sharesmile.share.utils.Logger;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by ankitm on 18/03/16.
 */
public class VigilanceTimer implements Runnable {

	private static final String TAG = "VigilanceTimer";
	private ScheduledExecutorService scheduledExecutor;
	private IWorkoutService workoutService;

	private int stepsTillNow = -1;
	private DistRecord lastValidatedRecord;
	private float lastValidatedDistance;
	private int lastValidatedNumSteps;

	public VigilanceTimer(IWorkoutService workoutService,
						  ScheduledExecutorService executorService){
		this.scheduledExecutor = executorService;
		this.workoutService = workoutService;
		scheduledExecutor.scheduleAtFixedRate(this, 0, Config.VIGILANCE_TIMER_INTERVAL, TimeUnit.MILLISECONDS);
	}

	@Override
	public void run() {
		try{
			Logger.d(TAG, "vigilanceTimer fired, tracker is NUll? " + (workoutService.getTracker() == null));
			if (workoutService.getTracker() != null){
				Logger.d(TAG, " tracker isActive? " + workoutService.getTracker().isActive()
						+ " and isRunning? " + workoutService.getTracker().isRunning());
			}
			if (workoutService.getTracker() != null && workoutService.getTracker().isActive()
					&& workoutService.getTracker().isRunning()){
				onTimerTick();
			}
		}catch(Exception e){
			Logger.d(TAG, "Problem in VigilanceTimer: " + e.getMessage());
			e.printStackTrace();
			Crashlytics.logException(e);
		}
	}

	public synchronized void pauseTimer(){
		resetCounters();
	}

	private void resetCounters(){
		Logger.d(TAG, "resetCounters");
		lastValidatedRecord = null;
		lastValidatedDistance = 0;
		lastValidatedNumSteps = 0;
	}

	private synchronized void onTimerTick(){

		Logger.d(TAG, "onTimerTick");


		//check for slow speed
		if (checkForTooSlow()){
			Logger.d(TAG, "Workout too slow, not enough distance will pause Workout");
			// Not enough steps/distance since the beginning
			workoutService.workoutVigilanceSessiondefaulted(Constants.PROBELM_TOO_SLOW);
			return;
		}

		//check for high speed
		if (checkForTooFast()){
			workoutService.workoutVigilanceSessiondefaulted(Constants.PROBELM_TOO_FAST);
			return;
		}

		if (workoutService.isCountingSteps()){
			// Check if user is actually running/moving
			if (checkForLackOfMovement()){
				workoutService.workoutVigilanceSessiondefaulted(Constants.PROBELM_NOT_MOVING);
				return;
			}
		}

		//Reaching here means everything is alright, vigilance approved
		long currentTime = System.currentTimeMillis();
		workoutService.workoutVigilanceSessionApproved(currentTime - Config.VIGILANCE_TIMER_INTERVAL,
									currentTime);

	}

	private boolean checkForTooSlow(){
		if (!Config.TOO_SLOW_CHECK){
			return false;
		}
		long timeElapsedSinceLastResume = System.currentTimeMillis() - workoutService.getTracker().getLastResumeTimeStamp();
		float inSecs = (float)(timeElapsedSinceLastResume / 1000);
		Logger.d(TAG, "onTick, Lower speed limit check, till now steps = " + workoutService.getTracker().getTotalSteps()
				+ ", timeElapsed in secs = " + inSecs
				+ ", distanceCovered = " +  workoutService.getTracker().getTotalDistanceCovered());
		if (timeElapsedSinceLastResume > Config.VIGILANCE_START_THRESHOLD
				&& workoutService.getTracker().getDistanceCoveredSinceLastResume() < inSecs*Config.LOWER_SPEED_LIMIT
				){
			return true;
		}
		return false;
	}

	private boolean checkForTooFast(){
		if (!Config.USAIN_BOLT_CHECK){
			return false;
		}

		if (ActivityRecognizedService.isIsInVehicle()){
			Logger.d(TAG, "Activity detected in_vehicle, must be Usain Bolt");
			return true;
		}

//		return tooFastSecondaryCheck();
		return false;

	}

	private boolean tooFastSecondaryCheck(){
		if (lastValidatedRecord == null){
			// Will wait for next tick
			lastValidatedRecord = workoutService.getTracker().getLastRecord();
			lastValidatedDistance = workoutService.getTracker().getTotalDistanceCovered();
			lastValidatedNumSteps = workoutService.getTracker().getTotalSteps();
		}else{
			DistRecord latestRecord = workoutService.getTracker().getLastRecord();
			if (!lastValidatedRecord.equals(latestRecord)){
				// We have a new record!
				float distanceInSession = workoutService.getTracker().getTotalDistanceCovered() - lastValidatedDistance;
				int stepsInSession = workoutService.getTracker().getTotalSteps() - lastValidatedNumSteps;
				float timeElapsedInSecs = (float)
						((latestRecord.getLocation().getTime() - lastValidatedRecord.getLocation().getTime()) / 1000);
				Logger.d(TAG, "onTick Upper speed limit check, Distance in last session = " + distanceInSession
						+ ", timeElapsedInSecs = " + timeElapsedInSecs
						+ ", distanceCovered = " +  workoutService.getTracker().getTotalDistanceCovered());
				if (distanceInSession > Config.MIN_DISTANCE_FOR_VIGILANCE){
					// Distance is above the threshold minimum to apply Usain Bolt Filter
					float speedInSession = distanceInSession / timeElapsedInSecs;
					if (speedInSession > Config.UPPER_SPEED_LIMIT){
						// Running faster than Usain Bolt
						Logger.d(TAG, "Speed " + speedInSession + " m/s is too fast, will check if runner covered sufficient steps");
						float averageStrideLength = (RunTracker.getAverageStrideLength() == 0)
								? (Config.GLOBAL_AVERAGE_STRIDE_LENGTH) : RunTracker.getAverageStrideLength();
						int expectedNumOfSteps = (int) (distanceInSession / averageStrideLength);
						Logger.d(TAG, "averageStrideLength = " + averageStrideLength + "meters hence "
								+ " expectedNumOfSteps = " + expectedNumOfSteps);

						if ( ( (float) stepsInSession / (float) expectedNumOfSteps) < Config.USAIN_BOLT_WAIVER_STEPS_RATIO){
							return true;
						}
					}
					lastValidatedRecord = latestRecord;
					lastValidatedDistance = workoutService.getTracker().getTotalDistanceCovered();
					lastValidatedNumSteps = workoutService.getTracker().getTotalSteps();
				}
			}
		}
		return false;
	}

	private boolean checkForLackOfMovement(){
		if (!Config.LAZY_ASS_CHECK){
			return false;
		}
		if (stepsTillNow == -1){
			//Will wait for the next tick
			stepsTillNow = workoutService.getTracker().getTotalSteps();
		}else{
			int totalSteps = workoutService.getTracker().getTotalSteps();
			int stepsThisSession = totalSteps - stepsTillNow;
			if (stepsThisSession < (Config.VIGILANCE_TIMER_INTERVAL / 1000)*Config.MIN_STEPS_PER_SECOND_FACTOR){
				//time to pause workout
				// Not enough steps since the beginning
				Logger.d(TAG, "Only " + stepsThisSession + " this session. Not enough! Will pause workout");
				return true;
			}else{
				stepsTillNow = totalSteps;
			}
		}
		return false;
	}
}
