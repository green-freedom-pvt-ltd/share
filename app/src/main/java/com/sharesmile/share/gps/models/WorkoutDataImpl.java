package com.sharesmile.share.gps.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;
import com.sharesmile.share.MainApplication;
import com.sharesmile.share.analytics.events.Properties;
import com.sharesmile.share.utils.DateUtil;
import com.sharesmile.share.utils.Logger;
import com.sharesmile.share.utils.Utils;

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
	private LatLng startPoint;
	private String workoutId;
	private LatLng latestPoint;
	private Calorie calories;
	private int usainBoltCount;
	private boolean mockLocationDetected;
	private int numGpsSpikes;
	private int numUpdateEvents;

	private List<WorkoutBatchImpl> batches;

	/**
	 * Constructor to be used for creating a new WorkoutData object
	 * @param beginTimeStamp
	 */
	public WorkoutDataImpl(long beginTimeStamp, String workoutId) {
		isActive = true;
		paused = false;
		batches = new ArrayList<>();
		this.beginTimeStamp = beginTimeStamp;
		invokeNewBatch(beginTimeStamp, workoutId);
		this.workoutId = workoutId;
		this.calories = new Calorie(0,0);
		this.usainBoltCount = 0;
		this.mockLocationDetected = false;
	}

	private WorkoutDataImpl(WorkoutDataImpl source){
		distance = source.distance;
		recordedTime = source.getRecordedTime();
		elapsedTime = source.getElapsedTime();
		totalSteps = source.totalSteps;
		beginTimeStamp = source.beginTimeStamp;
		isActive = source.isActive;
		paused = source.paused;
		this.startPoint = (source.startPoint != null) ? new LatLng(source.startPoint.latitude, source.startPoint.longitude) : null;
		this.workoutId = source.workoutId;
		this.latestPoint = (source.latestPoint != null) ? new LatLng(source.latestPoint.latitude, source.latestPoint.longitude) : null;
		batches = new ArrayList<>();
		if (source.batches != null){
			for (int i = 0; i < source.batches.size(); i++){
				batches.add(i, (WorkoutBatchImpl) source.batches.get(i).copy());
			}
		}
		calories = (source.calories != null) ? new Calorie(source.calories.getCalories(),
				source.calories.getCaloriesKarkanen()) : new Calorie(0,0);
		usainBoltCount = source.usainBoltCount;
		mockLocationDetected = source.mockLocationDetected;
		numGpsSpikes = source.numGpsSpikes;
		numUpdateEvents = source.numUpdateEvents;
	}

	private void invokeNewBatch(long startTimeStamp, String workoutId){
		int index = batches.size();
		WorkoutBatchImpl newbatch =
				new WorkoutBatchImpl(startTimeStamp, Utils.getLocationDataFileName(workoutId, index));
		batches.add(newbatch);
	}

	@Override
	public LatLng getStartPoint() {
		return startPoint;
	}

	@Override
	public LatLng getLatestPoint() {
		return latestPoint;
	}

	@Override
	public void setStartPoint(LatLng source) {
		this.startPoint = source;
	}

	@Override
	public String getWorkoutId() {
		return workoutId;
	}

	@Override
	public int getUsainBoltCount() {
		return usainBoltCount;
	}

	@Override
	public void incrementUsainBoltCounter() {
		usainBoltCount++;
	}

	@Override
	public void setMockLocationDetected(boolean detected) {
		mockLocationDetected = detected;
	}

	@Override
	public boolean isMockLocationDetected() {
		return mockLocationDetected;
	}

	@Override
	public float getDistance() {
		return distance;
	}

	@Override
	public float getRecordedTime() {
		setRecordedTime();
		return recordedTime;
	}

	@Override
	public float getElapsedTime(){
		setElapsedTime();
		return elapsedTime;
	}

	@Override
	public Calorie getCalories() {
		return calories;
	}

	@Override
	public int getNumGpsSpikes() {
		return numGpsSpikes;
	}

	@Override
	public void incrementGpsSpike() {
		numGpsSpikes++;
	}

	@Override
	public int getNumUpdateEvents() {
		return numUpdateEvents;
	}

	@Override
	public void incrementNumUpdates() {
		numUpdateEvents++;
	}

	@Override
	public void setCalories(Calorie calories) {
		this.calories = calories;
	}

	private void setRecordedTime(){
		if (isActive){
			float acc = 0;
			for (WorkoutBatch batch : getBatches()){
				acc += batch.getRecordedTime();
			}
			recordedTime = acc;
		}
	}

	private void setElapsedTime(){
		if (isActive){
			float acc = 0;
			for (WorkoutBatch batch : getBatches()){
				acc += batch.getElapsedTime();
			}
			elapsedTime = acc;
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
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
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
	public synchronized void workoutPause() {
		if (!isPaused()){
			getCurrentBatch().end();
			paused = true;
		}
	}

	@Override
	public synchronized void workoutResume() {
		if (isPaused()){
			invokeNewBatch(DateUtil.getServerTimeInMillis(), workoutId);
			paused = false;
		}
	}

	@Override
	public boolean coldStartAfterResume() {
		// TODO: Change this logic of coldStartAfterResume, as now only approved points are stored in a file
		// Size of points will be zero if none of the records is approved
		if (!isPaused()
				&& getCurrentBatch().getLastRecordedTimeStamp() == getCurrentBatch().getStartTimeStamp() ){
			return true;
		}
		return false;
	}

	@Override
	public List<LatLng> getPoints() {
		List<LatLng> points = new ArrayList<>();
		for (WorkoutBatch batch : getBatches()){
			List<LatLng> batchPoints = new ArrayList<>();
			for (WorkoutPoint workoutPoint : batch.getPoints()){
				batchPoints.add(new LatLng(workoutPoint.getLatitude(), workoutPoint.getLongitude()));
			}
			points.addAll(batchPoints);
		}
		return points;
	}

	@Override
	public synchronized void addRecord(DistRecord record, boolean persistPoints){
		if (!isRunning()){
			Logger.i(TAG, "Won't add add record as user is not running");
			return;
		}

		if (record.isFirstRecordAfterResume()){
			//Just set the start point if it is "first cold started" record after start/resume of workout
			getCurrentBatch().setStartPoint(record.getLocation(), persistPoints);
		}else{
			// Add record over here
			this.distance += record.getDist();
			getCurrentBatch().addRecord(record, persistPoints);
			if (MainApplication.getInstance().getBodyWeight() > 0){
				this.calories.incrementCaloriesMets(Utils.getDeltaCaloriesMets(record.getInterval(), record.getSpeed()));
				this.calories.incrementCaloriesKarkanen(Utils.getDeltaCaloriesKarkanen(record.getInterval(), record.getSpeed()));
			}
		}
		latestPoint = new LatLng(record.getLocation().getLatitude(), record.getLocation().getLongitude());
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
		setRecordedTime();
		isActive = false;
		return this;
	}

	@Override
	public synchronized WorkoutData copy() {
		return new WorkoutDataImpl(this);
	}

	@Override
	public Properties getWorkoutBundle() {
		Properties p = new Properties();
		p.put("distance", Utils.formatToKmsWithTwoDecimal(getDistance()));
		p.put("time_elapsed", getElapsedTime());
		p.put("avg_speed", getAvgSpeed()*(3.6f));
		p.put("num_steps", getTotalSteps());
		p.put("client_run_id", getWorkoutId());
		p.put("calories", getCalories().getCalories());
		return p;
	}

	public static WorkoutDataImpl getDummyWorkoutData(){
		WorkoutDataImpl workoutData = new WorkoutDataImpl(1498570023000L, "dwaidw-dwai-dwannu-dwa19n-2inhhb2");
		workoutData.distance = 4248;
		workoutData.elapsedTime = 1834;
		workoutData.calories = new Calorie(439, 389);
		workoutData.isActive = false;
		return workoutData;
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
				", startPoint=" + startPoint +
				", workoutId=" + workoutId +
				", latestPoint=" + latestPoint +
				", calories=" + calories +
				", usainBoltCount=" + usainBoltCount +
				", isMockLocationDetected=" + mockLocationDetected +
				", numGpsSpikes=" + numGpsSpikes +
				", numUpdateEvents=" + numUpdateEvents +
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
		startPoint = (LatLng) in.readValue(LatLng.class.getClassLoader());
		workoutId = in.readString();
		latestPoint = (LatLng) in.readValue(LatLng.class.getClassLoader());
		calories = (Calorie) in.readValue(Calorie.class.getClassLoader());
		usainBoltCount = in.readInt();
		mockLocationDetected = in.readByte() != 0x00;
		numGpsSpikes = in.readInt();
		numUpdateEvents = in.readInt();
		if (in.readByte() == 0x01) {
			batches = new ArrayList<WorkoutBatchImpl>();
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
		dest.writeValue(startPoint);
		dest.writeString(workoutId);
		dest.writeValue(latestPoint);
		dest.writeValue(calories);
		dest.writeInt(usainBoltCount);
		dest.writeByte((byte) (mockLocationDetected ? 0x01 : 0x00));
		dest.writeInt(numGpsSpikes);
		dest.writeInt(numUpdateEvents);
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