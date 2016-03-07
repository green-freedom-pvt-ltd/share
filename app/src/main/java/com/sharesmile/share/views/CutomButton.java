package com.sharesmile.share.views;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.Button;


abstract class CustomButton extends Button {

    public static final boolean IS_CUSTOM_FONT_ALLOWED = true;

    public CustomButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    public CustomButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);

    }

    public CustomButton(Context context) {
        super(context);
        init(null);
    }

    private void init(AttributeSet attrs) {
        if (IS_CUSTOM_FONT_ALLOWED) {
            Typeface myTypeface = FontCache.get(getFontName(), getContext());
            setTypeface(myTypeface);
        }
    }

    protected abstract String getFontName();
}