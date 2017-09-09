package com.sharesmile.share.gps.models;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import com.sharesmile.share.gps.WorkoutSingleton;
import com.sharesmile.share.utils.DateUtil;

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
	private List<WorkoutPoint> points;
	private boolean isRunning;
	private float elapsedTime; // in secs
	private long lastRecordAddedTs; // in millis

	public WorkoutBatchImpl(long startTimeStamp){
		isRunning = true;
		this.startTimeStamp = startTimeStamp;
		points = new ArrayList<>();
		lastRecordAddedTs = startTimeStamp;
	}

	private WorkoutBatchImpl(WorkoutBatchImpl source){
		distance = source.distance;
		startTimeStamp = source.startTimeStamp;
		endTimeStamp = source.endTimeStamp;
		isRunning = source.isRunning;
		elapsedTime = source.getElapsedTime();
		points = new ArrayList<>();
		if (source.points != null){
			for (int i = 0; i < source.points.size(); i++){
				WorkoutPoint point = source.points.get(i);
				points.add(i, new WorkoutPoint(point));
			}
		}
		lastRecordAddedTs = source.lastRecordAddedTs;
	}

	@Override
	public void addRecord(DistRecord record) {
		/*
			TODO: Instead of keeping an array of WorkoutPoints in memory convert WorkoutPoint into JSON string and write it off in a file
			This file will keep on increasing in size as and when points are added into it
			Every batch will have a separate file
			When the time comes to upload the location data all the batch files for given client_run_id will be
			 posted on server as WorkoutBatchLocationData

		 */
		addDistance(record.getDist());
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
		points.add(point);
		lastRecordAddedTs = DateUtil.getServerTimeInMillis();
	}

	@Override
	public float getDistance() {
		return distance;
	}

	@Override
	public void addDistance(float distanceToAdd) {
		distance += distanceToAdd;
		lastRecordAddedTs = DateUtil.getServerTimeInMillis();
	}

	@Override
	public void setStartPoint(Location loc) {
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
		points.add(0, point);
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

	private void setElapsedTime(){
		if (isRunning){
			elapsedTime = (DateUtil.getServerTimeInMillis() - startTimeStamp) / 1000;
		}
	}

	@Override
	public List<WorkoutPoint> getPoints() {
		return points;
	}

	@Override
	public synchronized WorkoutBatch end() {
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
				", points=" + points +
				", isRunning=" + isRunning +
				", elapsedTime=" + elapsedTime +
				", lastRecordAddedTs=" + lastRecordAddedTs +
				'}';
	}

	protected WorkoutBatchImpl(Parcel in) {
		distance = in.readFloat();
		startTimeStamp = in.readLong();
		endTimeStamp = in.readLong();
		if (in.readByte() == 0x01) {
			points = new ArrayList<WorkoutPoint>();
			in.readList(points, WorkoutPoint.class.getClassLoader());
		} else {
			points = null;
		}
		isRunning = in.readByte() != 0x00;
		elapsedTime = in.readFloat();
		lastRecordAddedTs = in.readLong();
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
		if (points == null) {
			dest.writeByte((byte) (0x00));
		} else {
			dest.writeByte((byte) (0x01));
			dest.writeList(points);
		}
		dest.writeByte((byte) (isRunning ? 0x01 : 0x00));
		dest.writeFloat(elapsedTime);
		dest.writeLong(lastRecordAddedTs);
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
