package com.sharesmile.share.leaderboard;

import com.sharesmile.share.LeaderBoard;

/**
 * Created by ankitmaheshwari on 9/27/17.
 */

public class LeaderBoardBannerItem implements LeaderBoardItem{

    private LeaderBoard leaderBoard;

    public LeaderBoard getLeaderBoard() {
        return leaderBoard;
    }

    public void setLeaderBoard(LeaderBoard leaderBoard) {
        this.leaderBoard = leaderBoard;
    }

    @Override
    public int getType() {
        return BANNER_HEADER;
    }
}
