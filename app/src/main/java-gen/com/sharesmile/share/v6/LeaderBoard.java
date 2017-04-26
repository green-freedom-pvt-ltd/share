package com.sharesmile.share.v6;

// THIS CODE IS GENERATED BY greenDAO, EDIT ONLY INSIDE THE "KEEP"-SECTIONS

// KEEP INCLUDES - put your custom includes here
// KEEP INCLUDES END
/**
 * Entity mapped to table "LEADER_BOARD".
 */
public class LeaderBoard {

    private Long id;
    private String first_name;
    private String last_name;
    private String social_thumb;
    private Float last_week_distance;
    private Integer rank;

    // KEEP FIELDS - put your custom fields here
    // KEEP FIELDS END

    public LeaderBoard() {
    }

    public LeaderBoard(Long id) {
        this.id = id;
    }

    public LeaderBoard(Long id, String first_name, String last_name, String social_thumb, Float last_week_distance, Integer rank) {
        this.id = id;
        this.first_name = first_name;
        this.last_name = last_name;
        this.social_thumb = social_thumb;
        this.last_week_distance = last_week_distance;
        this.rank = rank;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getSocial_thumb() {
        return social_thumb;
    }

    public void setSocial_thumb(String social_thumb) {
        this.social_thumb = social_thumb;
    }

    public Float getLast_week_distance() {
        return last_week_distance;
    }

    public void setLast_week_distance(Float last_week_distance) {
        this.last_week_distance = last_week_distance;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    // KEEP METHODS - put your custom methods here
    // KEEP METHODS END

}
