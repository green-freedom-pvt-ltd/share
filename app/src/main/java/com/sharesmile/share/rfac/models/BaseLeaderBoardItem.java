package com.sharesmile.share.rfac.models;

/**
 * Created by ankitmaheshwari on 8/24/17.
 */

public class BaseLeaderBoardItem {

    private long id;
    private String name;
    private String image;
    private float distance;
    private int ranking;
    private float amount;

    public BaseLeaderBoardItem(){

    }

    public BaseLeaderBoardItem(long id, String name, String image, float distance, int ranking, float amount) {
        this.id = id;
        this.name = name;
        this.image = image;
        this.distance = distance;
        this.ranking = ranking;
        this.amount = amount;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public int getRanking() {
        return ranking;
    }

    public void setRanking(int ranking) {
        this.ranking = ranking;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }
}
