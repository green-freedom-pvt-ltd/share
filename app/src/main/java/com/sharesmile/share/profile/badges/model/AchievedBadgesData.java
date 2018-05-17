package com.sharesmile.share.profile.badges.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.sharesmile.share.core.Constants;
import com.sharesmile.share.core.application.MainApplication;
import com.sharesmile.share.utils.Utils;

public class AchievedBadgesData implements Parcelable{
    private boolean changeMakerBadgeAchieved;
    private boolean causeBadgeAchieved;

    public boolean isChangeMakerBadgeAchieved() {
        return changeMakerBadgeAchieved;
    }

    public void setChangeMakerBadgeAchieved(boolean changeMakerBadgeAchieved) {
        this.changeMakerBadgeAchieved = changeMakerBadgeAchieved;
    }

    public boolean isCauseBadgeAchieved() {
        return causeBadgeAchieved;
    }

    public void setCauseBadgeAchieved(boolean causeBadgeAchieved) {
        this.causeBadgeAchieved = causeBadgeAchieved;
    }

    public boolean isStreakBadgeAchieved() {
        return streakBadgeAchieved;
    }

    public void setStreakBadgeAchieved(boolean streakBadgeAchieved) {
        this.streakBadgeAchieved = streakBadgeAchieved;
    }

    public boolean isMarathonBadgeAchieved() {
        return marathonBadgeAchieved;
    }

    public void setMarathonBadgeAchieved(boolean marathonBadgeAchieved) {
        this.marathonBadgeAchieved = marathonBadgeAchieved;
    }

    private boolean streakBadgeAchieved;
    private boolean marathonBadgeAchieved;

    public AchievedBadgesData()
    {
        changeMakerBadgeAchieved = false;
        causeBadgeAchieved = false;
        streakBadgeAchieved = false;
        marathonBadgeAchieved = false;
    }

    protected AchievedBadgesData(Parcel in) {
        changeMakerBadgeAchieved = in.readByte() != 0;
        causeBadgeAchieved = in.readByte() != 0;
        streakBadgeAchieved = in.readByte() != 0;
        marathonBadgeAchieved = in.readByte() != 0;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeByte((byte) (changeMakerBadgeAchieved ? 1 : 0));
        parcel.writeByte((byte) (causeBadgeAchieved ? 1 : 0));
        parcel.writeByte((byte) (streakBadgeAchieved ? 1 : 0));
        parcel.writeByte((byte) (marathonBadgeAchieved ? 1 : 0));
    }
}
