package com.sharesmile.share.gps.models;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;
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
	private List<LatLng> points;
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
		isRunning = source.isRunning;
		elapsedTime = source.elapsedTime;
		points = new ArrayList<>();
		for (LatLng latLong : points){
			points.add(new LatLng(latLong.latitude, latLong.longitude));
		}
		lastRecordAddedTs = source.lastRecordAddedTs;
	}

	@Override
	public void addRecord(DistRecord record) {
		addDistance(record.getDist());
		points.add(new LatLng(record.getLocation().getLatitude(), record.getLocation().getLongitude()));
		lastRecordAddedTs = record.getLocation().getTime();
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
	public void setStartPoint(Location location) {
		points.add(0, new LatLng(location.getLatitude(), location.getLongitude()));
	}

	@Override
	public long getStartTimeStamp() {
		return startTimeStamp;
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
	public List<LatLng> getPoints() {
		return points;
	}

	@Override
	public synchronized WorkoutBatch end() {
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
		if (in.readByte() == 0x01) {
			points = new ArrayList<LatLng>();
			in.readList(points, LatLng.class.getClassLoader());
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
