package com.sharesmile.share.views;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Created by ankitmaheshwari1 on 29/01/16.
 */
public class MLTextView extends BaseCustomFontTextView {

    private static final String TAG = "MLTextView";

    public MLTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public MLTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MLTextView(Context context) {
        super(context);
    }

    @Override
    protected String getFontName() {
        return "fonts/Montserrat-Light.otf";
    }
}
