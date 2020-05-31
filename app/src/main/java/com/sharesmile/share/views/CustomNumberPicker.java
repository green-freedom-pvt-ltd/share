package com.sharesmile.share.views;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.sharesmile.share.R;

import java.lang.reflect.Field;

public class CustomNumberPicker extends NumberPicker {

    public CustomNumberPicker(Context context) {
        super(context);
        init();
    }

    public CustomNumberPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomNumberPicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        try {
            Field fDividerDrawable = NumberPicker.class.getDeclaredField("mSelectionDivider");
            fDividerDrawable.setAccessible(true);
            Drawable d = (Drawable) fDividerDrawable.get(this);
            d.setColorFilter(getResources().getColor(R.color.clr_cb050505), PorterDuff.Mode.SRC_ATOP);
            d.invalidateSelf();
            postInvalidate(); // Drawable is dirty
        }
        catch (Exception e) {

        }
    }

    @Override
    public void addView(View child, android.view.ViewGroup.LayoutParams params) {
        super.addView(child, params);
        Typeface myTypeface = FontCache.get("fonts/Lato-Bold.ttf", getContext());
        if(child instanceof EditText)
        {
            ((EditText) child).setTypeface(myTypeface);
            ((EditText) child).setTextSize(TypedValue.COMPLEX_UNIT_SP,17);
            ((EditText) child).setTextColor(getResources().getColor(R.color.clr_cb050505));
        }
    }
}
