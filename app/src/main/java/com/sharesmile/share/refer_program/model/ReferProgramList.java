package com.sharesmile.share.refer_program.model;

import com.google.gson.annotations.SerializedName;
import com.sharesmile.share.core.base.UnObfuscable;
import com.sharesmile.share.leaderboard.referprogram.model.ReferProgramBoard;

import java.util.List;

public class ReferProgramList implements UnObfuscable{
    @SerializedName("results")
    List<ReferProgramBoard> referProgramBoardList;

    public List<ReferProgramBoard> getReferProgramBoardList() {
        return referProgramBoardList;
    }

    public void setReferProgramBoardList(List<ReferProgramBoard> referProgramBoardList) {
        this.referProgramBoardList = referProgramBoardList;
    }
}
