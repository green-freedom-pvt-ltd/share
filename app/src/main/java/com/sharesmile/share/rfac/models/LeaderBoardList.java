package com.sharesmile.share.rfac.models;

import com.google.gson.annotations.SerializedName;
import com.sharesmile.share.core.UnObfuscable;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Piyush on 9/8/16.
 */
public class LeaderBoardList implements UnObfuscable, Serializable {

    @SerializedName("results")
    List<LeaderBoardData> leaderBoardList;

    public List<LeaderBoardData> getLeaderBoardList() {
        return leaderBoardList;
    }

    public void setLeaderBoardList(List<LeaderBoardData> leaderBoardList) {
        this.leaderBoardList = leaderBoardList;
    }
}
