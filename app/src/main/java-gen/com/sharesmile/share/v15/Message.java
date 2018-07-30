package com.sharesmile.share.v15;

// THIS CODE IS GENERATED BY greenDAO, EDIT ONLY INSIDE THE "KEEP"-SECTIONS

// KEEP INCLUDES - put your custom includes here
// KEEP INCLUDES END
/**
 * Entity mapped to table "MESSAGE".
 */
public class Message {

    private Long id;
    /** Not-null value. */
    private String message_image;
    private String message_title;
    private String messageBrief;
    /** Not-null value. */
    private String message_description;
    /** Not-null value. */
    private String message_date;
    /** Not-null value. */
    private String shareTemplate;
    /** Not-null value. */
    private String videoId;
    private Boolean is_read;

    // KEEP FIELDS - put your custom fields here
    // KEEP FIELDS END

    public Message() {
    }

    public Message(Long id) {
        this.id = id;
    }

    public Message(Long id, String message_image, String message_title, String messageBrief, String message_description, String message_date, String shareTemplate, String videoId, Boolean is_read) {
        this.id = id;
        this.message_image = message_image;
        this.message_title = message_title;
        this.messageBrief = messageBrief;
        this.message_description = message_description;
        this.message_date = message_date;
        this.shareTemplate = shareTemplate;
        this.videoId = videoId;
        this.is_read = is_read;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /** Not-null value. */
    public String getMessage_image() {
        return message_image;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setMessage_image(String message_image) {
        this.message_image = message_image;
    }

    public String getMessage_title() {
        return message_title;
    }

    public void setMessage_title(String message_title) {
        this.message_title = message_title;
    }

    public String getMessageBrief() {
        return messageBrief;
    }

    public void setMessageBrief(String messageBrief) {
        this.messageBrief = messageBrief;
    }

    /** Not-null value. */
    public String getMessage_description() {
        return message_description;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setMessage_description(String message_description) {
        this.message_description = message_description;
    }

    /** Not-null value. */
    public String getMessage_date() {
        return message_date;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setMessage_date(String message_date) {
        this.message_date = message_date;
    }

    /** Not-null value. */
    public String getShareTemplate() {
        return shareTemplate;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setShareTemplate(String shareTemplate) {
        this.shareTemplate = shareTemplate;
    }

    /** Not-null value. */
    public String getVideoId() {
        return videoId;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public Boolean getIs_read() {
        return is_read;
    }

    public void setIs_read(Boolean is_read) {
        this.is_read = is_read;
    }

    // KEEP METHODS - put your custom methods here
    // KEEP METHODS END

}
