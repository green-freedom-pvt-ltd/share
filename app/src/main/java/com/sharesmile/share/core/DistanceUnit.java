package com.sharesmile.share.core;

/**
 * Created by ankitmaheshwari on 10/12/17.
 */

public enum DistanceUnit {

    KILOMETER("km"),
    MILES("mi");

    private String label;

    DistanceUnit(String label){
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static DistanceUnit fromString(String input){
        if (input == null || input.isEmpty()){
            return null;
        }
        input = input.toUpperCase();
        for (DistanceUnit distanceUnit : DistanceUnit.values()){
            if (distanceUnit.toString().equals(input)){
                return distanceUnit;
            }
        }
        return null;
    }
}
