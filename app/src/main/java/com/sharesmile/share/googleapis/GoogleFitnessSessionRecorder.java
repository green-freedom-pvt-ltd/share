package com.sharesmile.share.googleapis;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessActivities;
import com.google.android.gms.fitness.FitnessStatusCodes;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Session;
import com.google.android.gms.fitness.data.Value;
import com.google.android.gms.fitness.request.SessionReadRequest;
import com.google.android.gms.fitness.result.SessionReadResult;
import com.google.android.gms.fitness.result.SessionStopResult;
import com.google.gson.reflect.TypeToken;
import com.sharesmile.share.MainApplication;
import com.sharesmile.share.R;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.gps.WorkoutDataStore;
import com.sharesmile.share.gps.WorkoutSingleton;
import com.sharesmile.share.gps.models.WorkoutData;
import com.sharesmile.share.utils.DateUtil;
import com.sharesmile.share.utils.Logger;
import com.sharesmile.share.utils.SharedPrefsManager;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.text.DateFormat.getTimeInstance;

/**
 * Created by ankitmaheshwari on 12/1/17.
 */

public class GoogleFitnessSessionRecorder implements  GoogleApiHelper.Listener, GoogleTracker{

    private static final String TAG = "GoogleFitnessSessionRecorder";

    private GoogleApiHelper helper;
    List<String> sessionIds;

    public GoogleFitnessSessionRecorder(Context context) {
        this.helper = new GoogleApiHelper(GoogleApiHelper.API_SESSION_RECORDING, context);
        this.sessionIds = SharedPrefsManager.getInstance().getCollection(Constants.PREF_FITNESS_SESSION_IDS,
                new TypeToken<ArrayList<String>>(){}.getType());
        if (sessionIds == null){
            sessionIds = new ArrayList<>();
        }
    }

    @Override
    public void onConnected() {
        subscribeToFitnessData();
        // Call resume to start the first session
        resume();
    }

    @Override
    public void connectionFailed() {
        Logger.d(TAG, "User Denied permission to access Google Fit Data");
        MainApplication.showToast(R.string.google_fit_permission_rationale);
    }

    @Override
    public void start() {
        Logger.d(TAG, "start");
        helper.setListener(this);
        helper.connect();
    }

    private void subscribeToFitnessData(){

        subscribeToDataType(DataType.TYPE_STEP_COUNT_DELTA);
        subscribeToDataType(DataType.TYPE_STEP_COUNT_CUMULATIVE);
        subscribeToDataType(DataType.TYPE_DISTANCE_DELTA);
        subscribeToDataType(DataType.TYPE_CALORIES_EXPENDED);

    }

    private void subscribeToDataType(final DataType dataType){
        Fitness.RecordingApi.subscribe(helper.getGoogleApiClient(), dataType)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            if (status.getStatusCode()
                                    == FitnessStatusCodes.SUCCESS_ALREADY_SUBSCRIBED) {
                                Logger.d(TAG, "Existing subscription for "+dataType.getName()+" detected.");
                            } else {
                                Logger.d(TAG, "Successfully subscribed to " + dataType.getName());
                            }
                        } else {
                            Logger.d(TAG, "There was a problem subscribing to " + dataType.getName());
                        }
                    }
                });
    }

    private void cancelSubscriptions(){

        unsubscribeDataType(DataType.TYPE_STEP_COUNT_DELTA);
        unsubscribeDataType(DataType.TYPE_DISTANCE_CUMULATIVE);
        unsubscribeDataType(DataType.TYPE_DISTANCE_DELTA);
        unsubscribeDataType(DataType.TYPE_CALORIES_EXPENDED);

    }

    private void unsubscribeDataType(final DataType dataType){
        Fitness.RecordingApi.unsubscribe(helper.getGoogleApiClient(), dataType)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        if (status.isSuccess()) {
                            Logger.d(TAG, "Successfully unsubscribed to: " + dataType.getName());
                        } else {
                            Logger.d(TAG, "There was a problem in unsubscribing " + dataType.getName());
                        }
                    }
                });
    }

    @Override
    public void stop() {
        Logger.d(TAG, "stop");
        pause();
        cancelSubscriptions();
    }

    @Override
    public void pause() {
        Logger.d(TAG, "pause");
        final String currentSessionId = getLatestSessionId();
        PendingResult<SessionStopResult> pendingResult =
                Fitness.SessionsApi.stopSession(helper.getGoogleApiClient(), currentSessionId);

        pendingResult.setResultCallback(new ResultCallback<SessionStopResult>() {
            @Override
            public void onResult(SessionStopResult sessionStopResult) {
                if( sessionStopResult.getStatus().isSuccess() ) {
                    Logger.d(TAG, "Successfully stopped session " + currentSessionId);
                } else {
                    Logger.d(TAG, "Failed to stop session: " + currentSessionId);
                }
            }
        });
    }

    public String getLatestSessionId(){
        return sessionIds == null ? null : sessionIds.get(sessionIds.size() - 1);
    }

    public static String getSessionIdentifier(String workoutId, int index){
        return "session_" + workoutId + "_" + index;
    }

    public static String getSessionName(String workoutId){
        return "session_" + workoutId;
    }

    public void readStepCountData(final WorkoutData data){
        Logger.d(TAG, "Reading step count data between " + data.getBeginTimeStamp() + " and "
                + DateUtil.getServerTimeInMillis() + " for workoutId " + data.getWorkoutId());

        SessionReadRequest readRequest = new SessionReadRequest.Builder()
                .setTimeInterval(data.getBeginTimeStamp() - 60000, System.currentTimeMillis(), TimeUnit.MILLISECONDS)
//                .setTimeInterval(1512629718705L, 1512629741533L, TimeUnit.SECONDS)
                .read(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                .setSessionName(getSessionName(data.getWorkoutId()))
//                .setSessionName("session_00f768b5-1889-47f3-b9b3-18b25f98ed31_0")
                .build();


        /*

        // [START read_session]
        // Invoke the Sessions API to fetch the session with the query and wait for the result
        // of the read request. Note: Fitness.SessionsApi.readSession() requires the
        // ACCESS_FINE_LOCATION permission.
        Fitness.getSessionsClient(MainApplication.getContext(),
                GoogleSignIn.getLastSignedInAccount(MainApplication.getContext()))
                .readSession(readRequest)
                .addOnSuccessListener(new OnSuccessListener<SessionReadResponse>() {
                    @Override
                    public void onSuccess(SessionReadResponse sessionReadResponse) {
                        // Get a list of the sessions that match the criteria to check the result.
                        List<Session> sessions = sessionReadResponse.getSessions();
                        Logger.d(TAG, "Session read was successful. Number of returned sessions is: "
                                + sessions.size());

                        for (Session session : sessions) {
                            // Process the session
                            dumpSession(session);

                            // Process the data sets for this session
                            List<DataSet> dataSets = sessionReadResponse.getDataSet(session);
                            for (DataSet dataSet : dataSets) {
                                dumpDataSet(dataSet);
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Logger.d(TAG , "Failed to read session");
                    }
                });

        */


        PendingResult<SessionReadResult> sessionReadResult =
                Fitness.SessionsApi.readSession(helper.getGoogleApiClient(), readRequest);

        sessionReadResult.setResultCallback(new ResultCallback<SessionReadResult>() {
            @Override
            public void onResult(SessionReadResult sessionReadResult) {
                if (sessionReadResult.getStatus().isSuccess()) {
                    Logger.d(TAG, "Successfully read step count data");
                    int overallStepCount = 0;
                    for (Session session : sessionReadResult.getSessions()) {
                        dumpSession(session);
                        int stepCount = 0;
                        for (DataSet dataSet : sessionReadResult.getDataSet(session)) {
                            dumpDataSet(dataSet);
                            for (DataPoint dataPoint : dataSet.getDataPoints()) {
                                Value value = dataPoint.getValue(Field.FIELD_STEPS);
                                Logger.d(TAG, "Datapoint steps = " + value);
                                stepCount = stepCount + value.asInt();
                            }
                        }
                        Logger.d(TAG, "Total Steps in session " + session.getIdentifier() + " = "
                                + stepCount);
                        overallStepCount += stepCount;
                    }
                    Logger.d(TAG, "overallStepCount = " + overallStepCount);
                } else {
                    Logger.i(TAG, "Failed to read session data");
                }
            }
        });
    }

    private void dumpSession(Session session) {
        DateFormat dateFormat = getTimeInstance();
        Logger.d(TAG, "Data returned for Session: " + session.getName()
                + "\tDescription: " + session.getDescription()
                + "\tStart: " + dateFormat.format(session.getStartTime(TimeUnit.MILLISECONDS))
                + "\tEnd: " + dateFormat.format(session.getEndTime(TimeUnit.MILLISECONDS)));
    }


    private void dumpDataSet(DataSet dataSet) {
        Logger.d(TAG, "Data returned for Data type: " + dataSet.getDataType().getName());
        for (DataPoint dp : dataSet.getDataPoints()) {
            DateFormat dateFormat = getTimeInstance();
            Logger.d(TAG, "Data point:");
            Logger.d(TAG, "\tType: " + dp.getDataType().getName());
            Logger.d(TAG, "\tStart: " + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
            Logger.d(TAG, "\tEnd: " + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)));
            for(Field field : dp.getDataType().getFields()) {
                Logger.d(TAG, "\tField: " + field.getName() +
                        " Value: " + dp.getValue(field));
            }
        }
    }

    public void readDistanceData(final WorkoutData data){
        Logger.d(TAG, "Reading distance data between " + data.getBeginTimeStamp() + " and "
                + DateUtil.getServerTimeInMillis() + " for workoutId " + data.getWorkoutId());
        SessionReadRequest readRequest = new SessionReadRequest.Builder()
                .setTimeInterval(data.getBeginTimeStamp() - 60000, System.currentTimeMillis(),
                        TimeUnit.MILLISECONDS)
                .read(DataType.TYPE_DISTANCE_CUMULATIVE)
                .setSessionName(getSessionName(data.getWorkoutId()))
                .build();

        PendingResult<SessionReadResult> sessionReadResult =
                Fitness.SessionsApi.readSession(helper.getGoogleApiClient(), readRequest);

        sessionReadResult.setResultCallback(new ResultCallback<SessionReadResult>() {
            @Override
            public void onResult(SessionReadResult sessionReadResult) {
                if (sessionReadResult.getStatus().isSuccess()) {
                    Logger.d(TAG, "Successfully read distance data");
                    float overallDistance = 0;
                    for (Session session : sessionReadResult.getSessions()) {
                        Logger.d(TAG, "Session name: " + session.getName() + ", session identifier "
                                + session.getIdentifier());
                        float distance = 0;
                        for (DataSet dataSet : sessionReadResult.getDataSet(session)) {
                            for (DataPoint dataPoint : dataSet.getDataPoints()) {
                                Value value = dataPoint.getValue(Field.FIELD_DISTANCE);
                                Logger.d(TAG, "Datapoint distance = " + value);
                                distance = distance + value.asFloat();
                            }
                        }
                        Logger.d(TAG, "Total distance in session " + session.getIdentifier() + " = "
                                + distance);
                        overallDistance += distance;
                    }
                    Logger.d(TAG, "overallDistance = " + overallDistance);
                } else {
                    Logger.i(TAG, "Failed to read session data");
                }
            }
        });
    }

    @Override
    public void resume() {
        Logger.d(TAG, "resume");

        WorkoutDataStore dataStore = WorkoutSingleton
                .getInstance().getDataStore();
        if (dataStore == null){
            // Do Nothing, silently return
            return;
        }
        int batchIndex = dataStore.getCurrentBatchIndex();
        final String currentSessionIdentifier = getSessionIdentifier(dataStore.getWorkoutId(), batchIndex);
        final String currentSessionName = getSessionName(dataStore.getWorkoutId());

        if (!sessionIds.isEmpty() && currentSessionIdentifier.equals(getLatestSessionId())){
            // Session is already created for ongoing batch, WorkoutService must've started after restart
            // Don't create any new sessions
            return;
        }

        // TODO: Make sure that this resume is called after batch is created

        // 1. Create a session object
        // (provide a name, identifier, description and start time)

        Session session = new Session.Builder()
                .setName(currentSessionName)
                .setIdentifier(currentSessionIdentifier)
                .setDescription("Running session on " + DateUtil.getDate().toString())
                .setStartTime(System.currentTimeMillis() - 6000, TimeUnit.MILLISECONDS)
                .setActivity(FitnessActivities.RUNNING_JOGGING)
                .build();


        // 2. Invoke the Sessions API with:
        // - The Google API client object
        // - The request object
        PendingResult<Status> pendingResult =
                Fitness.SessionsApi.startSession(helper.getGoogleApiClient(), session);
        pendingResult.setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                if (status.isSuccess()){
                    Logger.d(TAG, "Successfully started session " + currentSessionIdentifier);
                    sessionIds.add(currentSessionIdentifier);
                    SharedPrefsManager.getInstance().setCollection(Constants.PREF_FITNESS_SESSION_IDS,
                            sessionIds);
                }else {
                    Logger.d(TAG, "Problem while starting session: " + status.getStatusMessage());
                }
            }
        });

    }
}
