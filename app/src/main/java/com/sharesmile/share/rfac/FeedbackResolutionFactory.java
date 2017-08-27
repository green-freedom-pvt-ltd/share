package com.sharesmile.share.rfac;

import com.sharesmile.share.rfac.models.Faq;
import com.sharesmile.share.rfac.models.FeedbackCategory;
import com.sharesmile.share.rfac.models.FeedbackQna;
import com.sharesmile.share.rfac.models.FeedbackResolution;

import java.util.List;

/**
 * Created by ankitmaheshwari on 8/26/17.
 */

public class FeedbackResolutionFactory {

    public static FeedbackResolution getResolutionForCategory(FeedbackCategory category){

        if (FeedbackCategory.DISTANCE_NOT_ACCURATE.equals(category)){
            return new FeedbackResolution("Got it. \n" +
                    "\n" +
                    "Our tracking algorithm uses a combination of GPS and motion sensors in the device to calculate distance.\n" +
                    "Sometimes because of unreliability and low accuracy of these sensor readings it ends up recording wrong distance. \n" +
                    "We are working hard everyday to make our tracking algorithm more accurate and robust and it would help enormously if you could tell us a bit more about the discrepancy you observed.\n",

                    "Tell us more about the issue",

                    "Enter here");
        }else if (FeedbackCategory.LESS_DISTANCE.equals(category)){
            return new FeedbackResolution("Got it. We regret that your distance counted was lesser than actual. \n" +
                    "\n" +
                    "GPS gets tricky at times. But no worries, just enter below the correct distance in Kms, and submit. Or chat with us. \n" +
                    "\n" +
                    "We will look into the case and change accordingly. Thanks for letting us know.\n",

                    "",

                    "Enter correct distance in Kms");
        }else if (FeedbackCategory.MORE_DISTANCE.equals(category)){
            return new FeedbackResolution("Got it. So awesome of you for letting us know !\n" +
                    "\n" +
                    "GPS gets tricky at times. Please enter below the correct distance in Kms and submit. Or chat with us.\n" +
                    "\n" +
                    "Thanks for letting us know.\n",

                    "",

                    "Enter correct distance in Kms");
        }else if (FeedbackCategory.FLAGGED_RUN.equals(category)){
            return new FeedbackResolution("Got it.  \n" +
                    "\n" +
                    "A scratched or a flagged workout is a workout detected as humanly impossible in our system. Hence it is not counted. \n",

                    "Issue still not resolved? Send feedback or chat with us.",

                    "Enter feedback here");
        }else if (FeedbackCategory.NOT_IN_VEHICLE.equals(category)){
            return new FeedbackResolution("Got it. Thanks for informing.\n" +
                    "\n" +
                    "Our automated algorithm detects when our app is used in a vehicle. Your workout is one of the 1.3 % of incorrectly detected cases. We are sorry for that. \n",

                    "Issu still not resolved? Send feedback or chat with us.",

                    "Enter feedback here");
        }else if (FeedbackCategory.IMPACT_MISSING_LEADERBOARD.equals(category)){
            return new FeedbackResolution("Got it. Thanks for informing.\n" +
                    "\n" +
                    "Data in leaderboard is fetched from server, but sometimes your workouts take a few hours to sync on server. Please wait for some time and make sure that you are connected to internet.\n",

                    "Issue still not resolved? Send feedback or chat with us.",

                    "Enter feedback here");
        }else if (FeedbackCategory.STILL_SOMETHING_ELSE.equals(category)){
            return new FeedbackResolution("",

                    "Let us know about your issue. Send feedback or chat with us.",

                    "Enter feedback here");
        }else if (FeedbackCategory.WORKOUT_MISSING_HISTORY.equals(category)){
            return new FeedbackResolution("Got it. Thanks for letting us know.\n" +
                    "\n" +
                    "Please enter the details of your workout and submit. We'll look into it and add from backend\n" +
                    "\n" +
                    "Chat with us if we can assist you with anything else.\n",

                    "",

                    "Enter details here");
        }else if (FeedbackCategory.GPS_ISSUE.equals(category)){
            return new FeedbackResolution("Got it.\n" +
                    "\n" +
                    "GPS can be tricky when you are using the app indoors or when the weather is cloudy/rainy. Try to be in open areas. You can also try restarting the GPS through system settings.\n",

                    "Issue still not resolved? Send feedback or chat with us.",

                    "Enter feedback here");
        }else if (FeedbackCategory.ZERO_DISTANCE.equals(category)){
            return new FeedbackResolution("Got it.\n" +
                    "\n" +
                    "Please enter the details of your workout along with actual distance (in Kms) and submit. We will look into it and add.\n" +
                    "\n" +
                    "Chat with us in case of any other issue.\n",

                    "",

                    "Enter details here");
        }else if (FeedbackCategory.FEEDBACK.equals(category)){
            // This is Level_1 feedback
            return new FeedbackResolution("That is great! We love to hear from our users.",

                    "Submit your feedback or chat with us",

                    "Enter feedback here");
        }else {
            return null;
        }

    }

    public static FeedbackQna getQna(List<Faq> questions){
        return new FeedbackQna(questions,

                "Ask any other question or chat with us.",

                "Enter your question here");
    }

}

