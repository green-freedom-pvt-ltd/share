package com.sharesmile.share.views;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Created by ankitmaheshwari on 10/13/17.
 */

public class MSBButton extends CustomButton {

    public MSBButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public MSBButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MSBButton(Context context) {
        super(context);
    }

    @Override
    protected String getFontName() {
        return "fonts/Montserrat-SemiBold.otf";
    }
}
