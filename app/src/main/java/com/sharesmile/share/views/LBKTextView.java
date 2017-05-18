package com.sharesmile.share.views;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Created by ankitmaheshwari on 5/17/17.
 */

public class LBKTextView extends BaseCustomFontTextView {

    private static final String TAG = "LBKTextView";

    public LBKTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public LBKTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LBKTextView(Context context) {
        super(context);
    }

    @Override
    protected String getFontName() {
        return "fonts/Lato-Black.ttf";
    }

}
