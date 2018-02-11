package com.sharesmile.share.rfac.models;

import com.google.gson.annotations.SerializedName;
import com.sharesmile.share.core.base.UnObfuscable;

/**
 * Created by ankitmaheshwari on 5/25/17.
 */

public class FraudData implements UnObfuscable{

    @SerializedName("user_id")
    private int userId;

    @SerializedName("client_run_id")
    private String clientRunId;

    @SerializedName("cause_id")
    private int causeId;

    @SerializedName("usain_bolt_count")
    private int usainBoltCount;

    @SerializedName("team_id")
    private int teamId;

    @SerializedName("timestamp")
    private long timeStamp;

    @SerializedName("mock_location_used")
    private boolean mockLocationUsed;

    public FraudData() {
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getClientRunId() {
        return clientRunId;
    }

    public void setClientRunId(String clientRunId) {
        this.clientRunId = clientRunId;
    }

    public int getCauseId() {
        return causeId;
    }

    public void setCauseId(int causeId) {
        this.causeId = causeId;
    }

    public int getUsainBoltCount() {
        return usainBoltCount;
    }

    public void setUsainBoltCount(int usainBoltCount) {
        this.usainBoltCount = usainBoltCount;
    }

    public int getTeamId() {
        return teamId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public boolean isMockLocationUsed() {
        return mockLocationUsed;
    }

    public void setMockLocationUsed(boolean mockLocationUsed) {
        this.mockLocationUsed = mockLocationUsed;
    }

    @Override
    public String toString() {
        return "FraudData{" +
                "userId=" + userId +
                ", clientRunId='" + clientRunId + '\'' +
                ", causeId=" + causeId +
                ", usainBoltCount=" + usainBoltCount +
                ", teamId=" + teamId +
                ", timeStamp=" + timeStamp +
                ", mockLocationUsed=" + mockLocationUsed +
                '}';
    }

}
