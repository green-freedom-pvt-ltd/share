package com.sharesmile.share.network;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.sharesmile.share.MainApplication;
import com.sharesmile.share.core.UnObfuscable;
import com.sharesmile.share.utils.Logger;
import com.sharesmile.share.utils.NameValuePair;
import com.sharesmile.share.utils.Utils;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by ankitmaheshwari1 on 08/01/16.
 */
public class NetworkDataProvider {

    public static final String HEADER_APP_NAME = "app_name";
    public static final String HEADER_APP_VERSION = "app_version";
    public static final String HEADER_LOGIN_AUTH_TOKEN = "login_auth_token";
    public static final String CONTENT_TYPE_TAG = "Content-Type";
    public static final String HTTP_HEADER_JSON = "application/json";
    public static final String HTTP_HEADER_FORM_URLENCODED = "application/x-www-form-urlencoded";
    public static final String ENCODING = "charset=utf-8";
    public static final long CONNECTION_TIMEOUT_POST_VALUE = 20;
    public static final long READ_TIMEOUT_POST_VALUE = 20;
    private static final MediaType JSON = MediaType.parse(HTTP_HEADER_JSON + "; " +
            ENCODING);
    private static final MediaType URLENCODED = MediaType.parse(HTTP_HEADER_FORM_URLENCODED + "; " +
            ENCODING);

    private static OkHttpClient myOkHttpClient;

    private static final String TAG = "NetworkDataProvider";

    /**
     * Class based get call. Use this when expecting the response as plain array of json
     *
     * @param url           to be hit
     * @param responseClass class of the response object
     * @param <T>           response object type
     * @return ServerResponse object wrapping desired object of type T
     * @throws NetworkException while handling you must check for httpStatusCode
     */

    public static <T> T doGetCall(String url, Class<T> responseClass)
            throws NetworkException {
        Response response = getResponseForGetCall(url);
        T responseObject = NetworkUtils.handleResponse(response, responseClass);
        return responseObject;
    }


    public static <T> T doGetCall(String url, Map<String, String> queryParamsMap,
                                  Class<T> responseClass) throws NetworkException {
        String modifiedUrl = getUrlWithParams(url, queryParamsMap);
        return doGetCall(modifiedUrl, responseClass);
    }

    /**
     * Type based get call. Use this when expecting the response as plain array of json
     *
     * @param url     to be hit
     * @param typeOfT Custom Type of the response object
     * @param <T>     response object generic
     * @return ServerResponse object wrapping desired object of type responseClass
     */
    public static <T> T doGetCall(String url, Type typeOfT)
            throws NetworkException {
        Response response = getResponseForGetCall(url);
        T responseObject = NetworkUtils.handleResponse(response, typeOfT);
        return responseObject;
    }

    /**
     * Type based get call. Use this when expecting the response as plain array of json
     *
     * @param url     to be hit
     * @param typeOfT Custom Type of the response object
     * @param <T>     response object type
     * @return ServerResponse object wrapping desired object of type typeOfT
     */
    public static <T> T doGetCall(String url, Map<String, String> queryParamsMap,
                                  Type typeOfT) throws NetworkException {
        String modifiedUrl = getUrlWithParams(url, queryParamsMap);
        return doGetCall(modifiedUrl, typeOfT);
    }

    /**
     * Makes Get request synchronously and return the response as plain JSON string
     *
     * @param url to be hit
     * @throws NetworkException while handling you must check for httpStatusCode
     */
    public static String getStringResponseForGetCall(String url) throws NetworkException {
        try {
            return getResponseForGetCall(url).body().string();
        } catch (IOException e) {
            NetworkException.Builder builder
                    = new NetworkException.Builder();
            builder.errorMessage("IOException while returning string response");
            builder.cause(e);
            throw builder.build();
        }
    }

    private static <T> T doPostCall(String url, RequestBody body,
                                    Class<T> responseClass) throws NetworkException {
        if (TextUtils.isEmpty(url)) {
            throw new IllegalArgumentException("Empty URL " + url);
        }
        Logger.d(TAG, "Url for POST request: " + url);
        Response response = getResponseForPostCall(url, body);
        T responseObject = NetworkUtils.handleResponse(response, responseClass);
        return responseObject;
    }

    private static <T> T doPostCall(String url, RequestBody body,
                                    Type typeOfT) throws NetworkException {
        if (TextUtils.isEmpty(url)) {
            throw new IllegalArgumentException("Empty URL " + url);
        }
        Logger.d(TAG, "Url for POST request: " + url);
        Response response = getResponseForPostCall(url, body);
        T responseObject = NetworkUtils.handleResponse(response, typeOfT);
        return responseObject;
    }

    /**
     * Makes post request synchronously using form data
     *
     * @param url           baseUrl of the endpoint
     * @param formData      list of NameValuePairs containing post data
     * @param responseClass class of the response object
     * @param <T>           response object type
     * @return ServerResponse object wrapping desired object of type T
     * @throws NetworkException while handling you must check for httpStatusCode
     */
    public static <T> T doPostCall(String url, List<NameValuePair> formData,
                                   Class<T> responseClass) throws NetworkException {
        return doPostCall(url, convertFormDataToBody(formData), responseClass);
    }

    public static <T> T doPostCall(String url, JSONObject jsonData,
                                   Class<T> responseClass) throws NetworkException {
        return doPostCall(url, convertJSONdataToBody(jsonData), responseClass);
    }

    public static <T> T doPostCall(String url, List<NameValuePair> formData,
                                   Type typeOfT) throws NetworkException {
        return doPostCall(url, convertFormDataToBody(formData), typeOfT);
    }

    public static <T> T doPostCall(String url, JSONObject jsonData,
                                   Type typeOfT) throws NetworkException {
        return doPostCall(url, convertJSONdataToBody(jsonData), typeOfT);
    }

    public static <T> T doPutCall(String url, JSONObject jsonData,
                                  Class<T> responseClass) throws NetworkException {
        return doPutCall(url, convertJSONdataToBody(jsonData), responseClass);
    }

    private static <T> T doPutCall(String url, RequestBody body,
                                   Class<T> responseClass) throws NetworkException {
        if (TextUtils.isEmpty(url)) {
            throw new IllegalArgumentException("Empty URL " + url);
        }
        Logger.d(TAG, "Url for put request: " + url);
        Response response = getResponseForPutCall(url, body);
        T responseObject = NetworkUtils.handleResponse(response, responseClass);
        return responseObject;
    }

    private static RequestBody convertFormDataToBody(List<NameValuePair> formData) {
        RequestBody body = null;
        if (Utils.isCollectionFilled(formData)) {
            MultipartBuilder bodyData = new MultipartBuilder().type(MultipartBuilder.FORM);
            for (NameValuePair pair : formData) {
                bodyData.addFormDataPart(pair.getName(), pair.getValue());
            }

            body = bodyData.build();
        } else {
            MultipartBuilder bodyData = new MultipartBuilder().type(MultipartBuilder.FORM);
            bodyData.addFormDataPart("", "");
            body = bodyData.build();
        }
        return body;
    }

    private static RequestBody convertJSONdataToBody(JSONObject jsonData) {
        RequestBody body;
        if (jsonData != null) {
            body = RequestBody.create(JSON, jsonData.toString());
        } else {
            body = RequestBody.create(JSON, "");
        }
        return body;
    }


    /**
     * Returns the raw response for GET call
     */
    private static Response getResponseForGetCall(String url)
            throws NetworkException {
        if (TextUtils.isEmpty(url)) {
            throw new IllegalArgumentException("Empty URL " + url);
        }
        Request.Builder requestBuilder = new Request.Builder().url(url);
        if (MainApplication.isLogin()) {
            requestBuilder.addHeader("Authorization", "Bearer " + MainApplication.getInstance().getToken());
        }
        Request request = requestBuilder.build();
        Call call = getSingleOkHttpClient().newCall(request);
        Logger.d(TAG, "Url for GET request: " + url);
        try {
            return call.execute();
        } catch (IOException e) {
            throw NetworkUtils.wrapIOException(request, e);
        }
    }

    private static Response getResponseForPostCall(String url, RequestBody body)
            throws NetworkException {
        Request.Builder builder = new Request.Builder().url(url)
                .addHeader(CONTENT_TYPE_TAG, HTTP_HEADER_JSON)
                .post(body);
        if (MainApplication.isLogin()) {
            builder.addHeader("Authorization", "Bearer " + MainApplication.getInstance().getToken());
        }
        Request request = builder.build();
        Call call = getSingleOkHttpClient().newCall(request);
        Logger.d(TAG, "Url for POST request: " + url + " Header " + request.headers().toString());
        try {
            return call.execute();
        } catch (IOException ioe) {
            throw NetworkUtils.wrapIOException(request, ioe);
        }
    }

    private static Response getResponseForPutCall(String url, RequestBody body)
            throws NetworkException {
        Request.Builder builder = new Request.Builder().url(url)
                .addHeader(CONTENT_TYPE_TAG, HTTP_HEADER_JSON)
                .put(body);
        if (MainApplication.isLogin()) {
            builder.addHeader("Authorization", "Bearer " + MainApplication.getInstance().getToken());
        }
        Request request = builder.build();
        Call call = getSingleOkHttpClient().newCall(request);
        Logger.d(TAG, "Url for Put request: " + url + " Header " + request.headers().toString());
        try {
            return call.execute();
        } catch (IOException ioe) {
            throw NetworkUtils.wrapIOException(request, ioe);
        }
    }

    public static <R extends UnObfuscable> void doPostCallAsync(String url, JSONObject requestJSON,
                                                                NetworkAsyncCallback<R> cb) {
        RequestBody body = RequestBody.create(JSON, requestJSON.toString());
        Request.Builder requestBuilder = new Request.Builder().url(url)
                .header(CONTENT_TYPE_TAG, HTTP_HEADER_JSON)
                .post(body);

        if (MainApplication.isLogin()) {
            requestBuilder.addHeader("Authorization", "Bearer " + MainApplication.getInstance().getToken());
        }
        Request request = requestBuilder.build();
        Call call = getSingleOkHttpClient().newCall(request);
        call.enqueue(cb);
    }

    public static <R extends UnObfuscable> void doPostCallAsync(String url, List<NameValuePair> data,
                                                                NetworkAsyncCallback<R> cb) {
        RequestBody body = RequestBody.create(URLENCODED, convertToData(data));
        Request.Builder requestBuilder = new Request.Builder().url(url)
                .post(body);

        if (MainApplication.isLogin()) {
            requestBuilder.addHeader("Authorization", "Bearer " + MainApplication.getInstance().getToken());
        }
        Request request = requestBuilder.build();
        Call call = getSingleOkHttpClient().newCall(request);
        call.enqueue(cb);
    }

    public static <R extends UnObfuscable> void doPostCallAsyncWithFormData(String url, List<NameValuePair> data,
                                                                            NetworkAsyncCallback<R> cb) {
        RequestBody body = convertFormDataToBody(data);
        Request.Builder requestBuilder = new Request.Builder().url(url)
                .post(body);

        if (MainApplication.isLogin()) {
            requestBuilder.addHeader("Authorization", "Bearer " + MainApplication.getInstance().getToken());
        }
        Request request = requestBuilder.build();
        Call call = getSingleOkHttpClient().newCall(request);
        call.enqueue(cb);
    }

    public static <R extends UnObfuscable> void doPutCallAsyncWithForData(String url, List<NameValuePair> data,
                                                                          NetworkAsyncCallback<R> cb) {
        RequestBody body = convertFormDataToBody(data);
        Request.Builder requestBuilder = new Request.Builder().url(url)
                .put(body);
        if (MainApplication.isLogin()) {
            requestBuilder.addHeader("Authorization", "Bearer " + MainApplication.getInstance().getToken());
        }
        Request request = requestBuilder.build();
        Call call = getSingleOkHttpClient().newCall(request);
        call.enqueue(cb);
    }

    private static String convertToData(List<NameValuePair> data) {

        String body = "";
        for (NameValuePair pair : data) {
            String value = "";
            try {
                value = URLEncoder.encode(pair.getValue(), "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return body;
            }
            if (!TextUtils.isEmpty(body)) {
                body = body + "&";
            }
            body = body + pair.getName() + "=" + value;

        }
        return body;
    }

    public static <R extends UnObfuscable> void doGetCallAsync(String url, NetworkAsyncCallback<R> cb) {
        Request.Builder requestBuilder = new Request.Builder().url(url);
        if (MainApplication.isLogin()) {
            requestBuilder.addHeader("Authorization", "Bearer " + MainApplication.getInstance().getToken());
        }
        Request request = requestBuilder.build();

        Call call = getSingleOkHttpClient().newCall(request);
        call.enqueue(cb);
    }

    public static <R extends UnObfuscable> void doGetCallAsync(String url, Map<String, String> header, Callback cb) {
        Request.Builder requestBuilder = new Request.Builder().url(url);

        if (header != null) {
            for (Map.Entry<String, String> entry : header.entrySet()) {
                System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
                requestBuilder.addHeader(entry.getKey(), entry.getValue());
            }
        }
        Request request = requestBuilder.build();
        Call call = getSingleOkHttpClient().newCall(request);
        call.enqueue(cb);
    }

    private static String getUrlWithParams(String baseUrl, Map<String, String> queryParamsMap) {
        Uri.Builder builder = Uri.parse(baseUrl).buildUpon();
        if (queryParamsMap == null || queryParamsMap.isEmpty()) {
            return builder.build().toString();
        }
        for (String key : queryParamsMap.keySet()) {
            String value = queryParamsMap.get(key);
            if (!TextUtils.isEmpty(value)) {
                builder.appendQueryParameter(key, value);
            }
        }
        return builder.build().toString();
    }

    public static synchronized OkHttpClient getSingleOkHttpClient() {
        if (myOkHttpClient == null) {
            myOkHttpClient = new OkHttpClient();
            myOkHttpClient.setConnectTimeout(CONNECTION_TIMEOUT_POST_VALUE, TimeUnit.SECONDS);
            myOkHttpClient.setReadTimeout(READ_TIMEOUT_POST_VALUE, TimeUnit.SECONDS);
        }
        return myOkHttpClient;
    }

}
