package com.sharesmile.share.gps;

import com.crashlytics.android.Crashlytics;
import com.sharesmile.share.core.Config;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.gps.activityrecognition.ActivityDetector;
import com.sharesmile.share.utils.DateUtil;
import com.sharesmile.share.utils.Logger;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.sharesmile.share.core.Config.CONFIDENCE_THRESHOLD_WALK_ENGAGEMENT;
import static com.sharesmile.share.core.Config.MIN_CADENCE_FOR_WALK;
import static com.sharesmile.share.core.Config.MIN_NUM_SPIKES_RATE_FOR_BAD_GPS;

/**
 * Created by ankitm on 18/03/16.
 */
public class VigilanceTimer implements Runnable {

	private static final String TAG = "VigilanceTimer";
	private ScheduledExecutorService scheduledExecutor;
	private IWorkoutService workoutService;
	private UsainBolt usainBolt;

//	private CircularQueue<RecentPerformance> recentPerformanceQueue;

	private int stepsTillNow = -1;
	private int numSpikesAtLastVigilance;
	private long lastVigilanceTimeStamp;

	private long timeWithContinuousBadGpsBehaviour;

	private static final int RECENT_PERF_QUEUE_SIZE = 4;

	public VigilanceTimer(IWorkoutService workoutService,
						  ScheduledExecutorService executorService){
		this.scheduledExecutor = executorService;
		this.workoutService = workoutService;
		scheduledExecutor.scheduleAtFixedRate(this, Config.VIGILANCE_TIMER_INTERVAL,
				Config.VIGILANCE_TIMER_INTERVAL, TimeUnit.MILLISECONDS);
		usainBolt = new UsainBolt(workoutService);
//		recentPerformanceQueue = new CircularQueue<>(RECENT_PERF_QUEUE_SIZE);
		numSpikesAtLastVigilance = WorkoutSingleton.getInstance().getDataStore().getNumGpsSpikes();
		lastVigilanceTimeStamp = DateUtil.getServerTimeInMillis();
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
		usainBolt.resetCounters();
		timeWithContinuousBadGpsBehaviour = 0;
//		synchronized (recentPerformanceQueue){
//			recentPerformanceQueue.clear();
//		}
	}

	public synchronized void resumeTimer(){
		numSpikesAtLastVigilance = WorkoutSingleton.getInstance().getDataStore().getNumGpsSpikes();
		lastVigilanceTimeStamp = DateUtil.getServerTimeInMillis();
		timeWithContinuousBadGpsBehaviour = 0;
	}

	private synchronized void onTimerTick(){
		Logger.d(TAG, "onTimerTick");

		if (WorkoutSingleton.getInstance().isRunning()){
			handleRecentPerformance();
		}

		//check for slow speed
		if (checkForTooSlow()){
			Logger.d(TAG, "Workout too slow, not enough distance will pause Workout");
			// Not enough steps/distance since the beginning
			workoutService.workoutVigilanceSessiondefaulted(Constants.PROBELM_TOO_SLOW);
			return;
		}

		//check for high speed
		if (usainBolt.checkForTooFast()){
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

	private void handleRecentPerformance(){
		// calculate current time
		long currentTime = DateUtil.getServerTimeInMillis();
		int currentNumSpikes = WorkoutSingleton.getInstance().getDataStore().getNumGpsSpikes();

		// Record recent performance
		long deltaMillis = currentTime - lastVigilanceTimeStamp;
		if (deltaMillis <= 0){
			return;
		}
		float recentSpeed = workoutService.getCurrentSpeed();
		float onFootConfidence = ActivityDetector.getInstance().getOnFootConfidence();
		float recentCadence = workoutService.getMovingAverageOfStepsPerSec();
		int recentNumSpikes = currentNumSpikes - numSpikesAtLastVigilance;

		RecentPerformance recentPerformance = new RecentPerformance();
		recentPerformance.setDeltaMillis(deltaMillis);
		recentPerformance.setRecentSpeed(recentSpeed);
		recentPerformance.setOnFootConfidenceRecentAvg(onFootConfidence);
		recentPerformance.setRecentCadence(recentCadence);
		recentPerformance.setRecentNumGpsSpikes(recentNumSpikes);

//		synchronized (recentPerformanceQueue){
//			recentPerformanceQueue.add(recentPerformance);
//
//		}

		lastVigilanceTimeStamp = currentTime;
		numSpikesAtLastVigilance = currentNumSpikes;

		// Check for possibilities of GPS misbehaviour
		float spikesPerSec = ((float) (recentNumSpikes * 1000)) / deltaMillis;

		if (recentSpeed < Config.GOOD_GPS_RECENT_SPEED_LOWER_THRESHOLD
				&& spikesPerSec > MIN_NUM_SPIKES_RATE_FOR_BAD_GPS
				&& ( onFootConfidence > CONFIDENCE_THRESHOLD_WALK_ENGAGEMENT || recentCadence > MIN_CADENCE_FOR_WALK))
		{
			// GPS misbehaved for this session
			timeWithContinuousBadGpsBehaviour += deltaMillis;
		} else {
			// GPS seems OK
			timeWithContinuousBadGpsBehaviour = 0;
			workoutService.cancelBadGpsNotification();
		}

		if (timeWithContinuousBadGpsBehaviour > 60000){
			// Time to show GPS signal Weak popup
			workoutService.notifyUserAboutBadGps();
		}
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

	public class RecentPerformance {

		long deltaMillis;
		float onFootConfidenceRecentAvg;
		float recentCadence;
		float recentSpeed;
		int recentNumGpsSpikes;


		public long getDeltaMillis() {
			return deltaMillis;
		}

		public void setDeltaMillis(long deltaMillis) {
			this.deltaMillis = deltaMillis;
		}

		public float getOnFootConfidenceRecentAvg() {
			return onFootConfidenceRecentAvg;
		}

		public void setOnFootConfidenceRecentAvg(float onFootConfidenceRecentAvg) {
			this.onFootConfidenceRecentAvg = onFootConfidenceRecentAvg;
		}

		public float getRecentCadence() {
			return recentCadence;
		}

		public void setRecentCadence(float recentCadence) {
			this.recentCadence = recentCadence;
		}

		public float getRecentSpeed() {
			return recentSpeed;
		}

		public void setRecentSpeed(float recentSpeed) {
			this.recentSpeed = recentSpeed;
		}

		public int getRecentNumGpsSpikes() {
			return recentNumGpsSpikes;
		}

		public void setRecentNumGpsSpikes(int recentNumGpsSpikes) {
			this.recentNumGpsSpikes = recentNumGpsSpikes;
		}
	}
}
