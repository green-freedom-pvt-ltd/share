package com.sharesmile.share.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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

    public static Intent shareOnWhatsAppIntent(String message, Uri imageUrl) {

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.setPackage("com.whatsapp");
        //
        sendIntent.putExtra(Intent.EXTRA_TEXT, message);
        if(imageUrl!=null) {
            sendIntent.putExtra(Intent.EXTRA_STREAM, imageUrl);
            sendIntent.setType("image/*");
        }else
        {
            sendIntent.setType("text/plain");
        }
        return sendIntent;
    }

    public static Intent shareOnTwitter(String message)
    {
        String tweetUrl = "https://twitter.com/intent/tweet?text="+message+"&url="
                + "onelink.to/impact";
        Uri uri = Uri.parse(tweetUrl);
        Intent tweet = new Intent(Intent.ACTION_VIEW,uri);
        return tweet;
    }

    public static Intent shareOnGmail(String message)
    {
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_SUBJECT, "Your Subject");
        intent.putExtra(Intent.EXTRA_TEXT, message);
        return intent;
    }

    public static void shareOnFb(Activity activity, String message, Uri imageUrl) {

        ShareLinkContent content = new ShareLinkContent.Builder()
                .setContentUrl(Uri.parse(activity.getString(R.string.url_play_store_with_utm)))
//                .setContentTitle(activity.getString(R.string.app_name))
                .setQuote(message)
                .setImageUrl(imageUrl)
                .build();


        ShareDialog shareDialog = new ShareDialog(activity);
        shareDialog.registerCallback(new CallbackManagerImpl(), new FacebookCallback<Sharer.Result>() {
            @Override
            public void onSuccess(Sharer.Result result) {
                System.out.println();
                //Toast.makeText(getActivity(),"Success",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancel() {
                System.out.println();
                //  Toast.makeText(getActivity(),"Failed",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                System.out.println();
            }
        });
        shareDialog.show(content, ShareDialog.Mode.AUTOMATIC);
    }

    public static boolean appInstalledOrNot(Context context,String uri) {
        PackageManager pm = context.getPackageManager();
        boolean app_installed;
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            app_installed = true;
        }
        catch (PackageManager.NameNotFoundException e) {
            app_installed = false;
        }
        return app_installed;
    }
}
