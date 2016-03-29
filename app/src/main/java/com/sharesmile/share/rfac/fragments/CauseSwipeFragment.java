package com.sharesmile.share.rfac.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sharesmile.share.R;


/**
 * Created by apurvgandhwani on 3/22/2016.
 */
public class CauseSwipeFragment extends Fragment {
    public static final String ARG_OBJECT = "object";

    FragmentManager mFragmentManager = getFragmentManager();
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // The last two arguments ensure LayoutParams are inflated
        // properly.
        View rootView = inflater.inflate(
                R.layout.swipe_layout, container, false);
        Bundle args = getArguments();

        TextView descriptionTextView = (TextView)rootView.findViewById(R.id.run_screen_description);
        descriptionTextView.setMovementMethod(new ScrollingMovementMethod());

        rootView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                //this will log the page number that was click

              //  FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
               // fragmentTransaction.replace(R.id.drawerLayout, new CauseInfoFragment()).addToBackStack( "tag" ).commit();
            }
        });
        // ((TextView) rootView.findViewById(R.id.RunList_description)).setText(
        // Integer.toString(args.getInt(ARG_OBJECT)));
        return rootView;
    }

  /*  public void scaleImage(float scale){
        rootView.setScaleX(scale);
//        rootView.setBackgroundColor(Color.BLUE);
    } */


}
