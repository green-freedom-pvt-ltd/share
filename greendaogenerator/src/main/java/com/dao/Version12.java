package com.dao;

import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Index;
import de.greenrobot.daogenerator.Property;
import de.greenrobot.daogenerator.Schema;

/**
 * Created by ankitmaheshwari on 7/24/17.
 */

public class Version12 extends SchemaVersion{

    public Version12(boolean current) {
        super(current);
        Schema schema = getSchema();
        addWorkoutData(schema);
        addCauseData(schema);
        addUserData(schema);
        addMessage(schema);
        addLeaderBoard(schema);

    }

    @Override
    public int getVersionNumber() {
        return 12;
    }

    private static void addWorkoutData(Schema schema) {
        Entity workout = schema.addEntity("Workout");
        workout.addIdProperty();
        workout.addFloatProperty("distance").notNull();
        workout.addStringProperty("elapsedTime").notNull();
        workout.addIntProperty("steps");
        workout.addFloatProperty("recordedTime").notNull();
        workout.addFloatProperty("avgSpeed").notNull();
        workout.addStringProperty("causeBrief");
        workout.addDateProperty("date");
        workout.addFloatProperty("runAmount");
        workout.addBooleanProperty("is_sync");
        Property workoutId = workout.addStringProperty("workoutId").getProperty();
        workout.addDoubleProperty("startPointLatitude");
        workout.addDoubleProperty("startPointLongitude");
        workout.addDoubleProperty("endPointLatitude");
        workout.addDoubleProperty("endPointLongitude");
        workout.addLongProperty("beginTimeStamp");
        workout.addLongProperty("endTimeStamp");
        workout.addBooleanProperty("isValidRun");
        workout.addLongProperty("version");
        workout.addDoubleProperty("calories");
        workout.addIntProperty("teamId");
        workout.addIntProperty("numSpikes");
        workout.addIntProperty("numUpdates");
        workout.addStringProperty("appVersion");
        workout.addIntProperty("osVersion");
        workout.addStringProperty("deviceId");
        workout.addStringProperty("deviceName");
        workout.addBooleanProperty("shouldSyncLocationData");

        Index workoutIdUniqueIndex = new Index();
        workoutIdUniqueIndex.addProperty(workoutId);
        workoutIdUniqueIndex.makeUnique();
        workout.addIndex(workoutIdUniqueIndex);
    }

    private static void addUserData(Schema schema) {
        Entity user = schema.addEntity("User");
        user.addIdProperty();
        user.addStringProperty("name");
        user.addStringProperty("emailId").notNull();
        user.addStringProperty("birthday");
        user.addStringProperty("mobileNO");
        user.addStringProperty("gender");
        user.addStringProperty("profileImageUrl");
    }

    private static void addCauseData(Schema schema) {
        Entity cause = schema.addEntity("Cause");

       /* pk: 2,
                cause_title: "Run a mile. Feed a child. (Dummy)",
                cause_description: "Shwas works with the extreme poor section of society and aims to provide them with food which is necessary for them to carry their day. More than 80 million kids are hungry in India and Shwas is working tirelessly to reduce this number everyday.",
                conversion_rate: "0.02",
                min_distance: 100,
                cause_category: "Hunger",*/

        cause.addIdProperty();
        cause.addStringProperty("causeTitle");
        cause.addStringProperty("causeDescription");
        cause.addFloatProperty("conversionRate");
        cause.addIntProperty("minDistance");
        cause.addStringProperty("causeCategory");

        /*

        cause_brief: "For every 1.6 kilometers that you run, a hungry child is fed a healthy meal.",
                cause_image: "http://139.59.243.247/media/photos/2/09-food-security-IndiaInk-superJumbo.jpg",
                cause_thank_you_image: "http://139.59.243.247/media/photos/2/Cancer_HomePage_.jpg",
                cause_share_message_template: "Great Work",
                is_active: false*/

        cause.addStringProperty("causeBrief");
        cause.addStringProperty("causeImage");
        cause.addStringProperty("causeThankyouImage");
        cause.addStringProperty("share_template");
        cause.addBooleanProperty("isActive");

        //sponsors

        /*sponsor_id: 2,
                sponsor_type: "Company",
                sponsor_company: "DBS Bank",
                sponsor_ngo: null,
                sponsor_logo: "http://139.59.243.247/media/photos/2/DBS_Logo.png"*/

        cause.addIntProperty("sponsorId");
        cause.addStringProperty("sponsorCompany");
        cause.addStringProperty("sponsorNgo");
        cause.addStringProperty("sponsorLogo");

        //Partners

      /*  partner_id: 2,
                partner_type: "NGO",
                partner_company: null,
                partner_ngo: "Avanti Fellows",
                partnered_on: "2016-05-25T12:00:00Z"*/

        cause.addIntProperty("partnerId");
        cause.addStringProperty("partnerCompany");
        cause.addStringProperty("partnerNgo");
        cause.addStringProperty("partnerType");

        //cause priority
        cause.addIntProperty("order_priority");

    }

    private static void addMessage(Schema schema) {

        Entity message = schema.addEntity("Message");
        message.addIdProperty();
        message.addStringProperty("message_image").notNull();
        message.addStringProperty("message_title");
        message.addStringProperty("messageBrief");
        message.addStringProperty("message_description").notNull();
        message.addStringProperty("message_date").notNull();
        message.addStringProperty("shareTemplate").notNull();
        message.addStringProperty("videoId").notNull();
        message.addBooleanProperty("is_read");

    }

    private static void addLeaderBoard(Schema schema){

        Entity leaderBoard = schema.addEntity("LeaderBoard");
        leaderBoard.addIdProperty();
        leaderBoard.addStringProperty("first_name");
        leaderBoard.addStringProperty("last_name");
        leaderBoard.addStringProperty("social_thumb");
        leaderBoard.addFloatProperty("last_week_distance");
        leaderBoard.addIntProperty("rank");
    }

}

