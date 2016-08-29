package com.sharesmile.share.views;

/**
 * Created by ankitmaheshwari1 on 07/03/16.
 */

import android.content.Context;
import android.util.AttributeSet;

/**
 * Created by ankitmaheshwari1 on 07/03/16.
 */
public class MRButton extends CustomButton{

    private static final String TAG = "MRButton";

    public MRButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public MRButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MRButton(Context context) {
        super(context);
    }

    @Override
    protected String getFontName() {
        return "fonts/Montserrat-Regular.otf";
    }
}