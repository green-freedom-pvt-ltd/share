package com.sharesmile.share.profile.badges.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.sharesmile.share.core.Constants;
import com.sharesmile.share.core.application.MainApplication;
import com.sharesmile.share.utils.Utils;

import java.util.ArrayList;

public class AchievedBadgesData implements Parcelable{
    private long changeMakerBadgeAchieved;
    private long causeBadgeAchieved;
    private long streakBadgeAchieved;
    private long marathonBadgeAchieved;
    private ArrayList<Long> titleIds;


    protected AchievedBadgesData(Parcel in) {
        changeMakerBadgeAchieved = in.readLong();
        causeBadgeAchieved = in.readLong();
        streakBadgeAchieved = in.readLong();
        marathonBadgeAchieved = in.readLong();
        int size = in.readInt();
        long[] titleIds = new long[size];
        in.readLongArray(titleIds);
        this.titleIds = toObjects(titleIds);
    }

    private ArrayList<Long> toObjects(long[] titleIds) {
        ArrayList<Long> longs = new ArrayList<>();
        for (long l:
             titleIds) {
            longs.add(l);
        }
        return longs;
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
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(changeMakerBadgeAchieved);
        dest.writeLong(causeBadgeAchieved);
        dest.writeLong(streakBadgeAchieved);
        dest.writeLong(marathonBadgeAchieved);
        dest.writeLong(titleIds.size());
        dest.writeLongArray(getArray(titleIds));
    }

    private long[] getArray(ArrayList<Long> titleIds) {
        long l[] = new long[titleIds.size()];
        for (int i=0;i<titleIds.size();i++) {
            l[i] = titleIds.get(i);
        }
        return l;
    }

    public AchievedBadgesData() {
        this.titleIds = new ArrayList<>();
    }

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

    public ArrayList<Long> getTitleIds() {
        return titleIds;
    }

    public void setTitleIds(ArrayList<Long> titleIds) {
        this.titleIds = titleIds;
    }
}
