package com.sharesmile.share.rfac;

import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;

import com.sharesmile.share.R;
import com.sharesmile.share.core.Logger;
import com.sharesmile.share.core.ShareImageLoader;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.sharesmile.share.core.application.MainApplication.getContext;

/**
 * Created by ankitmaheshwari on 9/3/17.
 */

public class ImageBannerContainer {

    private static final String TAG = "ImageBannerContainer";

    @BindView(R.id.iv_banner)
    ImageView bannerImage;

    private View containerView;
    private String imageUrl;

    public ImageBannerContainer(View containerView, String imageUrl){
        this.containerView = containerView;
        this.imageUrl = imageUrl;
        ButterKnife.bind(this, containerView);
        init();
    }

    private void init(){
        Logger.d(TAG, "Setting banner image: " + imageUrl);
        ShareImageLoader.getInstance().loadImage(imageUrl, bannerImage,
                ContextCompat.getDrawable(getContext(), R.drawable.cause_image_placeholder));
    }

}
