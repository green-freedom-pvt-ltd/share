package com.sharesmile.share.gps.models;

import android.content.Context;
import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.sharesmile.share.MainApplication;
import com.sharesmile.share.gps.WorkoutSingleton;
import com.sharesmile.share.utils.DateUtil;
import com.sharesmile.share.utils.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ankitm on 25/03/16.
 */
public class WorkoutBatchImpl implements WorkoutBatch {

	private static final String TAG = "WorkoutBatchImpl";

	private float distance; // in m
	private long startTimeStamp;// in millis
	private long endTimeStamp; // in millis
	private String locationDataFileName; // Name of the file in app's storage where all the location data of this batch is stored
	private boolean isRunning;
	private float elapsedTime; // in secs
	private long lastRecordAddedTs; // in millis
	private boolean wasInVehicle;

	public WorkoutBatchImpl(long startTimeStamp, String locationDataFileName){
		isRunning = true;
		this.startTimeStamp = startTimeStamp;
		lastRecordAddedTs = startTimeStamp;
		this.locationDataFileName = locationDataFileName;
	}

	private WorkoutBatchImpl(WorkoutBatchImpl source){
		distance = source.distance;
		startTimeStamp = source.startTimeStamp;
		endTimeStamp = source.endTimeStamp;
		locationDataFileName = source.locationDataFileName;
		isRunning = source.isRunning;
		elapsedTime = source.getElapsedTime();
		lastRecordAddedTs = source.lastRecordAddedTs;
		wasInVehicle = source.wasInVehicle;
	}

	@Override
	public void addRecord(DistRecord record, boolean persistPoints) {

		addDistance(record.getDist());

		/*
			If persistPoints is true instead of keeping an array of WorkoutPoints in memory
			convert WorkoutPoint into JSON string and write it off in a file

			This file will keep on increasing in size as and when points are added into it
			Every batch will have a separate file
			When the time comes to upload the location data all the batch files for given client_run_id will be
			posted on server as WorkoutBatchLocationData

		 */

		if (persistPoints){
			Location loc = record.getLocation();
			Location prevLocation = record.getPrevLocation();

			WorkoutPoint point = new WorkoutPoint();
			if (record.getDist() == 0 && prevLocation != null && prevLocation.distanceTo(loc) > 0){
				// The points were separated but dist was forcefully set as Zero
				point.setFlagged(true);
			}
			if (loc.hasAccuracy()){
				point.setAccuracy(loc.getAccuracy());
			}else {
				point.setAccuracy(-1);
			}
			if (loc.hasAltitude()){
				point.setAltitude(loc.getAltitude());
			}else {
				point.setAltitude(-1);
			}
			if (loc.hasBearing()){
				point.setBearing(loc.getBearing());
			}else {
				point.setBearing(-1);
			}
			if (loc.hasSpeed()){
				point.setGpsSpeed(loc.getSpeed());
			}else {
				point.setGpsSpeed(-1);
			}
			point.setLatitude(loc.getLatitude());
			point.setLongitude(loc.getLongitude());
			point.setTimeStamp(loc.getTime());
			point.setCumulativeDistance(WorkoutSingleton.getInstance().getTotalDistanceInMeters());
			point.setCumulativeStepCount(WorkoutSingleton.getInstance().getTotalSteps());
			point.setCumulativeNumSpikes(WorkoutSingleton.getInstance().getDataStore().getNumGpsSpikes());

			writePointToFile(point);
		}

		lastRecordAddedTs = DateUtil.getServerTimeInMillis();
	}

	private void writePointToFile(WorkoutPoint point){
		Gson gson = new Gson();
		try {
			OutputStreamWriter outputStreamWriter =
					new OutputStreamWriter(MainApplication.getContext()
							.openFileOutput(locationDataFileName, Context.MODE_APPEND));
			String text = gson.toJson(point);
			outputStreamWriter.append(text);
			outputStreamWriter.append("\n");
			outputStreamWriter.close();
		}
		catch (IOException e) {
			String message = "Exception occurred while writing WorkoutPoint to file: " + locationDataFileName
					+ ", exception: " + e.getMessage();
			Logger.e(TAG, message);
			e.printStackTrace();
			Logger.e(TAG, "Cant write WorkoutPoint: " + gson.toJson(point));
			Crashlytics.log(message);
			Crashlytics.logException(e);
		}
	}

	private List<WorkoutPoint> getPointsInFile(){
		List<WorkoutPoint> points = new ArrayList<>();
		Gson gson = new Gson();

		try {
			// open the file for reading
			InputStream inputStream = MainApplication.getContext().openFileInput(locationDataFileName);

			// if file the available for reading
			if (inputStream != null) {
				// prepare the file for reading
				InputStreamReader inputreader = new InputStreamReader(inputStream);
				BufferedReader buffreader = new BufferedReader(inputreader);

				String line;

				// Read every line of the file into the line-variable, one line at a time
				while ( (line = buffreader.readLine()) != null ) {
					// Parse the line as a WorkoutPoint and add it to the list
					WorkoutPoint point = gson.fromJson(line, WorkoutPoint.class);
					points.add(point);
				}
				inputStream.close();
			}else {
				throw new IllegalStateException("InputStream null for file: " + locationDataFileName);
			}
		} catch (Exception ex) {
			String message = "Exception occurred while reading WorkoutPoint from file: " + locationDataFileName
					+ ", exception: " + ex.getMessage();
			Logger.e(TAG, message);
			ex.printStackTrace();
			Crashlytics.log(message);
			Crashlytics.logException(ex);
		} finally {
			return points;
		}
	}

	@Override
	public float getDistance() {
		return distance;
	}

	@Override
	public void addDistance(float distanceToAdd) {
		distance += distanceToAdd;
	}

	@Override
	public void setStartPoint(Location loc, boolean persistPoints) {
		if (persistPoints){
			WorkoutPoint point = new WorkoutPoint();
			if (loc.hasAccuracy()){
				point.setAccuracy(loc.getAccuracy());
			}else {
				point.setAccuracy(-1);
			}
			if (loc.hasAltitude()){
				point.setAltitude(loc.getAltitude());
			}else {
				point.setAltitude(-1);
			}
			if (loc.hasBearing()){
				point.setBearing(loc.getBearing());
			}else {
				point.setBearing(-1);
			}
			if (loc.hasSpeed()){
				point.setGpsSpeed(loc.getSpeed());
			}else {
				point.setGpsSpeed(-1);
			}
			point.setLatitude(loc.getLatitude());
			point.setLongitude(loc.getLongitude());
			point.setTimeStamp(loc.getTime());
			point.setCumulativeDistance(WorkoutSingleton.getInstance().getTotalDistanceInMeters());
			point.setCumulativeStepCount(WorkoutSingleton.getInstance().getTotalSteps());
			point.setCumulativeNumSpikes(WorkoutSingleton.getInstance().getDataStore().getNumGpsSpikes());
			writePointToFile(point);
		}
		lastRecordAddedTs = DateUtil.getServerTimeInMillis();
	}

	@Override
	public long getStartTimeStamp() {
		return startTimeStamp;
	}

	@Override
	public long getEndTimeStamp() {
		return endTimeStamp;
	}

	@Override
	public String getLocationDataFileName() {
		return locationDataFileName;
	}

	@Override
	public long getLastRecordedTimeStamp() {
		return lastRecordAddedTs;
	}

	@Override
	public float getElapsedTime(){
		setElapsedTime();
		return elapsedTime;
	}

	@Override
	public float getRecordedTime() {
		return (lastRecordAddedTs - startTimeStamp) / 1000;
	}

	@Override
	public boolean wasInVehicle() {
		return wasInVehicle;
	}

	private void setElapsedTime(){
		if (isRunning){
			elapsedTime = (DateUtil.getServerTimeInMillis() - startTimeStamp) / 1000;
		}
	}

	@Override
	public List<WorkoutPoint> getPoints() {
		if (startTimeStamp == lastRecordAddedTs){
			// No record have been added yet, return empty list
			return new ArrayList<>();
		}else {
			return getPointsInFile();
		}
	}

	@Override
	public synchronized WorkoutBatch end(boolean wasInVehicle) {
		this.wasInVehicle = wasInVehicle;
		endTimeStamp = DateUtil.getServerTimeInMillis();
		setElapsedTime();
		isRunning = false;
		return this;
	}

	@Override
	public WorkoutBatch copy() {
		return new WorkoutBatchImpl(this);
	}

	@Override
	public String toString() {
		return "WorkoutBatchImpl{" +
				"distance=" + distance +
				", startTimeStamp=" + startTimeStamp +
				", workoutId=" + locationDataFileName +
				", isRunning=" + isRunning +
				", elapsedTime=" + elapsedTime +
				", lastRecordAddedTs=" + lastRecordAddedTs +
				'}';
	}

	protected WorkoutBatchImpl(Parcel in) {
		distance = in.readFloat();
		startTimeStamp = in.readLong();
		endTimeStamp = in.readLong();
		locationDataFileName = in.readString();
		isRunning = in.readByte() != 0x00;
		elapsedTime = in.readFloat();
		lastRecordAddedTs = in.readLong();
		wasInVehicle = in.readByte() != 0x00;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeFloat(distance);
		dest.writeLong(startTimeStamp);
		dest.writeLong(endTimeStamp);
		dest.writeString(locationDataFileName);
		dest.writeByte((byte) (isRunning ? 0x01 : 0x00));
		dest.writeFloat(elapsedTime);
		dest.writeLong(lastRecordAddedTs);
		dest.writeByte((byte) (wasInVehicle ? 0x01 : 0x00));
	}

	@SuppressWarnings("unused")
	public static final Parcelable.Creator<WorkoutBatchImpl> CREATOR = new Parcelable.Creator<WorkoutBatchImpl>() {
		@Override
		public WorkoutBatchImpl createFromParcel(Parcel in) {
			return new WorkoutBatchImpl(in);
		}

		@Override
		public WorkoutBatchImpl[] newArray(int size) {
			return new WorkoutBatchImpl[size];
		}
	};


}
