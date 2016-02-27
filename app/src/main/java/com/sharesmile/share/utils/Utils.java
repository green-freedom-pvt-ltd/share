package com.sharesmile.share.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.ImageView;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sharesmile.share.core.Constants;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by ankitmaheshwari1 on 08/01/16.
 */
public class Utils {

    private static final String TAG = "Utils";

    /* a utility to validate Indian phone number example - 03498985532, 5389829422 **/
    public static boolean isValidPhoneNumber(String number) {
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
     gets screen height in pixels, Application Context should be used
     */
    public static int getScreenHeightUsingDisplayMetrics(Context context) {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(displaymetrics);
        return displaymetrics.heightPixels;
    }

    /**
     gets screen width in pixels, Application Context should be used
     */
    public static int getScreenWidthUsingDisplayMetrics(Context context) {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(displaymetrics);
        return displaymetrics.widthPixels;
    }

    public static float convertDpToPixel(Context context, float dp) {
        Context localContext = context;
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
        if (isCollectionFilled(points) && points.size() >= 2){

            String staticMapUrl = Constants.STATIC_GOOGLE_MAP_BASE_URL + "size=" + width + "x" + height
                    + Constants.STATIC_GOOGLE_MAP_COMMON_PARAMS
                    + "&scale=" + (isScreenTooLarge(staticMapView.getContext()) ? 2 : 1)
                    + getMarkerParams(points.get(0), points.get(points.size() - 1))
                    + getPathParams(points)
                    + "&key=" + Constants.STATIC_GOOGLE_MAP_API_KEY;
            Logger.i(TAG,"Hitting Static Map API with URL: " + staticMapUrl);
            ShareImageLoader.getInstance().loadImage(staticMapUrl, staticMapView);
        }
    }

    public static boolean isScreenTooLarge(Context context){
        int screenLayoutWithMask = context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_LAYOUTDIR_MASK;
        switch (screenLayoutWithMask){
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

    public static String getMarkerParams(LatLng startPoint, LatLng endPoint){
        String firstMarker = "color:blue|label:S|" + startPoint.latitude + "," + startPoint.longitude;
        String secondMarker = "color:red|label:E|" + endPoint.latitude + "," + endPoint.longitude;
        try {
            return "&markers=" + URLEncoder.encode(firstMarker, "UTF-8") + "&markers="
                    + URLEncoder.encode(secondMarker, "UTF-8");
        }catch (UnsupportedEncodingException usee){
            Logger.e(TAG, usee.getMessage(), usee);
        }
        return "";
    }

    public static String getPathParams(List<LatLng> points){
        String prefix = "&path=";
        StringBuilder sb = new StringBuilder();
        sb.append("color:0x00ff0080|weight:6");
        for (LatLng point : points){
            sb.append("|").append(point.latitude).append(",").append(point.longitude);
        }
        try {
            return prefix + URLEncoder.encode(sb.toString(), "UTF-8");
        }catch (UnsupportedEncodingException usee){
            Logger.e(TAG, usee.getMessage(), usee);
        }
        return "";
    }
}
