package com.sharesmile.share.views;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Created by ankitmaheshwari on 5/17/17.
 */

public class LBITextView extends BaseCustomFontTextView{

    private static final String TAG = "LBTextView";

    public LBITextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public LBITextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LBITextView(Context context) {
        super(context);
    }

    @Override
    protected String getFontName() {
        return "fonts/Lato-BoldItalic.ttf";
    }

}
