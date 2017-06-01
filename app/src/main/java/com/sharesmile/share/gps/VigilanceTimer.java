package com.sharesmile.share.gps;

import com.crashlytics.android.Crashlytics;
import com.sharesmile.share.analytics.events.AnalyticsEvent;
import com.sharesmile.share.analytics.events.Event;
import com.sharesmile.share.core.Config;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.gps.activityrecognition.ActivityDetector;
import com.sharesmile.share.utils.DateUtil;
import com.sharesmile.share.utils.Logger;
import com.sharesmile.share.utils.Utils;

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
	private float lastValidatedRecordedTimeInSecs; // in secs
	private float lastValidatedDistance;
	private int lastValidatedNumSteps;

	public VigilanceTimer(IWorkoutService workoutService,
						  ScheduledExecutorService executorService){
		this.scheduledExecutor = executorService;
		this.workoutService = workoutService;
		scheduledExecutor.scheduleAtFixedRate(this, 0, Config.VIGILANCE_TIMER_INTERVAL, TimeUnit.MILLISECONDS);
		resetCounters();
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
		this.lastValidatedRecordedTimeInSecs = workoutService.getTracker().getRecordedTimeInSecs(); // in secs
		this.lastValidatedDistance = workoutService.getTracker().getTotalDistanceCovered();
		this.lastValidatedNumSteps = workoutService.getTracker().getTotalSteps();
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
		long currentTime = DateUtil.getServerTimeInMillis();
		workoutService.workoutVigilanceSessionApproved(currentTime - Config.VIGILANCE_TIMER_INTERVAL,
									currentTime);

	}

	private boolean checkForTooSlow(){
		if (!Config.TOO_SLOW_CHECK){
			return false;
		}
		long timeElapsedSinceLastResume = DateUtil.getServerTimeInMillis() - workoutService.getTracker().getLastResumeTimeStamp();
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
		float recentSpeed = workoutService.getCurrentSpeed();
		// If recentSpeed is above lower bounds then only enter the check.
		if (recentSpeed > Config.USAIN_BOLT_RECENT_SPEED_LOWER_BOUND){
			if (ActivityDetector.getInstance().isIsInVehicle()){
				Logger.d(TAG, "ActivityRecognition detected IN_VEHICLE, must be Usain Bolt");
				AnalyticsEvent.create(Event.ON_USAIN_BOLT_ALERT)
						.addBundle(workoutService.getWorkoutBundle())
						.put("detected_by", "activity_recognition")
						.put("recent_speed", recentSpeed*3.6)
						.buildAndDispatch();
				return true;
			}
			return tooFastSecondaryCheck();
		}
		return false;
	}

	private boolean tooFastSecondaryCheck(){

		float currentRecordedTime = workoutService.getTracker().getRecordedTimeInSecs();
		float currentDistanceCovered = workoutService.getTotalDistanceCoveredInMeters();
		int currentNumSteps = workoutService.getTotalStepsInWorkout();

		if (currentRecordedTime > lastValidatedRecordedTimeInSecs){
			float distanceInSession = currentDistanceCovered - lastValidatedDistance;
			int stepsInSession = currentNumSteps - lastValidatedNumSteps;
			float timeElapsedInSecs = currentRecordedTime - lastValidatedRecordedTimeInSecs;
			Logger.d(TAG, "onTick Upper speed limit check, Distance in last session = " + distanceInSession
					+ ", timeElapsedInSecs = " + timeElapsedInSecs
					+ ", total distanceCovered = " +  workoutService.getTracker().getTotalDistanceCovered());
			if (distanceInSession > Config.MIN_DISTANCE_FOR_VIGILANCE){
				// Distance is above the threshold minimum to apply Usain Bolt Filter
				float speedInSession = distanceInSession / timeElapsedInSecs;
				float upperSpeedLimit = ActivityDetector.getInstance().isOnFoot()
						? Config.USAIN_BOLT_UPPER_SPEED_LIMIT_ON_FOOT : Config.USAIN_BOLT_UPPER_SPEED_LIMIT;
				// Speed limit is greater for ON_FOOT cases to reduces Usain Bolt false positive occurrences
				if (speedInSession > upperSpeedLimit){
					// Running faster than Usain Bolt
					Logger.d(TAG, "Speed " + speedInSession + " m/s is too fast, will check if runner covered sufficient steps");

					float averageStrideLength = (Utils.getAverageStrideLength() == 0)
							? (Config.GLOBAL_AVERAGE_STRIDE_LENGTH) : Utils.getAverageStrideLength();

					// Normalising averageStrideLength obtained
					if (averageStrideLength < 0.25f){
						averageStrideLength = 0.25f;
					}
					if (averageStrideLength > 1f){
						averageStrideLength = 1f;
					}

					int expectedNumOfSteps = (int) (distanceInSession / averageStrideLength);
					Logger.d(TAG, "averageStrideLength = " + averageStrideLength + " meters hence "
							+ " expectedNumOfSteps = " + expectedNumOfSteps + ", but actual number of steps are "
							+ stepsInSession);

					if ( ( (float) stepsInSession / (float) expectedNumOfSteps) < Config.USAIN_BOLT_WAIVER_STEPS_RATIO){
						AnalyticsEvent.create(Event.ON_USAIN_BOLT_ALERT)
								.addBundle(workoutService.getWorkoutBundle())
								.put("detected_by", "speed_logic")
								.put("speed_in_session", speedInSession*3.6)
								.put("recent_speed", workoutService.getCurrentSpeed()*3.6)
								.buildAndDispatch();
						return true;
					}
				}
				lastValidatedRecordedTimeInSecs = currentRecordedTime;
				lastValidatedDistance = currentDistanceCovered;
				lastValidatedNumSteps = currentNumSteps;
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
