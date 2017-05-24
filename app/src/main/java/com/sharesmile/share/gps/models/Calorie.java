package com.sharesmile.share.gps.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ankitmaheshwari on 5/9/17.
 */

public class Calorie implements Parcelable {

    private double caloriesMets; // in Cals
    private double caloriesKarkanen; // in Cals

    public Calorie(double caloriesMets, double caloriesKarkanen){
        this.caloriesMets = caloriesMets;
        this.caloriesKarkanen = caloriesKarkanen;
    }

    /**
     * @return Calories burned calculated as per METS formula
     */
    public double getCalories() {
        return caloriesMets;
    }

    /**
     * @return Calories burned calculated as per Karkanen's patent
     */
    public double getCaloriesKarkanen() {
        return caloriesKarkanen;
    }

    public void incrementCaloriesMets(double delta){
        caloriesMets += delta;
    }

    public void incrementCaloriesKarkanen(double delta){
        caloriesKarkanen += delta;
    }

    @Override
    public String toString() {
        return "Calorie{" +
                "caloriesMets=" + caloriesMets +
                ", caloriesKarkanen=" + caloriesKarkanen +
                '}';
    }

    protected Calorie(Parcel in) {
        caloriesMets = in.readDouble();
        caloriesKarkanen = in.readDouble();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(caloriesMets);
        dest.writeDouble(caloriesKarkanen);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Calorie> CREATOR = new Parcelable.Creator<Calorie>() {
        @Override
        public Calorie createFromParcel(Parcel in) {
            return new Calorie(in);
        }

        @Override
        public Calorie[] newArray(int size) {
            return new Calorie[size];
        }
    };

}
