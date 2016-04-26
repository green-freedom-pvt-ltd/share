package com.sharesmile.share.rfac.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.sharesmile.share.R;
import com.sharesmile.share.core.BaseFragment;
import com.sharesmile.share.core.IFragmentController;

/**
 * Created by apurvgandhwani on 3/28/2016.
 */
public class CauseInfoFragment extends BaseFragment {


    FragmentManager mFragmentManager;
    Button beginRun;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragmant_cause_info, null);
        beginRun = (Button) v.findViewById(R.id.begin_run);
        TextView des_tv = (TextView) v.findViewById(R.id.run_screen_description);

        mFragmentManager = getFragmentManager();
        beginRun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //TODO: Start Tracker Activity here
                getFragmentController().performOperation(IFragmentController.START_RUN, null);
            }
        });
        return v;
    }


}

