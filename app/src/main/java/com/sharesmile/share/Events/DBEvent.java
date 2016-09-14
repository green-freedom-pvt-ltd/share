package com.sharesmile.share.Events;

import com.sharesmile.share.rfac.models.CauseList;

/**
 * Created by Shine on 03/06/16.
 */
public class DBEvent {

    public static class CauseDataUpdated {

        private final CauseList mCauseList;

        public CauseDataUpdated(CauseList causeList) {
            mCauseList = causeList;
        }

        public CauseList getCauseList() {
            return mCauseList;
        }
    }

    public static class CauseFetchDataFromDb {
        public CauseFetchDataFromDb(){}
    }

    public static class RunDataUpdated {
        public RunDataUpdated(){}
    }

    public static class MessageDataUpdated {
        public MessageDataUpdated(){}
    }

    public static class LeaderBoardDataUpdated{
        public LeaderBoardDataUpdated(){}
    }
}
