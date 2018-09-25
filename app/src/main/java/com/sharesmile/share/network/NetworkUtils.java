package com.sharesmile.share.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.core.Logger;
import com.sharesmile.share.core.config.Urls;
import com.sharesmile.share.core.event.UpdateEvent;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

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

    public static <T> T parseSuccessResponse(com.sharesmile.share.core.Response serverResponse, Class<T> tClass) throws NetworkException {

        Gson gson = new Gson();
        switch (serverResponse.getCode()) {
            case Constants.SUCCESS_GET:
                return gson.fromJson(serverResponse.getResponse().toString(), tClass);
            case Constants.SUCCESS_POST:
                return gson.fromJson(serverResponse.getResponse().toString(), tClass);

        }
        try{
            return gson.fromJson(serverResponse.getResponse().toString(), tClass);
        }catch(JsonSyntaxException jse){
            String message = "JsonSyntaxException while parsing response string to " + tClass.getSimpleName()
                    + ", responseString: " + serverResponse.getResponse().toString();
            Logger.e(TAG, message, jse);
            throw new NetworkException.Builder().cause(jse)
                    .httpStatusCode(serverResponse.getCode())
                    .errorMessage(message).build();
        }
    }

    public static <T> T parseSuccessResponse(Response serverResponse, Class<T> tClass) throws NetworkException {

        Gson gson = new Gson();
        try {
            return gson.fromJson(getStringResponse(serverResponse), tClass);
        } catch (JsonSyntaxException jse) {
            String message = "JsonSyntaxException while parsing response string to " + tClass.getSimpleName();
            Logger.e(TAG, message, jse);
            throw new NetworkException.Builder().cause(jse)
                    .httpStatusCode(serverResponse.code())
                    .errorMessage(message).build();
        }
    }

    public static <T> T parseSuccessResponse(com.sharesmile.share.core.Response serverResponse, Type typeOfT) throws NetworkException {
        Gson gson = new Gson();
        return gson.fromJson(serverResponse.getResponse().toString(), typeOfT);
    }

    private static NetworkException convertResponseToException(Response response, com.sharesmile.share.core.Response serverResponse) {
        String requestUrl = response.request().url().toString();
        String method = response.request().method();
        int responseCode = response.code();
        String messageFromServer = response.message();
        /*try {
            messageFromServer = response.body().string();
        }catch (IOException ioe){
            Logger.e(TAG, "Can't fetch response body");
        }*/

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
        String errorResponse = "";
        if (serverResponse != null) {
            try {
                JSONObject jsonObject = new JSONObject(serverResponse.getErrors().toString());
                if (jsonObject.has("msg")) {
                    errorResponse = jsonObject.getString("msg");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return new NetworkException.Builder().errorMessage(why).failureType(failureType)
                .httpStatusCode(response.code())
                .messageFromServer(messageFromServer).errorResponse(errorResponse)
                .build();
    }

    public static <T> T handleResponse(Response response, Class<T> mWrapperClass) throws NetworkException {
        T wrapperObj = null;
        if (null != response) {
            String requestUrl = response.request().url().toString();
            if (requestUrl.startsWith(Urls.getGoogleConvertTokenUrl())) {
                if (response.isSuccessful()) {
                    wrapperObj = parseSuccessResponse(response, mWrapperClass);
                    return wrapperObj;
                } else {
                    //Failure Scenarios
                    throw convertResponseToException(response, null);
                }
            } else {
                com.sharesmile.share.core.Response serverResponse = getResponseFromServerResponse(response);
                if (response.isSuccessful()) {
                    wrapperObj = parseSuccessResponse(serverResponse, mWrapperClass);
                    return wrapperObj;
                } else {
                    //Failure Scenarios
                    EventBus.getDefault().post(new UpdateEvent.OnErrorResponse(serverResponse));
                    throw convertResponseToException(response, serverResponse);
                }
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
            com.sharesmile.share.core.Response serverResponse = getResponseFromServerResponse(response);

            if (response.isSuccessful()) {
                wrapperObj = parseSuccessResponse(serverResponse, typeOfT);
                return wrapperObj;
            } else {
                //Failure Scenarios
                EventBus.getDefault().post(new UpdateEvent.OnErrorResponse(serverResponse));
                throw convertResponseToException(response, serverResponse);
            }
        } else {
            //response obtained from OkHttp is null
            String why = "Null Response obtained from URL: " + response.request().url();
            throw new NetworkException.Builder().errorMessage(why).failureType(FailureType.RESPONSE_FAILURE)
                    .build();
        }
    }

    private static com.sharesmile.share.core.Response getResponseFromServerResponse(Response response) throws NetworkException {

        Gson gson = new Gson();
        String responseString = getStringResponse(response);
        return gson.fromJson(responseString, com.sharesmile.share.core.Response.class);

    }

    public static NetworkException wrapIOException(Request request, IOException ioe){
        String requestUrl = request.url().toString();
        String method = request.method();
        return new NetworkException.Builder().cause(ioe).failureType(FailureType.REQUEST_FAILURE)
                                             .errorMessage(method + " request failed for URL: " +
                                                requestUrl).build();
    }

}