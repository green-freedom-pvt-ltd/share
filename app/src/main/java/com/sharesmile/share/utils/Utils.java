package com.sharesmile.share.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.sharesmile.share.AchievedBadge;
import com.sharesmile.share.AchievedBadgeDao;
import com.sharesmile.share.AchievedTitle;
import com.sharesmile.share.Badge;
import com.sharesmile.share.BadgeDao;
import com.sharesmile.share.BuildConfig;
import com.sharesmile.share.Title;
import com.sharesmile.share.TitleDao;
import com.sharesmile.share.core.Logger;
import com.sharesmile.share.core.ShareImageLoader;
import com.sharesmile.share.core.SharedPrefsManager;
import com.sharesmile.share.core.cause.model.CauseData;
import com.sharesmile.share.core.sync.SyncHelper;
import com.sharesmile.share.core.timekeeping.ServerTimeKeeper;
import com.sharesmile.share.home.settings.AlarmReceiver;
import com.sharesmile.share.login.UserDetails;
import com.sharesmile.share.profile.BodyWeightChangedEvent;
import com.sharesmile.share.leaderboard.LeaderBoardDataStore;
import com.sharesmile.share.core.application.MainApplication;
import com.sharesmile.share.R;
import com.sharesmile.share.Workout;
import com.sharesmile.share.WorkoutDao;
import com.sharesmile.share.analytics.Analytics;
import com.sharesmile.share.analytics.events.AnalyticsEvent;
import com.sharesmile.share.analytics.events.Event;
import com.sharesmile.share.core.config.ClientConfig;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.home.settings.CurrencyCode;
import com.sharesmile.share.home.settings.UnitsManager;
import com.sharesmile.share.tracking.activityrecognition.ActivityDetector;
import com.sharesmile.share.tracking.models.WorkoutData;
import com.sharesmile.share.home.homescreen.OnboardingOverlay;
import com.sharesmile.share.tracking.workout.WorkoutSingleton;
import com.sharesmile.share.tracking.workout.data.model.Run;
import com.sharesmile.share.views.CustomTypefaceSpan;
import com.sharesmile.share.views.LBTextView;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import Models.Level;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;
import uk.co.samuelwall.materialtaptargetprompt.extras.backgrounds.FullscreenPromptBackground;
import uk.co.samuelwall.materialtaptargetprompt.extras.focals.CirclePromptFocal;
import uk.co.samuelwall.materialtaptargetprompt.extras.focals.RectanglePromptFocal;

import static com.sharesmile.share.core.Constants.BADGE_TYPE_CAUSE;
import static com.sharesmile.share.core.Constants.PREF_PENDING_WORKOUT_LOCATION_DATA_QUEUE_PREFIX;
import static com.sharesmile.share.core.Constants.PREF_SHOWN_ONBOARDING;
import static com.sharesmile.share.core.Constants.PREF_STREAK_UPLOADED_FIRST_TIME;
import static com.sharesmile.share.core.Constants.USER_PROP_AVG_CADENCE;
import static com.sharesmile.share.core.Constants.USER_PROP_AVG_SPEED;
import static com.sharesmile.share.core.Constants.USER_PROP_AVG_STRIDE_LENGTH;
import static com.sharesmile.share.core.Constants.USER_PROP_LIFETIME_DISTANCE;
import static com.sharesmile.share.core.Constants.USER_PROP_LIFETIME_STEPS;
import static com.sharesmile.share.core.Constants.USER_PROP_TOTAL_AMT_RAISED;
import static com.sharesmile.share.core.Constants.USER_PROP_TOTAL_CALORIES;
import static com.sharesmile.share.core.Constants.USER_PROP_TOTAL_RUNS;
import static uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt.STATE_DISMISSED;
import static uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt.STATE_FINISHED;
import static uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt.STATE_FOCAL_PRESSED;

/**
 * Created by ankitmaheshwari1 on 08/01/16.
 */
public class Utils {

    private static final String TAG = "Utils";

    /* a utility to validate Indian phone number example - 03498985532, 5389829422 **/
    public static boolean isValidInternationalPhoneNumber(String number) {
        return PhoneNumberUtils.isGlobalPhoneNumber(number);
    }

    public static boolean isValidIndianPhoneNumber(String number) {
        if (!TextUtils.isEmpty(number)) {
            return number.matches("^0?(\\d{10})");
        }
        return false;
    }

    public static boolean isCollectionFilled(Collection<?> collection) {
        return null != collection && collection.isEmpty() == false;
    }

    public static boolean compareLists(List<String> list1, List<String> list2) {
        if (list1.size() != list2.size()) {
            return false;
        }
        Collections.sort(list1);
        Collections.sort(list2);
        for (int index = 0; index < list1.size(); index++) {
            if (list1.get(index).equals(list2.get(index)) == false) {
                return false;
            }
        }
        return true;
    }

    /**
     * Format distance in meters to a two decimal KM value, RoundingMode is FLOOR
     *
     * @param distanceInMeters
     * @return
     */
    public static String formatToKmsWithTwoDecimal(float distanceInMeters) {
        return getDecimalFormat("0.00").format(distanceInMeters / 1000);
    }

    /**
     * Format the input float to a one decimal String, RoundingMode is FLOOR
     *
     * @param distance
     * @return
     */
    public static String formatWithOneDecimal(float distance) {
        return getDecimalFormat("0.0").format(distance);
    }

    /**
     * Format the input double to a one decimal String, RoundingMode is FLOOR
     *
     * @param distance
     * @return
     */
    public static String formatWithOneDecimal(double distance) {
        return getDecimalFormat("0.0").format(distance);
    }

    public static String formatCalories(double calories) {
        String caloriesString = "";
        if (calories > 10) {
            caloriesString = Math.round(calories) + " Cal";
        } else {
            String cals = Utils.formatWithOneDecimal(calories);
            if ("0.0".equals(cals)) {
                caloriesString = "--";
            } else {
                caloriesString = cals + " Cal";
            }
        }
        return caloriesString;
    }

    public static DecimalFormat getDecimalFormat(String pattern) {
        DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        dfs.setDecimalSeparator('.');
        DecimalFormat df = new DecimalFormat(pattern, dfs);
        df.setGroupingUsed(false);
        df.setRoundingMode(RoundingMode.HALF_EVEN);
        return df;
    }

    public static String formatCommaSeparated(long value) {
        if (CurrencyCode.INR.equals(UnitsManager.getCurrencyCode())) {
//            return formatIndianCommaSeparated(value);
            Locale locale = new Locale("EN", "IN");
            NumberFormat format = NumberFormat.getInstance(locale);
            return format.format(value);
        } else {
            Locale locale = new Locale("EN", "US");
            NumberFormat format = NumberFormat.getInstance(locale);
            return format.format(value);
        }
    }

    public static String formatIndianCommaSeparated(long value) {
        Logger.d(TAG, "formatIndianCommaSeparated: value = " + value);
        // remove sign if present
        String raw = String.valueOf(Math.abs(value));
        int numDigits = raw.length();
        StringBuilder sb = new StringBuilder(raw);
        // Reverse the string to start from right most digits
        sb = sb.reverse();
        // Counter to keep track of number of commas placed
        int commas = 0;
        for (int i = 0; i < numDigits; i++) {
            // Insert a comma if i is in the range [3, 5, 7, 9, ...)
            if (i % 2 == 1 && i != 1) {
                sb.insert(i + commas, ",");
                commas++;
            }
        }
        // Reverse the string back to get original number
        String sign = (value < 0) ? "-" : "";
        return sign + sb.reverse().toString();
    }

    /**
     * gets screen height in pixels, Application Context should be used
     */
    public static int getScreenHeightUsingDisplayMetrics(Context context) {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(displaymetrics);
        return displaymetrics.heightPixels;
    }

    /**
     * gets screen width in pixels, Application Context should be used
     */
    public static int getScreenWidthUsingDisplayMetrics(Context context) {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(displaymetrics);
        return displaymetrics.widthPixels;
    }

    public static float convertDpToPixel(Context context, float dp) {
        Context localContext = context != null ? context : MainApplication.getContext();
        DisplayMetrics displayMetrics = localContext.getResources().getDisplayMetrics();
        return dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    public static String createJSONStringFromObject(Object object) {
        Gson gson = new Gson();
        return gson.toJson(object);
    }

    public static <T> T createObjectFromJSONString(String jsonString, Class<T> clazz)
            throws JsonSyntaxException {
        Gson gson = new Gson();
        return gson.fromJson(jsonString, clazz);

    }

    public static void setStaticGoogleMap(int width, int height, ImageView staticMapView,
                                          List<LatLng> points) {
        if (isCollectionFilled(points) && points.size() >= 2) {

            String staticMapUrl = Constants.STATIC_GOOGLE_MAP_BASE_URL + "size=" + width + "x" + height
                    + Constants.STATIC_GOOGLE_MAP_COMMON_PARAMS
                    + "&scale=" + (isScreenTooLarge(staticMapView.getContext()) ? 2 : 1)
                    + getMarkerParams(points.get(0), points.get(points.size() - 1))
                    + getPathParams(points)
                    + "&key=" + Constants.STATIC_GOOGLE_MAP_API_KEY;
            Logger.i(TAG, "Hitting Static Map API with URL: " + staticMapUrl);
            ShareImageLoader.getInstance().loadImage(staticMapUrl, staticMapView);
        }
    }

    public static boolean isScreenTooLarge(Context context) {
        int screenLayoutWithMask = context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_LAYOUTDIR_MASK;
        switch (screenLayoutWithMask) {
            case Configuration.SCREENLAYOUT_SIZE_LARGE:
                return true;
            case Configuration.SCREENLAYOUT_SIZE_XLARGE:
                return true;
            case Configuration.SCREENLAYOUT_SIZE_NORMAL:
                return false;
            case Configuration.SCREENLAYOUT_SIZE_SMALL:
                return false;
        }
        return false;
    }

    public static String getMarkerParams(LatLng startPoint, LatLng endPoint) {
        String firstMarker = "color:blue|label:S|" + startPoint.latitude + "," + startPoint.longitude;
        String secondMarker = "color:red|label:E|" + endPoint.latitude + "," + endPoint.longitude;
        try {
            return "&markers=" + URLEncoder.encode(firstMarker, "UTF-8") + "&markers="
                    + URLEncoder.encode(secondMarker, "UTF-8");
        } catch (UnsupportedEncodingException usee) {
            Logger.e(TAG, usee.getMessage(), usee);
        }
        return "";
    }

    public static String getPathParams(List<LatLng> points) {
        String prefix = "&path=";
        StringBuilder sb = new StringBuilder();
        sb.append("color:0x00ff0080|weight:6");
        for (LatLng point : points) {
            sb.append("|").append(point.latitude).append(",").append(point.longitude);
        }
        try {
            return prefix + URLEncoder.encode(sb.toString(), "UTF-8");
        } catch (UnsupportedEncodingException usee) {
            Logger.e(TAG, usee.getMessage(), usee);
        }
        return "";
    }

    /**
     * Returns time in HH:MM:SS format
     *
     * @param secs time interval in secs
     * @return
     */
    public static final String secondsToHHMMSS(int secs) {

        if (secs >= 3600) {
            int sec = secs % 60;
            int totalMins = secs / 60;
            int hour = totalMins / 60;
            int min = totalMins % 60;
            String formatted = String.format("%02d:%02d:%02d", hour, min, sec);
            if (formatted.startsWith("0")) {
                return formatted.substring(1);
            } else {
                return formatted;
            }
        } else {
            return String.format("%02d:%02d", secs / 60, secs % 60);
        }
    }

    public static final long hhmmssToSecs(String hhmmss) {
        Long secs = 0L;
        String[] parts = hhmmss.split(":");
        switch (parts.length) {
            case 3:
                String left = parts[0];
                if (left.contains(" ")) {
                    String[] leftParts = left.split("\\s+");
                    secs += 86400 * Long.parseLong(leftParts[0]);
                    secs += 3600 * Long.parseLong(leftParts[1]);
                } else {
                    secs += 3600 * Long.parseLong(left);
                }
                secs += 60 * Long.parseLong(parts[1]);
                secs += Long.parseLong(parts[2]);
                break;
            case 2:
                secs += 60 * Long.parseLong(parts[0]);
                secs += Long.parseLong(parts[1]);
                break;
            case 1:
                secs += Long.parseLong(parts[0]);
        }
        return secs;
    }

    public static final String secondsToHoursAndMins(int secs) {
        if (secs >= 3600) {
            int totalMins = secs / 60;
            int hour = totalMins / 60;
            int min = totalMins % 60;
            return String.format("%d hr %d min", hour, min);
        } else {
            int totalMins = secs / 60;
            return String.format("%d min", totalMins);
        }
    }

    public static final String secondsToVoiceUpdate(int secs) {
        if (secs >= 3600) {
            int totalMins = secs / 60;
            int hour = totalMins / 60;
            int min = totalMins % 60;
            if (min == 0) {
                if (hour == 1) {
                    return String.format("1 hour");
                } else {
                    return String.format("%d hours", hour);
                }
            } else {
                return String.format("%d hours %d minutes", hour, min);
            }
        } else {
            int totalMins = secs / 60;
            return String.format("%d minutes", totalMins);
        }
    }

    public static String createPrettyJSONStringFromObject(Object object) {
        return new GsonBuilder()
                .setPrettyPrinting()
                .serializeSpecialFloatingPointValues()
                .create().toJson(object);
    }

    public static final long stringToSec(String time) {

        String[] timeArray = time.split("[:\\s]");
        int j = 1;
        long sec = 0;
        for (int i = timeArray.length - 1; i >= 0; i--) {
            int duration = Integer.parseInt(timeArray[i]);
            sec = duration * j + sec;
            j = j * 60;

        }
        return sec;
    }

    public static void share(Context context, String shareTemplate) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareTemplate);
        shareIntent.setType("text/plain");
        context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.share_via)));
    }

    public static void share(Context context, Uri bitmapUri, String shareTemplate) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareTemplate);
        shareIntent.putExtra(Intent.EXTRA_STREAM, bitmapUri);
        shareIntent.setType("image/*");
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(Intent.createChooser(shareIntent, "send"));
    }

    public static void shareImageWithMessage(final Context context, final String imageUrl, final String shareMessage) {
        ShareImageLoader.getInstance().getImageLoader().load(imageUrl)
                .networkPolicy(NetworkPolicy.OFFLINE).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                Utils.share(context, Utils.getLocalBitmapUri(bitmap, context), shareMessage);
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                Logger.d(TAG, "shareImageWithMessage: Image could not be loaded from disk or memory, will try from network");
                Picasso.with(context).load(imageUrl).into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        Utils.share(context, Utils.getLocalBitmapUri(bitmap, context), shareMessage);
                    }

                    @Override
                    public void onBitmapFailed(Drawable errorDrawable) {
                        Utils.share(context, null, shareMessage);
                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                    }
                });
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        });
    }


    public static Uri getLocalBitmapUri(Bitmap bmp, Context context) {
        Uri bmpUri = null;
        try {
            File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "share_image_" + new Date().getTime() + ".png");
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();
            bmpUri = FileProvider.getUriForFile(context, getFileProvider(context)
                    , file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpUri;
    }

    public static String getFileProvider(Context context) {
        return context.getApplicationContext().getPackageName() + ".core.my.provider";
    }

    public static Bitmap getBitmapFromLiveView(View view) {
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null)
            bgDrawable.draw(canvas);
        else
            canvas.drawColor(Color.WHITE);
        view.draw(canvas);
        return returnedBitmap;
    }

    public static void redirectToPlayStore(Context context) {
        final String appPackageName = BuildConfig.APPLICATION_ID;
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException e) {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }

    public static void launchUri(Context context, Uri uri) {
        Logger.d(TAG, "Launching uri: " + uri.toString());
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, uri));
        } catch (android.content.ActivityNotFoundException e) {
            Logger.d(TAG, "Couldn't launchUri: " + uri.toString());
        }
    }

    public static String dedupName(String firstName, String lastName) {
        if (TextUtils.isEmpty(lastName)) {
            return firstName;
        } else if (TextUtils.isEmpty(firstName)) {
            return lastName;
        }
        // If both the names are present
        String name = firstName + " " + lastName;
        String[] parts = name.split("\\s+");
        StringBuilder sb = new StringBuilder();

        // De dup logic
        int len = parts.length;
        if (len > 1) {
            sb.append(toCamelCase(parts[0]));
            for (int i = 1; i < len; i++) {
                if (!parts[i - 1].equalsIgnoreCase(parts[i])) {
                    sb.append(" ");
                    sb.append(toCamelCase(parts[i]));
                }
            }
            return sb.toString();
        } else if (len == 1) {
            return toCamelCase(parts[0]);
        } else {
            return "";
        }
    }

    public static String toCamelCase(final String init) {
        if (init == null)
            return null;

        final StringBuilder ret = new StringBuilder(init.length());

        for (final String word : init.split(" ")) {
            if (!word.isEmpty()) {
                ret.append(word.substring(0, 1).toUpperCase());
                ret.append(word.substring(1).toLowerCase());
            }
            if (!(ret.length() == init.length()))
                ret.append(" ");
        }

        return ret.toString();
    }


    public static void hideKeyboard(View view, Context context) {
        if (view == null || context == null) {
            return;
        }
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(
                Context.INPUT_METHOD_SERVICE);

        if (view != null) {
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static Run convertWorkoutToRun(Workout workout) {
        Logger.d(TAG, "convertWorkoutToRun");

        Run run = new Run();
        run.setId(workout.getId());
        run.setCauseName(workout.getCauseBrief());
        run.setCauseId(workout.getCauseId() != null ? workout.getCauseId() : 0);

        run.setDistance(workout.getDistance());
        if (workout.getBeginTimeStamp() != null) {
            Logger.d(TAG, "BeginTimeStamp is present, will set start_time of run");
            run.setStartTime(DateUtil.getDefaultFormattedDate(new Date(workout.getBeginTimeStamp())));
        }
        if (workout.getEndTimeStamp() != null) {
            run.setEndTime(DateUtil.getDefaultFormattedDate(new Date(workout.getEndTimeStamp())));
        }
        run.setRunAmount(workout.getRunAmount() == null ? 0 : workout.getRunAmount());
        run.setRunDuration(workout.getElapsedTime());
        run.setNumSteps(workout.getSteps() == null ? 0 : workout.getSteps());
        run.setAvgSpeed(workout.getAvgSpeed());
        run.setClientRunId(workout.getWorkoutId());
        if (workout.getStartPointLatitude() != null) {
            run.setStartLocationLat(workout.getStartPointLatitude());
        }
        if (workout.getStartPointLongitude() != null) {
            run.setStartLocationLong(workout.getStartPointLongitude());
        }
        if (workout.getEndPointLatitude() != null) {
            run.setEndLocationLat(workout.getEndPointLatitude());
        }
        if (workout.getEndPointLongitude() != null) {
            run.setEndLocationLong(workout.getEndPointLongitude());
        }
        run.setIsFlag(!workout.getIsValidRun());
        run.setTeamId(workout.getTeamId() != null ? workout.getTeamId() : 0);
        run.setNumSpikes(workout.getNumSpikes() != null ? workout.getNumSpikes() : 0);
        run.setNumUpdates(workout.getNumUpdates() != null ? workout.getNumUpdates() : 0);
        run.setAppVersion(workout.getAppVersion() != null ? workout.getAppVersion() : "");
        run.setOsVersion(workout.getOsVersion() != null ? workout.getOsVersion() : 0);
        run.setDeviceId(workout.getDeviceId() != null ? workout.getDeviceId() : "");
        run.setDeviceName(workout.getDeviceName() != null ? workout.getDeviceName() : "");

        return run;
    }

    public static Run convertWorkoutDataToRun(WorkoutData data) {
        Logger.d(TAG, "convertWorkoutDataToRun");

        Run run = new Run();
        run.setDistance(data.getDistance() / 1000);
        if (data.getBeginTimeStamp() > 0) {
            Logger.d(TAG, "BeginTimeStamp is present, will set start_time of run");
            run.setStartTime(DateUtil.getDefaultFormattedDate(new Date(data.getBeginTimeStamp())));
        }
        run.setRunDuration(Utils.secondsToHHMMSS((int) data.getElapsedTime()));
        run.setNumSteps(data.getTotalSteps());
        run.setAvgSpeed(data.getAvgSpeed());
        run.setClientRunId(data.getWorkoutId());
        if (data.getStartPoint() != null) {
            run.setStartLocationLat(data.getStartPoint().latitude);
        }
        if (data.getStartPoint() != null) {
            run.setStartLocationLong(data.getStartPoint().longitude);
        }
        if (data.getLatestPoint() != null) {
            run.setEndLocationLat(data.getLatestPoint().latitude);
        }
        if (data.getLatestPoint() != null) {
            run.setEndLocationLong(data.getLatestPoint().longitude);
        }
        run.setTeamId(LeaderBoardDataStore.getInstance().getMyTeamId());
        run.setNumSpikes(data.getNumGpsSpikes());
        run.setNumUpdates(data.getNumUpdateEvents());
        run.setAppVersion(Utils.getAppVersion());
        run.setOsVersion(Build.VERSION.SDK_INT);
        run.setDeviceId(Utils.getUniqueId(MainApplication.getContext()));
        run.setDeviceName(Utils.getDeviceName());

        return run;
    }

    /**
     * Calculates DeltaCalories as per METS formula
     *
     * @param deltaTimeMillis time interval in millis in which this distance is covered
     * @param deltaSpeed      speed in m/s during which the distance was covered
     * @return Kcal calculated using METS formula
     */
    public static double getDeltaCaloriesMets(long deltaTimeMillis, float deltaSpeed) {
        double mets = getMetsValue(deltaSpeed);
        float bodyWeightKgs = MainApplication.getInstance().getBodyWeight();
        return mets * bodyWeightKgs * (((double) (deltaTimeMillis)) / (1000 * 60 * 60));
    }

    public static double getMetsValue(float deltaSpeed) {
        // deltaSpeed is in m/s
        double mph = 2.23694 * deltaSpeed;

        // Referring Compendium of Physical Activities over here
        // https://sites.google.com/site/compendiumofphysicalactivities/Activity-Categories/walking
        // and here
        // https://sites.google.com/site/compendiumofphysicalactivities/Activity-Categories/running

        if (mph <= 0.625) {
            return 0;
        } else if (mph <= 1) {
            return 1.3;
        } else if (mph <= 2) {
            return (1.3 + 1.5 * (mph - 1)); // 2.8 at 2 mph
        } else if (mph <= 2.5) {
            return 2.8 + 0.8 * (mph - 2); // 3.2 at 2.5 mph
        } else if (mph <= 3.5) {
            return 3.2 + 1.4 * (mph - 2.5); // 4.6 at 3.5 mph
        } else if (mph <= 4) {
            if (ActivityDetector.getInstance().getRunningConfidence()
                    >= ActivityDetector.getInstance().getWalkingConfidence()) {
                // User is running
                return 4.8 + 3.2 * (mph - 3.5); // 6.4 at 4 mph
            } else {
                // User is Walking
                return 4.6 + 1.4 * (mph - 3.5); // 5.3 at 4 mph
            }
        }
        /// All walking values uptill here, range 4-5 mph is ambiguous range need to decide between running and walking
        else if (mph <= 5) {
            if (ActivityDetector.getInstance().getRunningConfidence()
                    >= ActivityDetector.getInstance().getWalkingConfidence()) {
                // User is running
                return 6.4 + 2.3 * (mph - 4); // 8.7 at 5 mph
            } else {
                // User is Walking
                if (mph <= 4.5) {
                    // 4 - 4.5 mph
                    return 5.3 + 4.2 * (mph - 4); // 7.4 at 4.5 mph
                } else {
                    // 4.5 - 5 mph
                    return 7.4 + 2.6 * (mph - 4.5); // 8.7 at 5 mph
                }
            }
        } else if (mph <= 6) {
            return 8.7 + 1.6 * (mph - 5); // 10.3 at 6 mph
        } else if (mph <= 7) {
            return 10.3 + 1.3 * (mph - 6); // 11.6 at 7mph
        } else if (mph <= 7.7) {
            return 11.6 + 1.143 * (mph - 7); // 12.4 at 7.7 mph
        } else if (mph <= 9) {
            return 12.4 + 0.77 * (mph - 7.7); // 13.4 at 9 mph
        } else if (mph <= 10) {
            return 13.4 + 1.7 * (mph - 9); // 15.1 at 10 mph
        } else if (mph <= 11) {
            return 15.1 + 1.5 * (mph - 10); // 16.6 at 11 mph
        } else if (mph <= 12) {
            return 16.6 + 3 * (mph - 11); // 19.6 at 12 mph
        } else if (mph <= 14) {
            return 19.6 + 2 * (mph - 12); // 23.6 at 14 mph
        } else if (mph <= 15) {
            return 23.6 + 1 * (mph - 14); // 24.6 at 15 mph
        } else {
            // For speeds greater than 15 mph (23 kmph) we assume that the person is driving so we don't add calories
            return 1.3;
        }
    }

    public static double getDeltaCaloriesKarkanen(long deltaTimeMillis, float deltaSpeed) {
        double mph = 2.23694 * deltaSpeed;
        float bodyWeightKgs = MainApplication.getInstance().getBodyWeight();
        if (bodyWeightKgs == 0) {
            return 0;
        }
        double bodyWeightLbs = 2.205 * bodyWeightKgs;
        double mins = ((double) deltaTimeMillis) / (1000 * 60);

        if (mph <= 0) {
            return 0;
        } else if (mph <= 1) {
            // METS formula for the case when speed is extremely low
            return 1.3 * bodyWeightKgs * (mins / 60);
        } else if (mph <= 3) {
            return bodyWeightLbs * mins * karkanenCalorieRateForWalking(mph, bodyWeightLbs);
        } else if (mph <= 5) {
            // Range 3-5 mph is ambiguous range need to decide between running and walking
            if (ActivityDetector.getInstance().getRunningConfidence()
                    >= ActivityDetector.getInstance().getWalkingConfidence()) {
                // User is Running
                return bodyWeightLbs * mins * karkanenCalorieRateForRunning(mph, bodyWeightLbs);
            } else {
                // User is Walking
                return bodyWeightLbs * mins * karkanenCalorieRateForWalking(mph, bodyWeightLbs);
            }
        } else if (mph <= 14) {
            // User is assumed to be running
            return bodyWeightLbs * mins * karkanenCalorieRateForRunning(mph, bodyWeightLbs);
        } else {
            // Too fast, user must be in a vehicle
            // Hence METS formula for calculating calories burned for a static user
            return 1.3 * bodyWeightKgs * (mins / 60);
        }

    }

    /**
     * Calculates Karkanen rate of burning calories per lb per min for a Walking user
     *
     * @param speed       in mph
     * @param weightInLbs in lbs
     * @return Returns Kcal/lb-min
     */
    public static double karkanenCalorieRateForWalking(double speed, double weightInLbs) {
        double a = 0.0195;
        double b = (-1) * 0.00436;
        double c = 0.00245;
        double d = (0.000801 * Math.pow(weightInLbs / 154, 0.425)) / weightInLbs;
        return a + b * speed + c * Math.pow(speed, 2) + d * Math.pow(speed, 3);
    }

    /**
     * Calculates Karkanen rate of burning calories per lb per min for a Running user
     *
     * @param speed       in mph
     * @param weightInLbs in lbs
     * @return Returns Kcal/lb-min
     */
    public static double karkanenCalorieRateForRunning(double speed, double weightInLbs) {
        double a = 0.0395;
        double b = 0.00327;
        double c = 0.000455;
        double d = (0.00801 * Math.pow(weightInLbs / 154, 0.425)) / weightInLbs;
        return a + b * speed + c * Math.pow(speed, 2) + d * Math.pow(speed, 3);
    }

    public static final Level getLevel(int impactInRupees) {
        Iterator<Map.Entry<Integer, Level>> iter = Constants.LEVELS_MAP.entrySet().iterator();
        Level result = null;
        while (iter.hasNext()) {
            Map.Entry<Integer, Level> entry = iter.next();
            Level level = entry.getValue();
            if (impactInRupees >= level.getMinImpact() && impactInRupees < level.getMaxImpact()) {
                result = level;
                break;
            }
        }
        return result;
    }

    /**
     * Updates the overall metrics related to historical run data
     * For e.g. lifetime_impact, lifetime_distance, etc.
     */
    public static void updateTrackRecordFromDb() {

        // SQL Fiddle to refer: http://sqlfiddle.com/#!7/f0aed/1

        WorkoutDao mWorkoutDao = MainApplication.getInstance().getDbWrapper().getWorkoutDao();
        int totalRuns = (int) mWorkoutDao.queryBuilder().where(WorkoutDao.Properties.IsValidRun.eq(true)).count();

        SQLiteDatabase database = MainApplication.getInstance().getDbWrapper().getDaoSession().getDatabase();
        // Calculate total_amount_raised, total_distance, total_steps, total_recorded_time, and total_calories
        Cursor cursor = database.rawQuery("SELECT "
                + "SUM(" + WorkoutDao.Properties.RunAmount.columnName + "), "
                + "SUM(" + WorkoutDao.Properties.Distance.columnName + "), "
                + "SUM(" + WorkoutDao.Properties.Steps.columnName + "), "
                + "SUM(" + WorkoutDao.Properties.RecordedTime.columnName + "), "
                + "SUM(" + WorkoutDao.Properties.Calories.columnName + ") "
                + "FROM " + WorkoutDao.TABLENAME + " where "
                + WorkoutDao.Properties.IsValidRun.columnName + " is 1", new String[]{});
        cursor.moveToFirst();
        int totalAmountRaised = (int) Math.floor(cursor.getFloat(0));
        long totalDistance = Math.round(cursor.getDouble(1));
        long totalSteps = cursor.getLong(2);
        long totalRecordedTime = Math.round(cursor.getDouble(3));
        long totalCalories = Math.round(cursor.getDouble(4));
        cursor.close();
        Logger.d(TAG, "updateTrackRecordFromDb: totalAmountRaised: " + totalAmountRaised
                + ", totalDistance: " + totalDistance + ", totalSteps: " + totalSteps
                + ", totalRecordedTime: " + totalRecordedTime + ", totalCalories: " + totalCalories
                + ", totalRuns:  " + totalRuns);

        SharedPrefsManager.getInstance().setLong(Constants.PREF_WORKOUT_LIFETIME_DISTANCE, totalDistance); // in Kms
        SharedPrefsManager.getInstance().setLong(Constants.PREF_WORKOUT_LIFETIME_STEPS, totalSteps);
        SharedPrefsManager.getInstance().setLong(Constants.PREF_WORKOUT_LIFETIME_WORKING_OUT, totalRecordedTime); // in secs
        SharedPrefsManager.getInstance().setLong(Constants.PREF_TOTAL_CALORIES, totalCalories);

        SharedPrefsManager.getInstance().setInt(Constants.PREF_TOTAL_RUN, totalRuns);
        SharedPrefsManager.getInstance().setInt(Constants.PREF_TOTAL_IMPACT, totalAmountRaised);

        setTrackRecordForAnalytics();

    }

    public static String getAppVersion() {
        String appVersion = SharedPrefsManager.getInstance().getString(Constants.PREF_APP_VERSION);
        if (TextUtils.isEmpty(appVersion)) {
            MainApplication.getInstance().updateAppVersionInPrefs();
        }
        return appVersion;
    }


    private static void setTrackRecordForAnalytics() {
        Analytics.getInstance().setUserProperty(USER_PROP_LIFETIME_DISTANCE,
                SharedPrefsManager.getInstance().getLong(Constants.PREF_WORKOUT_LIFETIME_DISTANCE));
        Analytics.getInstance().setUserProperty(USER_PROP_LIFETIME_STEPS,
                SharedPrefsManager.getInstance().getLong(Constants.PREF_WORKOUT_LIFETIME_STEPS));
        Analytics.getInstance().setUserProperty(USER_PROP_AVG_STRIDE_LENGTH, getAverageStrideLength());
        Analytics.getInstance().setUserProperty(USER_PROP_AVG_SPEED, getLifetimeAverageSpeed());
        Analytics.getInstance().setUserProperty(USER_PROP_AVG_CADENCE, getLifetimeAverageStepsPerSec());
        Analytics.getInstance().setUserProperty(USER_PROP_TOTAL_CALORIES,
                SharedPrefsManager.getInstance().getLong(Constants.PREF_TOTAL_CALORIES));

        Analytics.getInstance().setUserProperty(USER_PROP_TOTAL_RUNS,
                SharedPrefsManager.getInstance().getInt(Constants.PREF_TOTAL_RUN));
        Analytics.getInstance().setUserProperty(USER_PROP_TOTAL_AMT_RAISED,
                SharedPrefsManager.getInstance().getInt(Constants.PREF_TOTAL_IMPACT));
    }

    public static float getAverageStrideLength() {
        long lifetimeDistance = SharedPrefsManager.getInstance().getLong(Constants.PREF_WORKOUT_LIFETIME_DISTANCE); // in Kms
        long lifetimeSteps = SharedPrefsManager.getInstance().getLong(Constants.PREF_WORKOUT_LIFETIME_STEPS);

        if (lifetimeDistance == 0 || lifetimeSteps == 0) {
            return 0;
        }
        return ((float) lifetimeDistance * 1000) / ((float) lifetimeSteps); // in meter/step
    }

    public static float getNormalizedStrideLength(float inputStrideLength) {
        // Calculate avgStrideLength (distance covered in one foot step) of the user
        float normalisedStrideLength = (inputStrideLength == 0)
                ? (ClientConfig.getInstance().GLOBAL_AVERAGE_STRIDE_LENGTH) : inputStrideLength;
        // Normalising averageStrideLength obtained
        if (normalisedStrideLength < ClientConfig.getInstance().GLOBAL_STRIDE_LENGTH_LOWER_LIMIT) {
            normalisedStrideLength = ClientConfig.getInstance().GLOBAL_STRIDE_LENGTH_LOWER_LIMIT;
        }
        if (normalisedStrideLength > ClientConfig.getInstance().GLOBAL_STRIDE_LENGTH_UPPER_LIMIT) {
            normalisedStrideLength = ClientConfig.getInstance().GLOBAL_STRIDE_LENGTH_UPPER_LIMIT;
        }
        return normalisedStrideLength;
    }

    public static float getLifetimeAverageSpeed() {
        long lifetimeDistance = SharedPrefsManager.getInstance().getLong(Constants.PREF_WORKOUT_LIFETIME_DISTANCE); // in Kms
        long lifetimeWorkingOut = SharedPrefsManager.getInstance().getLong(Constants.PREF_WORKOUT_LIFETIME_WORKING_OUT); // in secs

        if (lifetimeDistance <= 0 || lifetimeWorkingOut <= 0) {
            return 0;
        }
        float lifeTimeHours = ((float) lifetimeWorkingOut) / 3600;
        return ((float) lifetimeDistance) / lifeTimeHours; // in Km/hr
    }

    public static float getLifetimeAverageStepsPerSec() {
        long lifetimeSteps = SharedPrefsManager.getInstance().getLong(Constants.PREF_WORKOUT_LIFETIME_STEPS);
        long lifetimeWorkingOut = SharedPrefsManager.getInstance().getLong(Constants.PREF_WORKOUT_LIFETIME_WORKING_OUT);

        if (lifetimeSteps <= 0 || lifetimeWorkingOut <= 0) {
            return 0;
        }
        return ((float) lifetimeSteps) / ((float) lifetimeWorkingOut);
    }

    public static AlertDialog showWeightInputDialog(final Activity activity) {
        if (activity == null || activity.isFinishing()) {
            return null;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(activity.getString(R.string.enter_weight));

        // Set up the input
        LayoutInflater inflater = LayoutInflater.from(activity);
        final View container = inflater.inflate(R.layout.dialog_weight_input_container, null);
        final EditText editText = (EditText) container.findViewById(R.id.et_body_weight_kg);
        builder.setView(container);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // This method is overridden below
            }
        });
        builder.setNegativeButton("Later", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        final AlertDialog dialog = builder.create();
        dialog.show();

        AnalyticsEvent.create(Event.ON_LOAD_WEIGHT_INPUT_DIALOG)
                .buildAndDispatch();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputWeight = editText.getText().toString();
                if (TextUtils.isEmpty(inputWeight)) {
                    MainApplication.showToast(R.string.enter_actual_weight);
                } else {
                    float weight = Float.parseFloat(inputWeight);
                    if (weight < 10 || weight > 200) {
                        MainApplication.showToast(R.string.enter_actual_weight);
                    } else {
                        MainApplication.getInstance().setBodyWeight(weight);
                        EventBus.getDefault().post(new BodyWeightChangedEvent());
                        dialog.dismiss();
                        MainApplication.showToast("Will count calories for future walks/runs.");
                    }
                }
            }
        });

        return dialog;
    }

    public static AlertDialog showEnterEmailDialog(final Activity activity) {
        if (activity == null || activity.isFinishing()) {
            return null;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(activity.getString(R.string.please_enter_email));

        // Set up the input
        LayoutInflater inflater = LayoutInflater.from(activity);
        final View container = inflater.inflate(R.layout.dialog_weight_input_container, null);
        final EditText editText = (EditText) container.findViewById(R.id.et_body_weight_kg);
        builder.setView(container);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // This method is overridden below
            }
        });
        builder.setNegativeButton("Later", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        final AlertDialog dialog = builder.create();
        dialog.show();

        AnalyticsEvent.create(Event.ON_LOAD_WEIGHT_INPUT_DIALOG)
                .buildAndDispatch();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputWeight = editText.getText().toString();
                if (TextUtils.isEmpty(inputWeight)) {
                    MainApplication.showToast(R.string.enter_actual_weight);
                } else {
                    float weight = Float.parseFloat(inputWeight);
                    if (weight < 10 || weight > 200) {
                        MainApplication.showToast(R.string.enter_actual_weight);
                    } else {
                        MainApplication.getInstance().setBodyWeight(weight);
                        EventBus.getDefault().post(new BodyWeightChangedEvent());
                        dialog.dismiss();
                        MainApplication.showToast("Will count calories for future walks/runs.");
                    }
                }
            }
        });

        return dialog;
    }

    public static String getLocationDataFileName(String workoutId, int batchNum) {
        return "location_data_" + workoutId + "_" + batchNum;
    }

    public static double logBase2(double num) {
        return (Math.log(num) / Math.log(2));
    }

    public static String UNIQUE_ID;

    /**
     * Returns a unique ID of device that is ANDROID_ID
     *
     * @param context
     * @return
     */
    public static String getUniqueId(Context context) {
        if (!TextUtils.isEmpty(UNIQUE_ID)) {
            return UNIQUE_ID;
        }
        if (null == context) {
            context = MainApplication.getContext();
        }
        UNIQUE_ID = getAndroidID(context);
        return UNIQUE_ID;
    }

    /**
     * Returns device IMEI, requires READ_PHONE_STATE permission which belongs to Phone group
     *
     * @param context
     * @return device IMEI
     */
    private static String getDeviceIMEI(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getDeviceId();
    }

    /**
     * A 64-bit number (as a hex string) that is randomly generated when the user first sets up the
     * device and should remain constant for the lifetime of the user's device.
     * The value may change if a factory reset is performed on the device
     *
     * @param context
     * @return
     */
    private static String getAndroidID(Context context) {
        String androidId = Settings.Secure
                .getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        if (androidId != null) {
            return androidId;
        } else {
            return "";
        }
    }

    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    private static String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    public static int convertDistanceToRupees(float conversionRate, float distanceInMeters) {
        return Math.round(conversionRate * (distanceInMeters / 1000));
    }

    public static String getWorkoutLocationDataPendingQueuePrefKey(String workoutId) {
        return PREF_PENDING_WORKOUT_LOCATION_DATA_QUEUE_PREFIX + workoutId;
    }

    public static int convertSpToPixels(float sp, Context context) {
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.getResources().getDisplayMetrics());
        return px;
    }


    /**
     * Get ISO 3166-1 alpha-2 country code for this device (or null if not available)
     *
     * @param context Context reference to get the TelephonyManager instance from
     * @return country code or null
     */
    public static String getUserCountry(Context context) {
        try {
            final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            final String simCountry = tm.getSimCountryIso();
            if (simCountry != null && simCountry.length() == 2) { // SIM country code is available
                return simCountry.toLowerCase(Locale.US);
            } else if (tm.getPhoneType() != TelephonyManager.PHONE_TYPE_CDMA) { // device is not 3G (would be unreliable)
                String networkCountry = tm.getNetworkCountryIso();
                if (networkCountry != null && networkCountry.length() == 2) { // network country code is available
                    return networkCountry.toLowerCase(Locale.US);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static MaterialTapTargetPrompt setOverlay(final OnboardingOverlay overlay, View target,
                                                     Activity activity, boolean isRectangular, boolean isDissmissEnable, boolean isRect) {
        Logger.d(TAG, "showOverlay: " + overlay.name());
        MaterialTapTargetPrompt.Builder builder = new MaterialTapTargetPrompt.Builder(activity);
        builder.setTarget(target);

        builder.setPrimaryText(overlay.getTitle());
        builder.setSecondaryText(overlay.getDescription());
        if (isRectangular) {
            builder.setPromptBackground(new FullscreenPromptBackground());
//            builder.setPromptBackground(new RectanglePromptBackground());
            if (isRect)
                builder.setPromptFocal(new RectanglePromptFocal());
            else
                builder.setPromptFocal(new CirclePromptFocal());
        }
        builder.setAutoDismiss(isDissmissEnable);
        builder.setAutoFinish(isDissmissEnable);
        builder.setCaptureTouchEventOutsidePrompt(isDissmissEnable);
        builder.setPromptStateChangeListener(new MaterialTapTargetPrompt.PromptStateChangeListener() {
            @Override
            public void onPromptStateChanged(MaterialTapTargetPrompt prompt, int state) {
                if (state == STATE_FOCAL_PRESSED) {
                    Logger.d(TAG, overlay.name() + " onPromptStateChanged: " + STATE_FOCAL_PRESSED);
                    overlay.registerUseOfOverlay();
                    prompt.dismiss();
                }
                if (state == STATE_DISMISSED) {
                    Logger.d(TAG, overlay.name() + " onPromptStateChanged: " + STATE_DISMISSED);
                    overlay.registerUseOfOverlay();
                    prompt.dismiss();
                }
                if (state == STATE_FINISHED) {
                    Logger.d(TAG, overlay.name() + " onPromptStateChanged: " + STATE_FINISHED);
                    prompt.dismiss();
                    overlay.registerUseOfOverlay();
                }
            }
        });
        MaterialTapTargetPrompt materialTapTargetPrompt = builder.create();
        return materialTapTargetPrompt;
    }

    public static String getCurrentDateDDMMYYYY() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        return simpleDateFormat.format(new Date(ServerTimeKeeper.getInstance().getServerTimeAtSystemTime(calendar.getTimeInMillis())));
    }

    public static String getDateDDMMYYYYFromTimeInMillis(long timeInMillis) {
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTimeInMillis(timeInMillis);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        return simpleDateFormat.format(new Date(timeInMillis));
    }

    public static void setReminderTime(String time, Context context) {
        if (time.length() == 0) {
            SharedPrefsManager.getInstance().setBoolean(Constants.REMINDER_SET, false);
        } else {
            SharedPrefsManager.getInstance().setBoolean(Constants.REMINDER_SET, true);
            SharedPrefsManager.getInstance().setString(Constants.REMINDER_TIME, time);
            Calendar calendar = Calendar.getInstance();
            String[] hhmm = time.split(":");
            if (hhmm.length == 2) {
                calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hhmm[0]));
                calendar.set(Calendar.MINUTE, Integer.parseInt(hhmm[1]));
                calendar.set(Calendar.SECOND, 0);
                if (calendar.before(Calendar.getInstance())) {
                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                }
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                Intent intent = new Intent(context, AlarmReceiver.class);
                PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, alarmPendingIntent);

                AnalyticsEvent.create(Event.ON_SET_REMINDER)
                        .put("reminder_time", time)
                        .buildAndDispatch();
            }
        }

    }

    public static void cancelReminderTime(Context context) {
        SharedPrefsManager.getInstance().setBoolean(Constants.REMINDER_SET, false);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        alarmManager.cancel(alarmPendingIntent);
    }

    public static Calendar getReminderTime() {
        Calendar calendar = Calendar.getInstance();
        String time = SharedPrefsManager.getInstance().getString(Constants.REMINDER_TIME, calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE));
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
        try {
            calendar.setTimeInMillis(simpleDateFormat.parse(time).getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return calendar;
    }

    public static void setNumberPicker(NumberPicker picker, String s[], int setDefault) {
        picker.setValue(0);
        picker.setMinValue(0);
        picker.setMaxValue(s.length - 1);
        //implement array string to number picker
        picker.setDisplayedValues(s);
        picker.setWrapSelectorWheel(false);
        //disable soft keyboard
        picker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        if (setDefault != -1)
            picker.setValue(setDefault);
    }

    public static boolean checkOnboardingShown() {

        return SharedPrefsManager.getInstance().getBoolean(PREF_SHOWN_ONBOARDING, false);
    }

    public static void setOnboardingShown() {
        SharedPrefsManager.getInstance().setBoolean(PREF_SHOWN_ONBOARDING, true);
    }
    public static boolean checkStreakUploaded() {

        return SharedPrefsManager.getInstance().getBoolean(PREF_STREAK_UPLOADED_FIRST_TIME, false);
    }

    public static void setStreakUploaded(boolean b) {
        SharedPrefsManager.getInstance().setBoolean(PREF_STREAK_UPLOADED_FIRST_TIME, b);
    }


    public static void setMenuText(MenuItem menuItem, Context context, String s, int color) {
        SpannableString spannableString = new SpannableString(s);
        spannableString.setSpan(new RelativeSizeSpan(0.8f), 0, spannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); // set size
        Typeface font = Typeface.createFromAsset(context.getAssets(), "fonts/Lato-Bold.ttf");
        spannableString.setSpan(new CustomTypefaceSpan("", font), 0, spannableString.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(color), 0, spannableString.length(), 0);
        menuItem.setTitle(spannableString);

    }

    public static String cmsToInches(int cms) {
        int feet = (int) (cms / 30.48);
        int inches = (int) Math.round((cms % 30.48) * 0.393701);
        inches = inches == 12 ? 11 : inches;
        return feet + "' " + inches + "\"";
    }

    public static String inchesTocms(String i) {
        String[] feetInches = i.split(" ");
        int feet = Integer.parseInt(feetInches[0].substring(0, feetInches[0].length() - 1));
        int inches = Integer.parseInt(feetInches[1].substring(0, feetInches[1].length() - 1));
        int cms = (int) Math.round((feet * 30.48) + (inches / 0.393701));
        return cms + "";
    }

    public static void setBadgeForCategory(CauseData causeData, String type, int paramDone) {
        AchievedBadgeDao achievedBadgeDao = MainApplication.getInstance().getDbWrapper().getAchievedBadgeDao();
        boolean categoryCompleted = true;
        List<AchievedBadge> achievedBadges;
        if (causeData != null) {
            achievedBadges = achievedBadgeDao.queryBuilder()
                    .where(AchievedBadgeDao.Properties.CauseId.eq(causeData.getId()),
                            AchievedBadgeDao.Properties.UserId.eq(MainApplication.getInstance().getUserID())/*,
                            AchievedBadgeDao.Properties.CategoryStatus.eq(Constants.BADGE_IN_PROGRESS)*/).list();
            categoryCompleted = causeData.isCompleted();
        } else {
            achievedBadges = achievedBadgeDao.queryBuilder()
                    .where(AchievedBadgeDao.Properties.BadgeType.eq(type),
                            AchievedBadgeDao.Properties.UserId.eq(MainApplication.getInstance().getUserID()),
                            AchievedBadgeDao.Properties.CategoryStatus.eq(Constants.BADGE_IN_PROGRESS)).list();
            categoryCompleted = paramDone == 0 ? true : false;
        }
        AchievedBadge achievedBadge = null;
        if (achievedBadges.size() > 0) {
            achievedBadge = achievedBadges.get(0);
            if (categoryCompleted) {
                if (((type.equalsIgnoreCase(Constants.BADGE_TYPE_STREAK) && achievedBadge.getParamDone() != 0) ||
                        (!type.equalsIgnoreCase(Constants.BADGE_TYPE_STREAK))))
                    achievedBadge.setCategoryStatus(Constants.BADGE_COMPLETED);
            } else
                achievedBadge.setCategoryStatus(Constants.BADGE_IN_PROGRESS);
        } else if (type.equalsIgnoreCase(Constants.BADGE_TYPE_STREAK) || !categoryCompleted) {
            BadgeDao badgeDao = MainApplication.getInstance().getDbWrapper().getBadgeDao();
            String category = causeData == null ? type : causeData.getCategory();

            List<Badge> badges = badgeDao.queryBuilder()
                    .where(BadgeDao.Properties.Category.eq(category)).orderAsc(BadgeDao.Properties.NoOfStars).limit(1).list();
            if (badges.size() > 0) {
                Badge badge = badges.get(0);
                achievedBadge = new AchievedBadge();
                achievedBadge.setServerId(0);
                achievedBadge.setBadgeIdInProgress(badge.getBadgeId());
                achievedBadge.setBadgeType(badge.getType());
                achievedBadge.setCauseName(badge.getName());
                if(badge.getType().equalsIgnoreCase(BADGE_TYPE_CAUSE))
                achievedBadge.setCategory(causeData.getCategoryId());

                achievedBadge.setUserId(MainApplication.getInstance().getUserDetails().getUserId());
                achievedBadge.setParamDone(paramDone);
                if (categoryCompleted)
                    achievedBadge.setCategoryStatus(Constants.BADGE_COMPLETED);
                else
                    achievedBadge.setCategoryStatus(Constants.BADGE_IN_PROGRESS);
                if (type.equalsIgnoreCase(Constants.BADGE_TYPE_STREAK)) {
                    if (categoryCompleted && paramDone == 0) {
                        achievedBadge.setCategoryStatus(Constants.BADGE_IN_PROGRESS);
                    }
                }
            }
        }
        if (achievedBadge != null) {
            achievedBadge.setCauseId(causeData == null ? 0 : causeData.getId());
            if(causeData!=null)
            achievedBadge.setCauseName(causeData.getTitle());
            if (achievedBadge.getId() != null && achievedBadge.getId() > 0)
                achievedBadgeDao.update(achievedBadge);
            else
                achievedBadgeDao.insertOrReplace(achievedBadge);
        }
    }


    public static long checkAchievedBadge(double distanceCovered, String badgeType, CauseData mCauseData) {
        BadgeDao badgeDao = MainApplication.getInstance().getDbWrapper().getBadgeDao();
        List<Badge> badges;
        String category = "";
        if (badgeType.equalsIgnoreCase(Constants.BADGE_TYPE_CAUSE)) {
            badges = badgeDao.queryBuilder().where(BadgeDao.Properties.Type.eq(badgeType),
                    BadgeDao.Properties.Category.eq(mCauseData.getCategory()))
                    .orderAsc(BadgeDao.Properties.NoOfStars).list();
            category = mCauseData.getCategory();
        } else {
            badges = badgeDao.queryBuilder().where(BadgeDao.Properties.Type.eq(badgeType))
                    .orderAsc(BadgeDao.Properties.NoOfStars).list();
            category = badgeType;
        }

        AchievedBadgeDao achievedBadgeDao = MainApplication.getInstance().getDbWrapper().getAchievedBadgeDao();
        List<AchievedBadge> achievedBadges;
        if (badgeType.equalsIgnoreCase(Constants.BADGE_TYPE_CHANGEMAKER)) {
            achievedBadges = achievedBadgeDao.queryBuilder()
                    .where(AchievedBadgeDao.Properties.BadgeType.eq(badgeType),
                            AchievedBadgeDao.Properties.UserId.eq(MainApplication.getInstance().getUserID()),
                            AchievedBadgeDao.Properties.CategoryStatus.eq(Constants.BADGE_COMPLETED)).list();
            if (achievedBadges.size() > 0) {
                return -1;
            }
        }
        if (badgeType.equalsIgnoreCase(Constants.BADGE_TYPE_CAUSE)) {
            achievedBadges = achievedBadgeDao.queryBuilder()
                    .where(AchievedBadgeDao.Properties.BadgeType.eq(badgeType),
                            AchievedBadgeDao.Properties.CauseId.eq(mCauseData.getId()),
                            AchievedBadgeDao.Properties.UserId.eq(MainApplication.getInstance().getUserID()),
                            AchievedBadgeDao.Properties.CategoryStatus.eq(Constants.BADGE_IN_PROGRESS)).list();
        } else {
            achievedBadges = achievedBadgeDao.queryBuilder()
                    .where(AchievedBadgeDao.Properties.BadgeType.eq(badgeType),
                            AchievedBadgeDao.Properties.UserId.eq(MainApplication.getInstance().getUserID()),
                            AchievedBadgeDao.Properties.CategoryStatus.eq(Constants.BADGE_IN_PROGRESS)).list();
        }
        AchievedBadge achievedBadge;
        long badgeAchieved = -1;


        if (achievedBadges.size() == 0) {
            achievedBadge = new AchievedBadge();
            achievedBadge.setUserId(MainApplication.getInstance().getUserID());
            achievedBadge.setServerId(0);

            if(badgeType.equalsIgnoreCase(BADGE_TYPE_CAUSE))
            achievedBadge.setCategory(mCauseData.getCategoryId());

            achievedBadge.setCauseId(mCauseData.getId());
            achievedBadge.setCauseName(mCauseData.getTitle());
            achievedBadge.setBadgeType(badgeType);
            badgeAchieved = checkBadgeList(badges, distanceCovered, achievedBadge, mCauseData);
        } else {
            achievedBadge = achievedBadges.get(0);
            badgeAchieved = checkBadgeList(badges, distanceCovered, achievedBadge, mCauseData);
        }
        if(achievedBadge!=null)
        achievedBadge.setIsSync(false);

        if (badgeAchieved != 0)
            if (!badgeType.equalsIgnoreCase(Constants.BADGE_TYPE_MARATHON) ||
                    (badgeType.equalsIgnoreCase(Constants.BADGE_TYPE_MARATHON) && badgeAchieved != -1)) {
                if (achievedBadge.getId() != null && achievedBadge.getId() > 0) {
                    achievedBadgeDao.update(achievedBadge);
                } else {
                    achievedBadgeDao.insertOrReplace(achievedBadge);
                }
            }
            if(badgeAchieved>0) {
                for (int i = 0; i < badges.size(); i++) {
                    if (badges.get(i).getBadgeId() == badgeAchieved) {
                        if ((achievedBadge.getParamDone() - distanceCovered) >= badges.get(i).getBadgeParameter()) {
                            badgeAchieved = 0;
                        }
                        break;
                    }
                }
            }
        return badgeAchieved;
    }

    public static long checkBadgeList(List<Badge> badges, double paramDone, AchievedBadge achievedBadge, CauseData mCauseData) {
        int indexAcheived = -1;
        int indexInProgress = -1;
        double totalParamDone = 0;
        if (achievedBadge.getBadgeType().equalsIgnoreCase(Constants.BADGE_TYPE_MARATHON) ||
                achievedBadge.getBadgeType().equalsIgnoreCase(Constants.BADGE_TYPE_STREAK)) {
            totalParamDone = paramDone;
        } else {
            double x = achievedBadge.getParamDone();
            totalParamDone = x + paramDone;
        }
        achievedBadge.setParamDone(totalParamDone);
        long badgeIdAchieved = achievedBadge.getBadgeIdAchieved();
        long badgeIdInProgress = achievedBadge.getBadgeIdInProgress();
        if (!achievedBadge.getBadgeType().equalsIgnoreCase(Constants.BADGE_TYPE_MARATHON)) {
            for (int i = 0; i < badges.size(); i++) {
                Badge badge = badges.get(i);
                if (totalParamDone < badge.getBadgeParameter()) {
                    indexInProgress = i;
                    break;
                }
            }
            if (indexInProgress != -1) {
                if (indexInProgress > 0) {
                    indexAcheived = indexInProgress - 1;
                } else {
                    if (totalParamDone >= badges.get(indexInProgress).getBadgeParameter())
                        indexAcheived = indexInProgress;
                }
            } else {
                indexInProgress = badges.size() - 1;
                indexAcheived = badges.size() - 1;
            }
        } else {
            for (int i = 0; i < badges.size(); i++) {
                Badge badge = badges.get(i);
                if (totalParamDone >= badge.getBadgeParameter()) {
                    indexInProgress = i;
                    indexAcheived = i;
                }
            }
        }
        if (indexInProgress == -1) {
            return 0;
        }
        Badge badge = badges.get(indexInProgress);
        achievedBadge.setBadgeIdInProgress(badge.getBadgeId());
        if (indexAcheived != -1) {
            Badge badgeAcheived = badges.get(indexAcheived);
            achievedBadge.setBadgeIdAchieved(badgeAcheived.getBadgeId());
            achievedBadge.setNoOfStarAchieved(badgeAcheived.getNoOfStars());
            achievedBadge.setBadgeIdAchievedDate(new Date(ServerTimeKeeper.getInstance().getServerTimeAtSystemTime(Calendar.getInstance().getTimeInMillis())));

        } else {
            achievedBadge.setBadgeIdAchieved(0);
            achievedBadge.setNoOfStarAchieved(0);
        }
        String status = Constants.BADGE_IN_PROGRESS;
        switch (achievedBadge.getBadgeType()) {
            case Constants.BADGE_TYPE_CAUSE:
                if (mCauseData.isCompleted()) {
                    status = Constants.BADGE_COMPLETED;
                } else {
                    status = Constants.BADGE_IN_PROGRESS;
                }
                break;
            case Constants.BADGE_TYPE_STREAK:
                if (indexAcheived == indexInProgress) {
                    status = Constants.BADGE_COMPLETED;
                } else {
                    status = Constants.BADGE_IN_PROGRESS;
                }
                achievedBadge.setCauseName(badge.getName());
                break;
            case Constants.BADGE_TYPE_CHANGEMAKER:
                if (indexAcheived == indexInProgress) {
                    status = Constants.BADGE_COMPLETED;
                } else {
                    status = Constants.BADGE_IN_PROGRESS;
                }
                achievedBadge.setCauseName(badge.getName());
                break;
            case Constants.BADGE_TYPE_MARATHON:
                if (indexAcheived == indexInProgress) {
                    status = Constants.BADGE_COMPLETED;
                } else {
                    status = Constants.BADGE_IN_PROGRESS;
                }
                achievedBadge.setCauseName(badge.getName());
                break;
        }

        achievedBadge.setCategoryStatus(status);

        if (badgeIdAchieved != achievedBadge.getBadgeIdAchieved())
            return achievedBadge.getBadgeIdAchieved();
        else
            return -1;
    }

    public static File createImageFile(Context context) throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        return image;
    }

    @SuppressLint("NewApi")
    public static String getRealPathFromURI_API19(Context context, Uri uri) {
        String filePath = "";
        String wholeID = DocumentsContract.getDocumentId(uri);

        // Split at colon, use second item in the array
        String id = wholeID.split(":")[1];

        String[] column = {MediaStore.Images.Media.DATA};

        // where id is equal to
        String sel = MediaStore.Images.Media._ID + "=?";

        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                column, sel, new String[]{id}, null);

        int columnIndex = cursor.getColumnIndex(column[0]);

        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex);
        }
        cursor.close();
        return filePath;
    }


    @SuppressLint("NewApi")
    public static String getRealPathFromURI_API11to18(Context context, Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        String result = null;

        CursorLoader cursorLoader = new CursorLoader(
                context,
                contentUri, proj, null, null, null);
        Cursor cursor = cursorLoader.loadInBackground();

        if (cursor != null) {
            int column_index =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            result = cursor.getString(column_index);
        }
        return result;
    }

    public static boolean isVisible(View view) {
        if (view == null) {
            return false;
        }
        if (!view.isShown()) {
            return false;
        }
        final Rect actualPosition = new Rect();
        view.getGlobalVisibleRect(actualPosition);
        final Rect screen = new Rect(0, 0, getScreenWidth(), getScreenHeight());
        return actualPosition.intersect(screen);
    }

    private static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    private static int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }

    public static boolean storeImage(Bitmap image,String imagePath) {
        File pictureFile = new File(imagePath);
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            image.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.close();

        } catch (FileNotFoundException e) {
            Logger.d(TAG, "File not found: " + e.getMessage());
            return false;
        } catch (IOException e) {
            Logger.d(TAG, "Error accessing file: " + e.getMessage());
            return false;
        }
        return true;
    }
    public static void setGradientBackground(int color1,int color2,View view)
    {
        GradientDrawable gd = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[] {color1,color2});
//        gd.setCornerRadii(new float[]{10,10,0,0,10,10,0,0});
        view.setBackground(gd);
    }

    public static long getMillisElapsedSinceBeginningOfDay(Calendar day) {
        long hour = day.get(Calendar.HOUR_OF_DAY);
        long minute = day.get(Calendar.MINUTE);
        long secs = day.get(Calendar.SECOND);
        long millis = day.get(Calendar.MILLISECOND);
        return hour * 3600000 + minute * 60000 + secs * 1000 + millis;
    }

    public static long getMillisElapsedSinceBeginningOfWeek(Calendar day) {
        long days = day.get(Calendar.DAY_OF_WEEK) - 1;
        long hour = day.get(Calendar.HOUR_OF_DAY);
        long minute = day.get(Calendar.MINUTE);
        long secs = day.get(Calendar.SECOND);
        long millis = day.get(Calendar.MILLISECOND);
        return days * 86400000 + hour * 3600000 + minute * 60000 + secs * 1000 + millis;
    }

    public static long getMillisElapsedSinceBeginningOfMonth(Calendar day) {
        long days = day.get(Calendar.DAY_OF_MONTH) - 1;
        long hour = day.get(Calendar.HOUR_OF_DAY);
        long minute = day.get(Calendar.MINUTE);
        long secs = day.get(Calendar.SECOND);
        long millis = day.get(Calendar.MILLISECOND);
        return days * 86400000 + hour * 3600000 + minute * 60000 + secs * 1000 + millis;
    }

    public static long getEpochForBeginningOfDay(Calendar day) {
        long currentTs = day.getTimeInMillis();
        long millisElapsedSinceBeginning = getMillisElapsedSinceBeginningOfDay(day);
        return currentTs - millisElapsedSinceBeginning;
    }

    public static long getEpochForBeginningOfWeek(Calendar day) {
        long currentTs = day.getTimeInMillis();
        long millisElapsedSinceBeginning = getMillisElapsedSinceBeginningOfWeek(day);
        return currentTs - millisElapsedSinceBeginning;
    }

    public static long getEpochForBeginningOfMonth(Calendar day) {
        long currentTs = day.getTimeInMillis();
        long millisElapsedSinceBeginning = getMillisElapsedSinceBeginningOfMonth(day);
        return currentTs - millisElapsedSinceBeginning;
    }

    public static void addStars(LinearLayout layoutStar, int cause_no_of_stars, Context context,boolean showGreyStar) {
        layoutStar.removeAllViews();
        if(cause_no_of_stars>3)
        {
            LBTextView lbTextView = new LBTextView(context);
            lbTextView.setText(cause_no_of_stars);
            lbTextView.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.star_badges,0);
            layoutStar.addView(lbTextView);
        }else
        {
            if(cause_no_of_stars==0)
            {// to give the layout height of the star
                LBTextView lbTextView = new LBTextView(context);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins(5,0,5,0);
                lbTextView.setLayoutParams(layoutParams);
                if(showGreyStar) {
                    lbTextView.setText("");
                    lbTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.star_badges_grey, 0);
                }else
                {
                    lbTextView.setText("-");
                    lbTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                }
                layoutStar.addView(lbTextView);
            }else
            for(int i=0;i<cause_no_of_stars;i++)
            {
                ImageView imageView = new ImageView(context);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins(5,0,5,0);
                imageView.setLayoutParams(layoutParams);
                imageView.setImageResource(R.drawable.star_badges);
                layoutStar.addView(imageView);
            }
        }
    }

    public static void checkStreak() {
        UserDetails userDetails = MainApplication.getInstance().getUserDetails();
        // just to change the format from dd/MM/yyyy to dd-MM-yyyy
        if(userDetails.getStreakCurrentDate().length()>0)
        {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
            try {
                if(userDetails.getStreakCurrentDate()!=null && !userDetails.getStreakCurrentDate().equals("null")) {
                    Date streakDate = simpleDateFormat.parse(userDetails.getStreakCurrentDate());
                    SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("dd-MM-yyyy");
                    userDetails.setStreakCurrentDate(simpleDateFormat2.format(streakDate));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        AnalyticsEvent.Builder builder = AnalyticsEvent.create(Event.ON_STREAK_CHECK);
        boolean sendStreak = false;
        if(userDetails!=null) {
            try {
                if (userDetails.getStreakCurrentDate() != null
                        && userDetails.getStreakCurrentDate().length() > 0) {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
                    Date streakDate;
                    if(userDetails.getStreakCurrentDate()!=null && !userDetails.getStreakCurrentDate().equals("null"))
                    {
                         streakDate = simpleDateFormat.parse(userDetails.getStreakCurrentDate());
                    }else
                    {
                        streakDate = simpleDateFormat.parse(simpleDateFormat.format(ServerTimeKeeper.getInstance()
                                .getServerTimeAtSystemTime(Calendar.getInstance().getTimeInMillis())));
                    }

                    Date currentDate = simpleDateFormat.parse(simpleDateFormat.format(ServerTimeKeeper.getInstance()
                            .getServerTimeAtSystemTime(Calendar.getInstance().getTimeInMillis())));
                    long diff = currentDate.getTime() - streakDate.getTime();
                    float dayCount = (float) diff / (24 * 60 * 60 * 1000);
                    //set EVENT
                    builder.put("streakDate",streakDate.getTime());
                    builder.put("currentDate",currentDate.getTime());
                    builder.put("diff",diff);
                    builder.put("dayCount",dayCount);
                    builder.put("goal_id",userDetails.getStreakGoalID());
                    builder.put("goal_distance",userDetails.getStreakGoalDistance());
                    builder.put("goal_run_progress",userDetails.getStreakRunProgress());
                    builder.buildAndDispatch();
                    if (!WorkoutSingleton.getInstance().isWorkoutActive()) {
                        if ((dayCount > 1)) {
                            userDetails.setStreakRunProgress(0);
                            userDetails.setStreakCount(0);
                            userDetails.setStreakAdded(false);
                            userDetails.setStreakCurrentDate(Utils.getCurrentDateDDMMYYYY());
                            sendStreak = true;
                        } else if (dayCount == 1) {
                            userDetails.setStreakAdded(false);
                            userDetails.setStreakRunProgress(0);
                        }
                    }
                } else {
                    userDetails.setStreakRunProgress(0);
                    userDetails.setStreakCount(0);
                    userDetails.setStreakAdded(false);
                    userDetails.setStreakCurrentDate(Utils.getCurrentDateDDMMYYYY());
                }
                if (userDetails.getStreakCount() > userDetails.getStreakMaxCount())
                    userDetails.setStreakMaxCount(userDetails.getStreakCount());

                if(userDetails.getStreakRunProgress() == 0)
                {
                    long currentTimeStampMillis = DateUtil.getServerTimeInMillis();
                    Calendar today = Calendar.getInstance();
                    today.setTimeInMillis(currentTimeStampMillis);
                    long begin = Utils.getEpochForBeginningOfDay(today);
                    long end = currentTimeStampMillis;
                    SQLiteDatabase database = MainApplication.getInstance().getDbWrapper().getDaoSession().getDatabase();
                    // Calculate amount_raised in interval
                    Cursor cursor = database.rawQuery("SELECT "
                            + " SUM(" + WorkoutDao.Properties.Distance.columnName + ") AS total_distance"
                            + " FROM " + WorkoutDao.TABLENAME + " where "
                            + WorkoutDao.Properties.IsValidRun.columnName + " is 1" +
                            " and " + WorkoutDao.Properties.BeginTimeStamp.columnName + " between "
                            + begin + " and " + end, new String[]{});
                    cursor.moveToFirst();

                    float run_progress = cursor.getFloat(cursor.getColumnIndex("total_distance"));
                    userDetails.setStreakRunProgress(run_progress);
                    userDetails.addStreakCount();
                }

                MainApplication.getInstance().setUserDetails(userDetails);
                if(sendStreak)
                    SyncHelper.uploadStreak();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Utils.setBadgeForCategory(null,Constants.BADGE_TYPE_STREAK,MainApplication.getInstance().getUserDetails().getStreakCount());
        }
        if(Utils.checkStreakUploaded())
        {
            Utils.setStreakUploaded(true);
            SyncHelper.uploadStreak();
        }
    }

    public static void saveTitleIdToUserDetails(AchievedTitle achievedTitle) {
        UserDetails userDetails = MainApplication.getInstance().getUserDetails();
        TitleDao titleDao = MainApplication.getInstance().getDbWrapper().getTitleDao();
        List<Title> titles = titleDao.queryBuilder()
                .where(TitleDao.Properties.CategoryId.eq(achievedTitle.getCategoryId()))
                .orderAsc(TitleDao.Properties.GoalNStars).list();
        int title1 = userDetails.getTitle1();
        int title2 = userDetails.getTitle2();

        if(userDetails.getTitle1()==0)
        {
            userDetails.setTitle1(achievedTitle.getTitleId());
        }else {
            boolean b = true;
                for (int i = 0; i < titles.size(); i++) {
                    if (titles.get(i).getTitleId() == title1) {
                            userDetails.setTitle1(achievedTitle.getTitleId());
                            b = false;
                            break;
                    }
                }

            if(b && title1!=achievedTitle.getTitleId())
            {
                if(userDetails.getTitle2() == 0)
                {
                    userDetails.setTitle2(achievedTitle.getTitleId());
                }else
                {
                    for (int i = 0; i < titles.size(); i++) {
                        if (titles.get(i).getTitleId() == title2) {
                            userDetails.setTitle2(achievedTitle.getTitleId());
                            break;
                        }
                    }
                }
            }

        }

        MainApplication.getInstance().setUserDetails(userDetails);
    }

    public static void setStarImage(int starCount, ImageView starImageView) {
        switch (starCount) {
            case 1 :
                starImageView.setImageResource(R.drawable.star_1);
                break;
            case 2 :
                starImageView.setImageResource(R.drawable.star_2);
                break;
            case 3 :
                starImageView.setImageResource(R.drawable.star_3);
                break;
        }
    }

    public static String dateToString(Date date) {
        String dateString = null;
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            dateString = simpleDateFormat.format(date);
        }catch (Exception e)
        {
            e.printStackTrace();
        }

        return dateString;
    }

    public static Date stringToDate(String dateString) {
        Date date = null;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        try {
            if(dateString!=null && !dateString.equals("null"))
            date = simpleDateFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static int dpToPx(int dp)
    {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int pxToDp(int px)
    {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }
}
