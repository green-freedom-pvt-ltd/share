package com.sharesmile.share.profile.badges.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.sharesmile.share.core.Constants;
import com.sharesmile.share.core.application.MainApplication;
import com.sharesmile.share.utils.Utils;

public class AchievedBadgesData implements Parcelable{
    private long changeMakerBadgeAchieved;
    private long causeBadgeAchieved;
    private long streakBadgeAchieved;
    private long marathonBadgeAchieved;

    protected AchievedBadgesData(Parcel in) {
        changeMakerBadgeAchieved = in.readLong();
        causeBadgeAchieved = in.readLong();
        streakBadgeAchieved = in.readLong();
        marathonBadgeAchieved = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(changeMakerBadgeAchieved);
        dest.writeLong(causeBadgeAchieved);
        dest.writeLong(streakBadgeAchieved);
        dest.writeLong(marathonBadgeAchieved);
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

    public long getChangeMakerBadgeAchieved() {
        return changeMakerBadgeAchieved;
    }

    public void setChangeMakerBadgeAchieved(long changeMakerBadgeAchieved) {
        this.changeMakerBadgeAchieved = changeMakerBadgeAchieved;
    }

    public long getCauseBadgeAchieved() {
        return causeBadgeAchieved;
    }

    public void setCauseBadgeAchieved(long causeBadgeAchieved) {
        this.causeBadgeAchieved = causeBadgeAchieved;
    }

    public long getStreakBadgeAchieved() {
        return streakBadgeAchieved;
    }

    public void setStreakBadgeAchieved(long streakBadgeAchieved) {
        this.streakBadgeAchieved = streakBadgeAchieved;
    }

    public long getMarathonBadgeAchieved() {
        return marathonBadgeAchieved;
    }

    public void setMarathonBadgeAchieved(long marathonBadgeAchieved) {
        this.marathonBadgeAchieved = marathonBadgeAchieved;
    }

    public AchievedBadgesData()
    {

    }

}
