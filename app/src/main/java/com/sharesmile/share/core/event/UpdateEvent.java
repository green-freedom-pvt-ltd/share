package com.sharesmile.share.core.event;

import android.content.Intent;

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
        public int result;
        public BadgeUpdated(int result)
        {
            this.result = result;
        }
    }

    public static class EditImagePermissionGranted{
        private int requestCode;

        public int getRequestCode() {
            return requestCode;
        }

        public void setRequestCode(int requestCode) {
            this.requestCode = requestCode;
        }

        public EditImagePermissionGranted(int requestCode)
        {
            setRequestCode(requestCode);
        }
    }

    public static class ImageCapture{
        private int resultCode;
        private int requestCode;

        public int getRequestCode() {
            return requestCode;
        }

        public void setRequestCode(int requestCode) {
            this.requestCode = requestCode;
        }

        public Intent getData() {
            return data;
        }

        public void setData(Intent data) {
            this.data = data;
        }

        private Intent data;

        public int getResultCode() {
            return resultCode;
        }

        public void setResultCode(int resultCode) {
            this.resultCode = resultCode;
        }

        public ImageCapture(int requestCode, int resultCode, Intent data)
        {
            setResultCode(resultCode);
            setRequestCode(requestCode);
            setData(data);
        }
    }

    public static class CharityOverviewUpdated{
        public CharityOverviewUpdated() {
        }
    }

    public static class OnGetStreak{
        public int result;
        public OnGetStreak(int result) {
            this.result = result;
        }
    }

    public static class OnGetCause{
        public int result;
        public OnGetCause(int result) {
            this.result = result;
        }
    }

    public static class OnGetAchivement{
        public int result;
        public OnGetAchivement(int result) {
            this.result = result;
        }
    }
    public static class OnGetTitle{
        public int result;
        public OnGetTitle(int result) {
            this.result = result;
        }
    }
    public static class LoadAchivedBadges{
        public LoadAchivedBadges() {
        }
    }
    public static class OnCharityLoad{
        public OnCharityLoad() {
        }
    }
}
