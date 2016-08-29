package com.sharesmile.share.views;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Created by ankitmaheshwari1 on 07/03/16.
 */
public class MLButton extends CustomButton{

    private static final String TAG = "MLButton";

    public MLButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public MLButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MLButton(Context context) {
        super(context);
    }

    @Override
    protected String getFontName() {
        return "fonts/Montserrat-Light.otf";
    }
}