package com.sharesmile.share.views;

import android.content.Context;
import android.graphics.Typeface;

import java.util.HashMap;

/**
 * Created by ankitmaheshwari1 on 29/01/16.
 */
public class FontCache {

    private static final String TAG = "FontCache";

    private static HashMap<String, Typeface> cache = new HashMap<String, Typeface>();

    public static Typeface get(String name, Context context) {
        Typeface typeFace = cache.get(name);
        if (typeFace == null) {
            try {
                typeFace = Typeface.createFromAsset(context.getAssets(), name);
                cache.put(name, typeFace);
            } catch (Exception e) {
                return Typeface.DEFAULT;
            }
        }
        return typeFace;
    }

}
