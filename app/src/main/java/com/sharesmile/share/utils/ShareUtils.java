package com.sharesmile.share.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.internal.CallbackManagerImpl;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.sharesmile.share.R;

/**
 * Created by Shine on 16/01/17.
 */

public class ShareUtils {

    public static Intent shareOnWhatsAppIntent(Context context, String message, Uri imageUrl) {

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, message);
        sendIntent.setPackage("com.whatsapp");

        //
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, message);
        sendIntent.putExtra(Intent.EXTRA_STREAM, imageUrl);
        sendIntent.setType("image/*");
        return sendIntent;

    }

    public static void shareOnFb(Activity activity, String message, Uri imageUrl) {

        FacebookSdk.sdkInitialize(activity.getApplicationContext());
        ShareLinkContent content = new ShareLinkContent.Builder()
                .setContentUrl(Uri.parse(activity.getString(R.string.url_play_store_with_utm)))
                .setContentTitle(activity.getString(R.string.app_name))
                .setContentDescription(message)
                .setImageUrl(imageUrl)
                .build();


        ShareDialog shareDialog = new ShareDialog(activity);
        shareDialog.registerCallback(new CallbackManagerImpl(), new FacebookCallback<Sharer.Result>() {
            @Override
            public void onSuccess(Sharer.Result result) {
                //Toast.makeText(getActivity(),"Success",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancel() {
                //  Toast.makeText(getActivity(),"Failed",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {

            }
        });
        shareDialog.show(content);
    }

}
