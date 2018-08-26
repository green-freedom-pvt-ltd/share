package com.sharesmile.share.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.core.Logger;

import java.io.IOException;
import java.lang.reflect.Type;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import okhttp3.Request;
import okhttp3.Response;

/**
 Contains static methods to make POST and GET requests to REST API endpoints

 @author ravikiransahajan parth ankitmaheshwari */

public class NetworkUtils {

    private static final String TAG = "NetworkUtils";

    public static final String UTF_8 = "UTF-8";
    public static final String ZERO = "0";
    public static final String MD5 = "MD5";
    public static final int UNAUTHORISED_CODE = 401;
    public static final int TOKEN_EXPIRED_CODE = 498;


    /**
     This method returns the status of data connectivity of your phone.
     */
    public static String md5(final String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = MessageDigest.getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2) {
                    h = ZERO + h;
                }
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(
                                                                              Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return (activeNetwork != null && activeNetwork.isConnectedOrConnecting());
    }


    public static boolean isWifiConnected(Context context) {
        ConnectivityManager connManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (mWifi.isConnected()) {
            return true;
        } else {
            return false;
        }
    }

    public static int getNetworkType(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(
                Context.TELEPHONY_SERVICE);
        return tm.getNetworkType();
    }

    private static String getStringResponse(Response response) throws NetworkException{
        try{
            //TODO : hack
               /* Object obj = new JSONParser().parse(response.body().string());
                if (obj instanceof JSONObject) {
                    JSONObject jsonObject = (JSONObject) obj;
                    if (jsonObject.containsKey("code")) {
                        return jsonObject.get("result").toString();
                    } else {
                        return jsonObject.toString();
                    }
                } else {
                    JSONArray ja = (JSONArray) obj;
                    return ja.toString();
                }*/
            return  response.body().string();
        }catch (IOException ioe){
            String message = "Exception while converting response body to string: " + ioe.getMessage();
            Logger.e(TAG, message, ioe);
            throw new NetworkException.Builder().cause(ioe).httpStatusCode(response.code())
                    .errorMessage(message).build();
        }
    }

    public static <T> T parseSuccessResponse(Response response, Class<T> tClass) throws NetworkException{

        Gson gson = new Gson();
        String responseString = getStringResponse(response);
        com.sharesmile.share.core.Response serverResponse = gson.fromJson(responseString, com.sharesmile.share.core.Response.class);
        switch (serverResponse.getCode()) {
            case Constants.SUCCESS_GET:
                return gson.fromJson(serverResponse.getResponse().toString(), tClass);
            case Constants.SUCCESS_POST:

                break;

        }
        try{
            return gson.fromJson(responseString, tClass);
        }catch(JsonSyntaxException jse){
            String message = "JsonSyntaxException while parsing response string to " + tClass.getSimpleName()
                    + ", responseString: " + responseString;
            Logger.e(TAG, message, jse);
            throw new NetworkException.Builder().cause(jse)
                    .httpStatusCode(response.code())
                    .errorMessage(message).build();
        }
    }

    public static <T> T parseSuccessResponse(Response response, Type typeOfT) throws NetworkException{

        Gson gson = new Gson();
        String responseString = getStringResponse(response);
        com.sharesmile.share.core.Response serverResponse = gson.fromJson(responseString, com.sharesmile.share.core.Response.class);
        switch (serverResponse.getCode()) {
            case Constants.SUCCESS_GET:
                return gson.fromJson(serverResponse.getResponse().toString(), typeOfT);
            case Constants.SUCCESS_POST:

                break;

        }
        try{
            return gson.fromJson(responseString, typeOfT);
        }catch(JsonSyntaxException jse){
            String message = "JsonSyntaxException while parsing response string to " + typeOfT.toString()
                    + ", responseString: " + responseString;
            Logger.e(TAG, message, jse);
            throw new NetworkException.Builder().cause(jse).httpStatusCode(response.code()).errorMessage(message).build();
        }
    }

    private static NetworkException convertResponseToException(Response response) {
        String requestUrl = response.request().url().toString();
        String method = response.request().method();
        int responseCode = response.code();
        String messageFromServer = response.message();
        try {
            messageFromServer = response.body().string();
        }catch (IOException ioe){
            Logger.e(TAG, "Can't fetch response body");
        }

        String why = null;
        int failureType;
        if (TOKEN_EXPIRED_CODE == responseCode) {
            // Login auth token expired, special handling
            why = "Login Auth Token expired";
            failureType = FailureType.TOKEN_EXPIRED;
        } else {
            why = method + " response not successful for URL: " + requestUrl;
            failureType = FailureType.RESPONSE_FAILURE;
        }
        return new NetworkException.Builder().errorMessage(why).failureType(failureType)
                .httpStatusCode(response.code())
                .messageFromServer(messageFromServer)
                .build();
    }

    public static <T> T handleResponse(Response response, Class<T> mWrapperClass) throws NetworkException {
        T wrapperObj = null;
        if (null != response) {
            if (response.isSuccessful()) {
                wrapperObj = parseSuccessResponse(response, mWrapperClass);
                return wrapperObj;
            } else {
                //Failure Scenarios
                throw convertResponseToException(response);
            }
        } else {
            //response obtained from OkHttp is null
            String why = "Null Response obtained from URL: " + response.request().url();
            throw new NetworkException.Builder().errorMessage(why).failureType(FailureType.RESPONSE_FAILURE)
                                                .build();
        }
    }

    public static <T> T handleResponse(Response response, Type typeOfT) throws NetworkException {
        T wrapperObj = null;
        if (null != response) {
            if (response.isSuccessful()) {
                wrapperObj = parseSuccessResponse(response, typeOfT);
                return wrapperObj;
            } else {
                //Failure Scenarios
                throw convertResponseToException(response);
            }
        } else {
            //response obtained from OkHttp is null
            String why = "Null Response obtained from URL: " + response.request().url();
            throw new NetworkException.Builder().errorMessage(why).failureType(FailureType.RESPONSE_FAILURE)
                    .build();
        }
    }

    public static NetworkException wrapIOException(Request request, IOException ioe){
        String requestUrl = request.url().toString();
        String method = request.method();
        return new NetworkException.Builder().cause(ioe).failureType(FailureType.REQUEST_FAILURE)
                                             .errorMessage(method + " request failed for URL: " +
                                                requestUrl).build();
    }

}