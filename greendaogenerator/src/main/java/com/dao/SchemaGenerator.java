package com.dao;

import java.io.IOException;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Schema;

public class SchemaGenerator {

    public static void main(String[] args) throws IOException, Exception {
        Schema schema = new Schema(1, "com.sharesmile.share");
        addWorkoutData(schema);
        new DaoGenerator().generateAll(schema, "../app/src/main/java-gen");
    }

    private static void addWorkoutData(Schema schema) {
        Entity workout = schema.addEntity("Workout");
        workout.addIdProperty();
        workout.addFloatProperty("distance").notNull();
        workout.addFloatProperty("elapsedTime").notNull();
        workout.addIntProperty("steps");
        workout.addFloatProperty("recordedTime").notNull();
        workout.addFloatProperty("avgSpeed").notNull();
        workout.addDateProperty("date");
    }

}
