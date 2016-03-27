package com.sharesmile.share.gps.models;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;
import com.sharesmile.share.utils.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ankitm on 25/03/16.
 */
public class WorkoutDataImpl implements WorkoutData, Parcelable {

	private static final String TAG = "WorkoutDataImpl";

	private float distance; // in m
	private float recordedTime; // in secs
	private float elapsedTime; // in secs
	private int totalSteps;
	private long beginTimeStamp;
	private boolean isActive;
	private boolean paused;
	private List<WorkoutBatchImpl> batches;

	/**
	 * Constructor to be used for creating a new WorkoutData object
	 * @param beginTimeStamp
	 */
	public WorkoutDataImpl(long beginTimeStamp) {
		isActive = true;
		paused = false;
		batches = new ArrayList<>();
		this.beginTimeStamp = beginTimeStamp;
		invokeNewBatch(beginTimeStamp);
	}

	private void invokeNewBatch(long startTimeStamp){
		WorkoutBatchImpl newbatch = new WorkoutBatchImpl(startTimeStamp);
		batches.add(newbatch);
	}

	@Override
	public float getDistance() {
		return distance;
	}

	@Override
	public float getRecordedTime() {
		return recordedTime;
	}

	@Override
	public float getElapsedTime(){
		setElapsedTime();
		return elapsedTime;
	}

	private void setElapsedTime(){
		if (isActive){
			elapsedTime = 0;
			for (WorkoutBatch batch : getBatches()){
				elapsedTime += batch.getElapsedTime();
			}
		}
	}

	@Override
	public float getAvgSpeed() {
		if (getRecordedTime() > 0){
			return (distance / getRecordedTime());
		}else
			return 0;
	}

	@Override
	public int getTotalSteps() {
		return totalSteps;
	}


	@Override
	public List<WorkoutBatchImpl> getBatches() {
		return batches;
	}

	@Override
	public int getCurrentBatchIndex() {
		return getBatches().size() - 1;
	}

	@Override
	public boolean isPaused() {
		return paused;
	}

	@Override
	public boolean isRunning() {
		return isActive && !paused;
	}

	@Override
	public WorkoutBatch getCurrentBatch(){
		return getBatches().get(getCurrentBatchIndex());
	}

	@Override
	public void workoutPause() {
		if (!isPaused()){
			getCurrentBatch().end();
			paused = true;
		}
	}

	@Override
	public void workoutResume() {
		if (isPaused()){
			invokeNewBatch(System.currentTimeMillis());
			paused = false;
		}
	}

	@Override
	public boolean coldStartAfterResume() {
		if (!isPaused() && getCurrentBatch().getPoints().size() == 0){
			return true;
		}
		return false;
	}

	@Override
	public List<LatLng> getPoints() {
		List<LatLng> points = new ArrayList<>();
		for (WorkoutBatch batch : getBatches()){
			points.addAll(batch.getPoints());
		}
		return points;
	}

	@Override
	public void addRecord(DistRecord record){
		if (!isRunning()){
			Logger.i(TAG, "Won't add add record as user is not running");
			return;
		}
		if (record.isStartRecord()){
			//Just set the start point if it is "first cold started" record after start/resume of workout
			getCurrentBatch().setStartPoint(record.getLocation());
		}else{
			// Add record over here
			getCurrentBatch().addRecord(record);
			this.distance += record.getDist();
			recordedTime =
					((float) (record.getLocation().getTime() - getCurrentBatch().getStartTimeStamp())) / 1000;
		}
	}

	@Override
	public void addSteps(int numSteps){
		if (!isPaused()){
			totalSteps = totalSteps + numSteps;
		}
	}

	@Override
	public void addDistance(float distanceToAdd) {
		if (!isPaused()){
			getCurrentBatch().addDistance(distanceToAdd);
			this.distance += distanceToAdd;
		}
	}


	@Override
	public long getBeginTimeStamp() {
		return beginTimeStamp;
	}

	@Override
	public synchronized WorkoutData close(){
		if (!isPaused()){
			workoutPause();
		}
		setElapsedTime();
		isActive = false;
		return this;
	}

	@Override
	public String toString() {
		return "WorkoutDataImpl{" +
				"distance=" + distance +
				", recordedTime=" + recordedTime +
				", elapsedTime=" + elapsedTime +
				", totalSteps=" + totalSteps +
				", isActive=" + isActive +
				", paused=" + paused +
				", beginTimeStamp=" + beginTimeStamp +
				", batches=" + batches +
				'}';
	}

	protected WorkoutDataImpl(Parcel in) {
		distance = in.readFloat();
		recordedTime = in.readFloat();
		elapsedTime = in.readFloat();
		totalSteps = in.readInt();
		beginTimeStamp = in.readLong();
		isActive = in.readByte() != 0x00;
		paused = in.readByte() != 0x00;
		if (in.readByte() == 0x01) {
			batches = new ArrayList<>();
			in.readList(batches, WorkoutBatchImpl.class.getClassLoader());
		} else {
			batches = null;
		}
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeFloat(distance);
		dest.writeFloat(recordedTime);
		dest.writeFloat(elapsedTime);
		dest.writeInt(totalSteps);
		dest.writeLong(beginTimeStamp);
		dest.writeByte((byte) (isActive ? 0x01 : 0x00));
		dest.writeByte((byte) (paused ? 0x01 : 0x00));
		if (batches == null) {
			dest.writeByte((byte) (0x00));
		} else {
			dest.writeByte((byte) (0x01));
			dest.writeList(batches);
		}
	}

	@SuppressWarnings("unused")
	public static final Parcelable.Creator<WorkoutDataImpl> CREATOR = new Parcelable.Creator<WorkoutDataImpl>() {
		@Override
		public WorkoutDataImpl createFromParcel(Parcel in) {
			return new WorkoutDataImpl(in);
		}

		@Override
		public WorkoutDataImpl[] newArray(int size) {
			return new WorkoutDataImpl[size];
		}
	};
}