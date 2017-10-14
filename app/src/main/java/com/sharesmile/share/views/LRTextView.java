package com.sharesmile.share.views;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Created by ankitmaheshwari on 10/14/17.
 */

public class LRTextView extends BaseCustomFontTextView {

    public LRTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public LRTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LRTextView(Context context) {
        super(context);
    }

    @Override
    protected String getFontName() {
        return "fonts/Lato-Regular.ttf";
    }
}
