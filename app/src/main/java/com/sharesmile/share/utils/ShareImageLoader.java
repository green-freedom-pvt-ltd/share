package com.sharesmile.share.utils;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import com.sharesmile.share.MainApplication;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import java.io.IOException;

/**
 * Created by ankitmaheshwari1 on 30/12/15.
 */
public class ShareImageLoader {

    private static final String TAG = "ShareImageLoader";

    private static final Bitmap.Config BITMAP_CONFIG = Bitmap.Config.RGB_565;
    private static final MemoryPolicy MEMORY_POLICY = MemoryPolicy.NO_CACHE;
    private static final boolean USE_MEMORY_CACHE = true;
    private static final float LOW_MEMORY_THRESHOLD_PERCENTAGE = 5;
    private final Picasso picasso;


    private static ShareImageLoader instance;

    private ShareImageLoader() {
        picasso = getImageLoader();
    }

    public static synchronized ShareImageLoader getInstance() {
        if (instance == null) {
            instance = new ShareImageLoader();
        }
        return instance;
    }

    private Picasso getImageLoader() {

        OkHttpClient picassoClient = new OkHttpClient();
        picassoClient.interceptors().add(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {

                Request newRequest = chain.request().newBuilder().addHeader("Accept", "image/webp")
                        .build();
                return chain.proceed(newRequest);
            }
        });
        return new Picasso.Builder(MainApplication.getContext())
                .downloader(new OkHttpDownloader(picassoClient)).build();
    }


    /**
     * Downloads image from given URL and load it in ImageView
     *
     * @param url       URL string
     * @param imageView ImageView for loading image
     */
    public void loadImage(String url, ImageView imageView) {
        loadImage(url, imageView, null, null);
    }

    /**
     * Downloads image from given URL and load it in ImageView
     *
     * @param url            URL string
     * @param imageView      ImageView for loading image
     * @param backupDrawable displayed while image is downloading and if download fails
     */
    public void loadImage(String url, ImageView imageView, Drawable backupDrawable) {
        loadImage(url, imageView, backupDrawable, backupDrawable);
    }


    /**
     * Downloads image from given URL and load it in ImageView
     *
     * @param url                  URL string
     * @param imageView            ImageView for loading image
     * @param drawableWhileLoading displayed while image is downloading
     * @param drawableOnFailure    displayed if download fails
     */
    public void loadImage(String url, ImageView imageView, Drawable drawableWhileLoading,
                          Drawable drawableOnFailure) {
        if (isAdequateMemoryAvailable()) {
            if (imageView != null && !TextUtils.isEmpty(url)) {
                RequestCreator request = picasso.load(url);
                if (drawableWhileLoading != null) {
                    request.placeholder(drawableWhileLoading);
                }
                if (drawableOnFailure != null) {
                    request.error(drawableOnFailure);
                }
                request.config(BITMAP_CONFIG);
                if (!USE_MEMORY_CACHE) {
                    request.memoryPolicy(MEMORY_POLICY);
                }
                request.into(imageView);
                return;
            }
        }
        if (imageView != null && drawableOnFailure != null) {
            imageView.setImageDrawable(drawableOnFailure);
        }

    }

    /**
     * Downloads image from given URL and load it in square ImageView with given dimension
     *
     * @param url       URL string
     * @param imageView ImageView for loading image
     */
    public void loadSquareImage(String url, ImageView imageView, Drawable backupDrawable,
                                int dimension) {
        if (isAdequateMemoryAvailable()) {
            if (imageView != null && !TextUtils.isEmpty(url)) {
                RequestCreator request = picasso.load(url).resize(dimension, dimension)
                        .centerCrop();
                if (backupDrawable != null) {
                    request.placeholder(backupDrawable).error(backupDrawable);
                }
                request.config(BITMAP_CONFIG);
                if (!USE_MEMORY_CACHE) {
                    request.memoryPolicy(MEMORY_POLICY);
                }
                request.into(imageView);
            } else {
                if (imageView != null && backupDrawable != null) {
                    imageView.setImageDrawable(backupDrawable);
                }
            }
        } else {
            if (imageView != null && backupDrawable != null) {
                imageView.setImageDrawable(backupDrawable);
            }
        }
    }

    /**
     * Loads and decodes image synchronously
     * Default display image options from provided configuration will be used
     *
     * @return Result image Bitmap. Can be <b>null</b> if image loading/decoding was failed or
     * cancelled.
     */
    public Bitmap loadImageSync(String imageUrl) {
        if (TextUtils.isEmpty(imageUrl)) {
            return null;
        }

        Bitmap bitmap = null;
        try {
            bitmap = picasso.load(imageUrl).get();
        } catch (IOException e) {
            Logger.e(TAG, "IOException during synchronous image download from URL");
            e.printStackTrace();
        }
        return bitmap;
    }

    private boolean isAdequateMemoryAvailable() {

        boolean isAdequateMemoryAvailable = true;
        // Get app memory info
        long total = Runtime.getRuntime().maxMemory();
        long used = Runtime.getRuntime().totalMemory();
        float percentAvailable = 100f * (1f - ((float) used / total));
        if (percentAvailable <= LOW_MEMORY_THRESHOLD_PERCENTAGE) {
            Logger.e(TAG, "LOW MEMORY, ONLY " + percentAvailable + "% AVAILABLE!");
            isAdequateMemoryAvailable = false;
            handleLowMemory();
        }

        return isAdequateMemoryAvailable;
    }

    private void handleLowMemory() {
        // handle low memory
        Logger.d(TAG,"HANDLING LOW MEMORY");
        System.gc();
    }

}
