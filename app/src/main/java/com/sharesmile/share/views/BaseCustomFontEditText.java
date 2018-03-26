package com.sharesmile.share.views;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by ankitmaheshwari1 on 29/01/16.
 */
public abstract class BaseCustomFontEditText extends android.support.v7.widget.AppCompatEditText {

    private static final String TAG = "BaseCustomFontTextView";

    public static final boolean IS_CUSTOM_FONT_ALLOWED = true;

    public BaseCustomFontEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    public BaseCustomFontEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);

    }

    public BaseCustomFontEditText(Context context) {
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
