package com.sharesmile.share.views;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Created by ankitmaheshwari on 5/17/17.
 */

public class LBTextView extends BaseCustomFontTextView{

    private static final String TAG = "LBTextView";

    public LBTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public LBTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LBTextView(Context context) {
        super(context);
    }

    @Override
    protected String getFontName() {
        return "fonts/Lato-Bold.ttf";
    }

}
