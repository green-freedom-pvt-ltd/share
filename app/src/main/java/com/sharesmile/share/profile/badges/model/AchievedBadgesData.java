package com.sharesmile.share.profile.badges.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.sharesmile.share.core.Constants;
import com.sharesmile.share.core.application.MainApplication;
import com.sharesmile.share.utils.Utils;

public class AchievedBadgesData implements Parcelable{
    private int changeMakerBadgeAchieved;
    private int causeBadgeAchieved;
    private int streakBadgeAchieved;
    private int marathonBadgeAchieved;

    public int getChangeMakerBadgeAchieved() {
        return changeMakerBadgeAchieved;
    }

    public void setChangeMakerBadgeAchieved(int changeMakerBadgeAchieved) {
        this.changeMakerBadgeAchieved = changeMakerBadgeAchieved;
    }

    public int getCauseBadgeAchieved() {
        return causeBadgeAchieved;
    }

    public void setCauseBadgeAchieved(int causeBadgeAchieved) {
        this.causeBadgeAchieved = causeBadgeAchieved;
    }

    public int getStreakBadgeAchieved() {
        return streakBadgeAchieved;
    }

    public void setStreakBadgeAchieved(int streakBadgeAchieved) {
        this.streakBadgeAchieved = streakBadgeAchieved;
    }

    public int getMarathonBadgeAchieved() {
        return marathonBadgeAchieved;
    }

    public void setMarathonBadgeAchieved(int marathonBadgeAchieved) {
        this.marathonBadgeAchieved = marathonBadgeAchieved;
    }

    public AchievedBadgesData()
    {

    }
    protected AchievedBadgesData(Parcel in) {
        changeMakerBadgeAchieved = in.readInt();
        causeBadgeAchieved = in.readInt();
        streakBadgeAchieved = in.readInt();
        marathonBadgeAchieved = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(changeMakerBadgeAchieved);
        dest.writeInt(causeBadgeAchieved);
        dest.writeInt(streakBadgeAchieved);
        dest.writeInt(marathonBadgeAchieved);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<AchievedBadgesData> CREATOR = new Creator<AchievedBadgesData>() {
        @Override
        public AchievedBadgesData createFromParcel(Parcel in) {
            return new AchievedBadgesData(in);
        }

        @Override
        public AchievedBadgesData[] newArray(int size) {
            return new AchievedBadgesData[size];
        }
    };
}
