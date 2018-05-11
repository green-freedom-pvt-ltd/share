package com.sharesmile.share.core.event;

import com.sharesmile.share.core.cause.model.CauseList;

import Models.CampaignList;

/**
 * Created by Shine on 03/06/16.
 */
public class UpdateEvent {

    public static class CauseDataUpdated {

        private final CauseList mCauseList;

        public CauseDataUpdated(CauseList causeList) {
            mCauseList = causeList;
        }

        public CauseList getCauseList() {
            return mCauseList;
        }
    }

    public static class FaqsUpdated{

        private boolean success;

        public FaqsUpdated(boolean success){
            this.success = success;
        }

        public boolean isSuccess() {
            return success;
        }
    }

    public static class RunDataUpdated {
        public RunDataUpdated() {
        }
    }

    public static class PendingWorkoutUploaded {
        public PendingWorkoutUploaded() {
        }
    }

    public static class MessageDataUpdated {
        public MessageDataUpdated() {
        }
    }


    public static class CampaignDataUpdated {
        private CampaignList.Campaign campaign;

        public CampaignDataUpdated(CampaignList.Campaign data) {
            campaign = data;
        }

        public CampaignList.Campaign getCampaign() {
            return campaign;
        }
    }

    public static class BadgeUpdated{
        public BadgeUpdated()
        {

        }
    }
}
