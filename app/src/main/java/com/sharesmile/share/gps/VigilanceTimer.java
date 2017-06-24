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

import static com.sharesmile.share.core.Config.USAIN_BOLT_GPS_SPEED_LIMIT;

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

	/**
	 * This check is triggered after every VIGILANCE_TIMER_INTERVAL (17 secs)
	 * @return true if we believe that user is in a vehicle
	 */
	private boolean checkForTooFast(){
		if (!Config.USAIN_BOLT_CHECK){
			return false;
		}

		// recentSpeed is avg speed from recent few samples of accepted GPS points in the past 24 secs
		float recentSpeed = workoutService.getCurrentSpeed();
		// If recentSpeed is above USAIN_BOLT_RECENT_SPEED_LOWER_BOUND (14.8 km/hr) then only enter the check for USAIN_BOLT
		if (recentSpeed > Config.USAIN_BOLT_RECENT_SPEED_LOWER_BOUND){

			// recent is avg GPS speed from recent few samples of accepted GPS points in the past 24 secs
			// GPS speed is obtained directly from location object and is calculated using doppler shift
			float recentGpsSpeed = GoogleLocationTracker.getInstance().getRecentGpsSpeed();
			// If recentGpsSpeed is above USAIN_BOLT_GPS_SPEED_LIMIT (24 km/hr) then user must be in a vehicle
			if (recentGpsSpeed > USAIN_BOLT_GPS_SPEED_LIMIT){
				Logger.d(TAG, "Recent GPS speed is greater than threshold, must be Usain Bolt");
				AnalyticsEvent.create(Event.ON_USAIN_BOLT_ALERT)
						.addBundle(workoutService.getWorkoutBundle())
						.put("detected_by", "gps_speed")
						.put("recent_speed", recentSpeed*3.6)
						.put("recent_gps_speed", recentGpsSpeed*3.6)
						.buildAndDispatch();
				return true;
			}

			// If ActivityDetector says that user is in a vehicle with confidence then user must be in a vehicle
			if (ActivityDetector.getInstance().isIsInVehicle()){
				Logger.d(TAG, "ActivityRecognition detected IN_VEHICLE, must be Usain Bolt");
				AnalyticsEvent.create(Event.ON_USAIN_BOLT_ALERT)
						.addBundle(workoutService.getWorkoutBundle())
						.put("detected_by", "activity_recognition")
						.put("recent_speed", recentSpeed*3.6)
						.put("time_considered_ad", ActivityDetector.getInstance().getTimeCoveredByHistoryQueueInSecs())
						.buildAndDispatch();
				return true;
			}
			// Go for speed logic based check with recent speed as input
			return tooFastSecondaryCheck(recentSpeed);
		}
		return false;
	}

	/**
	 * Returns true if it thinks the user is in a vehicle as per speed calculations
	 * @param recentSpeed
	 * @return
	 */
	private boolean tooFastSecondaryCheck(float recentSpeed){
		/*
		  We store recordedTime (as lastValidatedRecordedTimeInSecs), distanceCovered (as lastValidatedDistance)
		  and numSteps (as lastValidatedNumSteps) every time this check is cleared successfully
		  */
		float currentRecordedTime = workoutService.getTracker().getRecordedTimeInSecs();
		float currentDistanceCovered = workoutService.getTotalDistanceCoveredInMeters();
		int currentNumSteps = workoutService.getTotalStepsInWorkout();

		// Proceed for check only when we have recorded more distance after the last time recorded distance was validated
		if (currentRecordedTime > lastValidatedRecordedTimeInSecs){
			float distanceInSession = currentDistanceCovered - lastValidatedDistance;
			int stepsInSession = currentNumSteps - lastValidatedNumSteps;
			float timeElapsedInSecs = currentRecordedTime - lastValidatedRecordedTimeInSecs;
			Logger.d(TAG, "onTick Upper speed limit check, Distance in last session = " + distanceInSession
					+ ", timeElapsedInSecs = " + timeElapsedInSecs
					+ ", total distanceCovered = " +  workoutService.getTracker().getTotalDistanceCovered());

			// Calculate speed in the last recorded session
			float speedInSession = distanceInSession / timeElapsedInSecs;

			// Calculate avgStrideLength (distance covered in one foot step) of the user
			float averageStrideLength = (Utils.getAverageStrideLength() == 0)
					? (Config.GLOBAL_AVERAGE_STRIDE_LENGTH) : Utils.getAverageStrideLength();
			// Normalising averageStrideLength obtained
			if (averageStrideLength < 0.3f){
				averageStrideLength = 0.3f;
			}
			if (averageStrideLength > 1f){
				averageStrideLength = 1f;
			}
			// Calculate expected num of steps in the recorded session based on speed and avgStrideLength
			int expectedNumOfSteps = (int) (distanceInSession / averageStrideLength);
			// ratio between actual steps counted and the expected num of steps in recorded session
			float stepsRatio = ( (float) stepsInSession / (float) expectedNumOfSteps);

			// Distance is above the threshold minimum MIN_DISTANCE_FOR_VIGILANCE (50 m) to apply Usain Bolt Filter
			if (distanceInSession > Config.MIN_DISTANCE_FOR_VIGILANCE){

				// If user is onFoot (less probability of user being in vehicle) then
				// speed limit is different then when the user is not onFoot (more probability of being in vehicle).
				// speed limit is greater for ON_FOOT cases to reduces Usain Bolt false positive occurrences
				float upperSpeedLimit;
				if (ActivityDetector.getInstance().isOnFoot()){
					upperSpeedLimit = Config.USAIN_BOLT_UPPER_SPEED_LIMIT_ON_FOOT; // 45 km/hr
				}else {
					upperSpeedLimit = Config.USAIN_BOLT_UPPER_SPEED_LIMIT; // 25.2 km/hr
				}

				// Check if the speed is greater than allowed limit
				if ( speedInSession > upperSpeedLimit ){
					/*
						 Running faster than Usain Bolt, but we have to give benefit of doubt
						 and waive the user from USAIN_BOLT if he/she has covered
						 a fraction (USAIN_BOLT_WAIVER_STEPS_RATIO = 0.27) of expected num of steps
						 during this recorded session.
						 This is done to avoid false USAIN_BOLT cases where the user is actually walking
						 and speed goes above limit because of GPS spikes
					 */
					if (stepsRatio < Config.USAIN_BOLT_WAIVER_STEPS_RATIO){
						// Speed more than limit, and insufficient num of steps,
						// user definitely must be inside a vehicle
						AnalyticsEvent.create(Event.ON_USAIN_BOLT_ALERT)
								.addBundle(workoutService.getWorkoutBundle())
								.put("detected_by", "speed_logic")
								.put("speed_in_session", speedInSession*3.6)
								.put("recent_speed", recentSpeed*3.6)
								.put("steps_ratio", stepsRatio)
								.put("activity", ActivityDetector.getInstance().getCurrentActivity())
								.buildAndDispatch();
						return true;
					}
				}
				lastValidatedRecordedTimeInSecs = currentRecordedTime;
				lastValidatedDistance = currentDistanceCovered;
				lastValidatedNumSteps = currentNumSteps;
			}
			if (speedInSession > Config.USAIN_BOLT_UPPER_SPEED_LIMIT){
				// Potential Usain Bolt Which was missed
				AnalyticsEvent.create(Event.ON_POTENTIAL_USAIN_BOLT_MISSED)
						.addBundle(workoutService.getWorkoutBundle())
						.put("speed_in_session", speedInSession*3.6)
						.put("recent_speed", recentSpeed*3.6)
						.put("steps_ratio", stepsRatio)
						.put("activity", ActivityDetector.getInstance().getCurrentActivity())
						.buildAndDispatch();
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
