package com.sharesmile.share.rfac.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.sharesmile.share.R;
import com.sharesmile.share.rfac.activities.ShareMainActivity;

/**
 * Created by apurvgandhwani on 3/28/2016.
 */
public class RunProgress extends Fragment {

    Button butnstart, butnreset;
    TextView time;
    long starttime = 0L;
    long timeInMilliseconds = 0L;
    long timeSwapBuff = 0L;
    long updatedtime = 0L;
    int t = 1;
    int secs = 0;
    int mins = 0;
    int milliseconds = 0;
    Handler handler = new Handler();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
     //  getActivity().getActionBar().hide();
        //setHasOptionsMenu(true);
        // update the actionbar to show the up carat/affordance
      //  getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        View v = inflater.inflate(R.layout.run_progress, null);
        time = (TextView)v.findViewById(R.id.timer);
        final Button startButton = (Button)v.findViewById(R.id.btn_pause);
        Button stopButton  = (Button)v.findViewById(R.id.btn_stop);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (t == 1) {
                    startButton.setText("Pause");
                    starttime = SystemClock.uptimeMillis();
                    handler.postDelayed(updateTimer, 0);
                    t = 0;
                } else {
                    startButton.setText("Start");
                    timeSwapBuff += timeInMilliseconds;
                    handler.removeCallbacks(updateTimer);
                    t = 1;
                }
            }

        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             /*   starttime = 0L;
                timeInMilliseconds = 0L;
                timeSwapBuff = 0L;
                updatedtime = 0L;
                t = 1;
                secs = 0;
                mins = 0;
                milliseconds = 0;
                startButton.setText("Start");
                handler.removeCallbacks(updateTimer);
                time.setText("00:00:00"); */
                Intent i = new Intent(getActivity(), ShareMainActivity.class);
                startActivity(i);
              //  ((Activity) getActivity()).overridePendingTransition(0,0);

            }

        });
        return v;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }


    public Runnable updateTimer = new Runnable() {
        public void run() {
            timeInMilliseconds = SystemClock.uptimeMillis() - starttime;
            updatedtime = timeSwapBuff + timeInMilliseconds;
            secs = (int) (updatedtime / 1000);
            mins = secs / 60;
            secs = secs % 60;
            milliseconds = (int) (updatedtime % 1000);
            time.setText("" + mins + ":" + String.format("%02d", secs) + ":"
                    + String.format("%03d", milliseconds));
            handler.postDelayed(this, 0);
        }};


}
