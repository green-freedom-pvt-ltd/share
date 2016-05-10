package com.sharesmile.share.rfac.fragments;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sharesmile.share.R;
import com.sharesmile.share.core.BaseFragment;
import com.sharesmile.share.core.IFragmentController;

/**
 * Created by apurvgandhwani on 3/31/2016.
 */
public class StartRunFragment extends BaseFragment {

    TextView countdown;
    FragmentManager mFragmentManager;
    CountDownTimer Count;

    public static StartRunFragment newInstance(){
        return new StartRunFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_start_run, null);
        countdown = (TextView) v.findViewById(R.id.tv_countdown);
        RelativeLayout layout_countdown = (RelativeLayout) v.findViewById(R.id.start_countdown_layout);
        layout_countdown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                proceedToRunProgress();
            }
        });
        mFragmentManager = getFragmentManager();
        Count = new CountDownTimer(10000, 1000) {



            public void onTick(long millisUntilFinished) {
                int seconds = (int) ((millisUntilFinished / 1000));
                countdown.setText(seconds + "");
            }

            public void onFinish() {
                proceedToRunProgress();
            }
        };


        Count.start();

        return v;
    }


    private void proceedToRunProgress(){
        if (isAttachedToActivity()){
            getFragmentController().performOperation(IFragmentController.END_RUN_START_COUNTDOWN, null);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Count.start();
    }

    @Override
    public void onPause() {
        Count.cancel();
        super.onPause();
    }


}