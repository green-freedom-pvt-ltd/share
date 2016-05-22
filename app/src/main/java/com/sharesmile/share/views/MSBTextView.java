package com.sharesmile.share.views;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Created by ankitmaheshwari1 on 29/01/16.
 */
public class MSBTextView extends BaseCustomFontTextView {

    private static final String TAG = "MULTextView";

    public MSBTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public MSBTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MSBTextView(Context context) {
        super(context);
    }

    @Override
    protected String getFontName() {
        return "fonts/Montserrat-SemiBold.otf";
    }
}
