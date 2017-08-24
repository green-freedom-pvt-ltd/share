package com.sharesmile.share.gps.models;

import android.location.Location;
import android.os.Parcelable;

import com.sharesmile.share.core.UnObfuscable;

import java.util.List;

/**
 * Created by ankitm on 25/03/16.
 */
public interface WorkoutBatch extends UnObfuscable, Parcelable{

	/**
	 * attribute given record to this batch
	 * @param record which is to be attributed to this batch
	 */
	void addRecord(DistRecord record);

	/**
	 * @return distance covered in this batch
	 */
	float getDistance();

	/**
	 * adds distance in this batch
	 */
	void addDistance(float distanceToAdd);

	/**
	 * sets the start point for this batch
	 * @param location
	 */
	void setStartPoint(Location location);

	/**
	 * @return the epoch (in millis) at which this batch began
	 */
	long getStartTimeStamp();

	/**
	 * @return the epoch (in millis) at which the last point of this batch was recorded
	 */
	long getLastRecordedTimeStamp();

	/**
	 * @return elapsed time, till the batch is running, since the beginning of this batch in secs;
	 * once the batch completes it returns the total time for which the batch was running
	 */
	float getElapsedTime();

	/**
	 * @return time interval in secs for which distance has been recorded
	 */
	float getRecordedTime();

	/**
	 * @return list of all points of this batch
	 */
	List<WorkoutPoint> getPoints();

	/**
	 *completes this batch and returns this after whatever post processing is required
	 */
	WorkoutBatch end();

	/**
	 * Creates a Deep copy of this batch
	 */
	WorkoutBatch copy();
}
