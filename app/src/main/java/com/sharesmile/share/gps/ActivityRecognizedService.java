package com.sharesmile.share.gps;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.sharesmile.share.R;
import com.sharesmile.share.utils.Logger;

import java.util.List;

/**
 * Created by piyush on 10/1/16.
 */

public class ActivityRecognizedService extends IntentService {

    public static DetectedActivity detectedActivity;
    public static String detectedActivityText = "Running";

    public ActivityRecognizedService() {
        super("ActivityRecognizedService");
    }

    public ActivityRecognizedService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if(ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            handleDetectedActivities( result.getProbableActivities() );
        }
    }

    private void handleDetectedActivities(List<DetectedActivity> probableActivities) {
        for( DetectedActivity activity : probableActivities ) {
            switch( activity.getType() ) {
                case DetectedActivity.IN_VEHICLE: {
                    Logger.d( "ActivityRecogition", "In Vehicle: " + activity.getConfidence() );
                    detectedActivity = activity;
                    if(activity.getConfidence() > 85) {
                        detectedActivityText = "Driving";
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
                        builder.setContentText( "We have detected that you are driving." );
                        builder.setSmallIcon(getNotificationIcon()).setColor(getResources().getColor(R.color.denim_blue));
                        builder.setLargeIcon(BitmapFactory.decodeResource(getBaseContext().getResources(),
                                R.mipmap.ic_launcher));
                        builder.setContentTitle( getString( R.string.app_name ) );
                        builder.setVibrate(new long[] {500,500,500,500});

                        NotificationManagerCompat.from(this).notify(0, builder.build());
                    }
                    break;
                }
                case DetectedActivity.ON_BICYCLE: {
                    Logger.d( "ActivityRecogition", "On Bicycle: " + activity.getConfidence() );
                    detectedActivity = activity;
                    detectedActivityText = "Cycling";
                    NotificationManagerCompat.from(this).cancel(0);


                    break;
                }
                case DetectedActivity.ON_FOOT: {
                    Logger.d( "ActivityRecogition", "On Foot: " + activity.getConfidence() );
                    NotificationManagerCompat.from(this).cancel(0);
                    break;
                }
                case DetectedActivity.RUNNING: {
                    Logger.d( "ActivityRecogition", "Running: " + activity.getConfidence() );
                    detectedActivity = activity;
                    NotificationManagerCompat.from(this).cancel(0);

                    detectedActivityText = "Running";

                    break;
                }
                case DetectedActivity.STILL: {
                    Logger.d( "ActivityRecogition", "Still: " + activity.getConfidence() );
                    detectedActivityText = "Still";
                    detectedActivity = activity;
                    if(activity.getConfidence() > 85) {
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
                        builder.setContentText("It seems like you are still!");
                        builder.setSmallIcon(getNotificationIcon()).setColor(getResources().getColor(R.color.denim_blue));
                        builder.setLargeIcon(BitmapFactory.decodeResource(getBaseContext().getResources(),
                                R.mipmap.ic_launcher));
                        builder.setContentTitle(getString(R.string.app_name));
                        builder.setVibrate(new long[] {500,500,500,500});
                        builder.setAutoCancel(true);
                        NotificationManagerCompat.from(this).notify(0, builder.build());
                    }
                    break;
                }
                case DetectedActivity.TILTING: {
                    Logger.d( "ActivityRecogition", "Tilting: " + activity.getConfidence() );
                    break;
                }
                case DetectedActivity.WALKING: {
                    Logger.d( "ActivityRecogition", "Walking: " + activity.getConfidence() );
                    detectedActivity = activity;
                    detectedActivityText = "Walking";
                    NotificationManagerCompat.from(this).cancel(0);


                    break;
                }
                case DetectedActivity.UNKNOWN: {
                    Logger.d( "ActivityRecogition", "Unknown: " + activity.getConfidence() );
                    break;
                }
            }
        }
    }

    public static DetectedActivity getDetectedActivity(){
        return detectedActivity;
    }


    private int getNotificationIcon() {
        boolean useWhiteIcon = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP);
        return useWhiteIcon ? R.drawable.ic_stat_onesignal_default : R.mipmap.ic_launcher;
    }


}
