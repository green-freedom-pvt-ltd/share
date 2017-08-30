package com.sharesmile.share.rfac.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ankitmaheshwari on 6/26/17.
 */

public class UserFeedback {

    @SerializedName("user_id")
    private int userId;
    @SerializedName("email")
    private String email;
    @SerializedName("phone_number")
    private String phoneNumber;
    @SerializedName("run_id")
    private int runId;
    @SerializedName("client_run_id")
    private String clientRunId;
    @SerializedName("tag")
    private String tag;
    @SerializedName("message")
    private String message;
    @SerializedName("app_version")
    private String appVersion;
    @SerializedName("sub_tag")
    private String subTag;
    @SerializedName("is_chat")
    private boolean isChat;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public int getRunId() {
        return runId;
    }

    public void setRunId(int runId) {
        this.runId = runId;
    }

    public String getClientRunId() {
        return clientRunId;
    }

    public void setClientRunId(String clientRunId) {
        this.clientRunId = clientRunId;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getSubTag() {
        return subTag;
    }

    public void setSubTag(String subTag) {
        this.subTag = subTag;
    }

    public boolean isChat() {
        return isChat;
    }

    public void setChat(boolean chat) {
        isChat = chat;
    }

    public static class Builder {

        private UserFeedback feedback;

        public Builder(){
            feedback = new UserFeedback();
        }

        public Builder userId(int userId){
            feedback.userId = userId;
            return this;
        }

        public Builder email(String email){
            feedback.email = email;
            return this;
        }

        public Builder phoneNumber(String phoneNumber){
            feedback.phoneNumber = phoneNumber;
            return this;
        }

        public Builder runId(int runId){
            feedback.runId = runId;
            return this;
        }

        public Builder clientRunId(String clientRunId){
            feedback.clientRunId = clientRunId;
            return this;
        }

        public Builder tag(String tag){
            feedback.tag = tag;
            return this;
        }

        public Builder message(String message){
            feedback.message = message;
            return this;
        }

        public Builder appVersion(String appVersion){
            feedback.appVersion = appVersion;
            return this;
        }

        public Builder subTag(String subTag){
            feedback.subTag = subTag;
            return this;
        }

        public Builder chat(boolean chat){
            feedback.isChat = chat;
            return this;
        }

        public String getMessage(){
            return feedback.getMessage();
        }

        public UserFeedback build(){
            return feedback;
        }

    }
}
