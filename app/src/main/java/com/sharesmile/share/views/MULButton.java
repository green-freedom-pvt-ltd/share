package com.sharesmile.share.views;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Created by ankitmaheshwari1 on 07/03/16.
 */
public class MULButton extends CustomButton{

    private static final String TAG = "MULButton";

    public MULButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public MULButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MULButton(Context context) {
        super(context);
    }

    @Override
    protected String getFontName() {
        return "fonts/Montserrat-UltraLight.otf";
    }
}
