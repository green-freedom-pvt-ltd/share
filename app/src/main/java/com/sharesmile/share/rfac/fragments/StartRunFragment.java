package com.sharesmile.share.rfac.fragments;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sharesmile.share.R;

/**
 * Created by apurvgandhwani on 3/31/2016.
 */
public class StartRunFragment extends Fragment {

    TextView countdown;
    FragmentManager mFragmentManager;
    CountDownTimer Count;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_start_run, null);
        countdown = (TextView) v.findViewById(R.id.tv_countdown);
        RelativeLayout layout_countdown = (RelativeLayout) v.findViewById(R.id.start_countdown_layout);
        layout_countdown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.drawerLayout, new RunProgressFragment()).addToBackStack("tag").commit();
            }
        });
        mFragmentManager = getFragmentManager();
        Count = new CountDownTimer(10000, 1000) {
            public void onTick(long millisUntilFinished) {
                int seconds = (int) ((millisUntilFinished / 1000));

                countdown.setText(millisUntilFinished / 1000 + "");

            }

            public void onFinish() {
                FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.drawerLayout, new RunProgressFragment()).addToBackStack("tag").commit();
            }
        };


        Count.start();

        return v;
    }

    @Override
    public void onPause() {
        Count.cancel();
        super.onPause();
    }


}