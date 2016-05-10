package com.sharesmile.share.rfac.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sharesmile.share.R;

/**
 * Created by apurvgandhwani on 3/22/2016.
 */
public class CauseSwipeFragment extends Fragment {
    public static final String ARG_OBJECT = "object";

    FragmentManager mFragmentManager;
    TextView tv_description;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // The last two arguments ensure LayoutParams are inflated
        // properly.
        View rootView = inflater.inflate(
                R.layout.swipe_layout, container, false);
        tv_description = (TextView) rootView.findViewById(R.id.run_screen_description);

        mFragmentManager = getFragmentManager();
        TextView descriptionTextView = (TextView) rootView.findViewById(R.id.run_screen_description);
        ImageView causeImage = (ImageView) rootView.findViewById(R.id.img_run);
        descriptionTextView.setMovementMethod(new ScrollingMovementMethod());
        return rootView;
    }


}
