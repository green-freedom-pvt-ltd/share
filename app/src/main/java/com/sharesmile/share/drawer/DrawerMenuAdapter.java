package com.sharesmile.share.drawer;

import android.content.Context;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

/**
 * Created by ankitmaheshwari1 on 11/01/16.
 */
public class DrawerMenuAdapter extends ArrayAdapter<String>{

    private static final String TAG = "DrawerMenuAdapter";

    public DrawerMenuAdapter(Context context, int layoutResource, int textViewResource, ArrayList<String> menuItems) {
        super(context, layoutResource, textViewResource, menuItems);
    }


}
