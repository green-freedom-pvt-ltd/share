package com.sharesmile.share.gps;

import com.sharesmile.share.analytics.events.AnalyticsEvent;
import com.sharesmile.share.analytics.events.Event;
import com.sharesmile.share.core.ClientConfig;
import com.sharesmile.share.core.Config;
import com.sharesmile.share.gps.activityrecognition.ActivityDetector;
import com.sharesmile.share.utils.Logger;
import com.sharesmile.share.utils.Utils;

import static com.sharesmile.share.core.Config.GLOBAL_AVERAGE_STRIDE_LENGTH_LOWER_LIMIT;
import static com.sharesmile.share.core.Config.GLOBAL_AVERAGE_STRIDE_LENGTH_UPPER_LIMIT;

/**
 * Created by ankitmaheshwari on 6/24/17.
 */

public class UsainBolt {

    private static final String TAG = "UsainBolt";

    private IWorkoutService workoutService;
    private float lastValidatedRecordedTimeInSecs; // in secs
    private float lastValidatedDistance;
    private int lastValidatedNumSteps;

    public UsainBolt(IWorkoutService workoutService){
        this.workoutService = workoutService;
        resetCounters();
    }

    public void resetCounters(){
        Logger.d(TAG, "resetCounters");
        this.lastValidatedRecordedTimeInSecs = workoutService.getTracker().getRecordedTimeInSecs(); // in secs
        this.lastValidatedDistance = workoutService.getTracker().getTotalDistanceCovered();
        this.lastValidatedNumSteps = workoutService.getTracker().getTotalSteps();
    }

    /**
     * This check is triggered after every VIGILANCE_TIMER_INTERVAL (17 secs)
     * @return true if we believe that user is in a vehicle
     */
    public boolean checkForTooFast(){
        if (!Config.USAIN_BOLT_CHECK){
            return false;
        }

        // recentSpeed is avg speed from recent few samples of accepted GPS points in the past 24 secs
        float recentSpeed = workoutService.getCurrentSpeed();
        // If recentSpeed is above USAIN_BOLT_RECENT_SPEED_LOWER_BOUND (14.8 km/hr) then only enter the check for USAIN_BOLT
        if (recentSpeed > ClientConfig.getInstance().USAIN_BOLT_RECENT_SPEED_LOWER_BOUND){

            // recent is avg GPS speed from recent few samples of accepted GPS points in the past 24 secs
            // GPS speed is obtained directly from location object and is calculated using doppler shift
            float recentGpsSpeed = GoogleLocationTracker.getInstance().getRecentGpsSpeed();
            // If recentGpsSpeed is above USAIN_BOLT_GPS_SPEED_LIMIT (21 km/hr) then user must be in a vehicle
            if (recentGpsSpeed > ClientConfig.getInstance().USAIN_BOLT_GPS_SPEED_LIMIT){
                Logger.d(TAG, "Recent GPS speed is greater than threshold, must be Usain Bolt");
                AnalyticsEvent.create(Event.ON_USAIN_BOLT_ALERT)
                        .addBundle(workoutService.getWorkoutBundle())
                        .put("detected_by", "gps_speed")
                        .put("gps_speed", recentGpsSpeed*3.6)
                        .put("recent_speed", recentSpeed*3.6)
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
            if (averageStrideLength < GLOBAL_AVERAGE_STRIDE_LENGTH_LOWER_LIMIT){
                averageStrideLength = GLOBAL_AVERAGE_STRIDE_LENGTH_LOWER_LIMIT;
            }
            if (averageStrideLength > GLOBAL_AVERAGE_STRIDE_LENGTH_UPPER_LIMIT){
                averageStrideLength = GLOBAL_AVERAGE_STRIDE_LENGTH_UPPER_LIMIT;
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
                    upperSpeedLimit = ClientConfig.getInstance().USAIN_BOLT_UPPER_SPEED_LIMIT_ON_FOOT; // 45 km/hr
                }else {
                    upperSpeedLimit = ClientConfig.getInstance().USAIN_BOLT_UPPER_SPEED_LIMIT; // 25.2 km/hr
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
                    if (stepsRatio < ClientConfig.getInstance().USAIN_BOLT_WAIVER_STEPS_RATIO){
                        // Speed more than limit, and insufficient num of steps,
                        // user definitely must be inside a vehicle
                        AnalyticsEvent.create(Event.ON_USAIN_BOLT_ALERT)
                                .addBundle(workoutService.getWorkoutBundle())
                                .put("detected_by", "speed_logic")
                                .put("recent_speed", recentSpeed*3.6)
                                .put("cadence_ratio", stepsRatio)
                                .put("activity", ActivityDetector.getInstance().getCurrentActivity())
                                .buildAndDispatch();
                        return true;
                    }
                }
                lastValidatedRecordedTimeInSecs = currentRecordedTime;
                lastValidatedDistance = currentDistanceCovered;
                lastValidatedNumSteps = currentNumSteps;
            }
            if (speedInSession > ClientConfig.getInstance().USAIN_BOLT_UPPER_SPEED_LIMIT){
                // Potential Usain Bolt Which was missed
                AnalyticsEvent.create(Event.ON_POTENTIAL_USAIN_BOLT_MISSED)
                        .addBundle(workoutService.getWorkoutBundle())
                        .put("speed_in_session", speedInSession*3.6)
                        .put("recent_speed", recentSpeed*3.6)
                        .put("steps_ratio", stepsRatio)
                        .put("activity", ActivityDetector.getInstance().getCurrentActivity())
                        .put("bolt_count", WorkoutSingleton.getInstance().getDataStore().getUsainBoltCount())
                        .buildAndDispatch();
            }
        }
        return false;
    }
}
