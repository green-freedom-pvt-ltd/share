package com.sharesmile.share.views;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.RadioButton;

/**
 * Created by ankitmaheshwari on 5/17/17.
 */

public class LBRadioButton extends android.support.v7.widget.AppCompatRadioButton{

    private static final String TAG = "LBRadioButton";

    public LBRadioButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public LBRadioButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LBRadioButton(Context context) {
        super(context);
        init();
    }

    private void init() {
            Typeface myTypeface = FontCache.get(getFontName(), getContext());
            setTypeface(myTypeface);

    }
    protected String getFontName() {
        return "fonts/Lato-Bold.ttf";
    }

}
