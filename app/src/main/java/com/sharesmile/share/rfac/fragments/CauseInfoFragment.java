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
import com.sharesmile.share.utils.Logger;

/**
 * Created by apurvgandhwani on 3/28/2016.
 */
public class CauseInfoFragment extends BaseFragment {

    private static final String TAG = "CauseInfoFragment";

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
                Logger.d(TAG, "onClick of Begin Run, will start Tracker Activity flow");
                getFragmentController().performOperation(IFragmentController.START_RUN, false);
            }
        });
        beginRun.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Logger.d(TAG, "onClick of Begin Run, will start Tracker Activity flow");
                getFragmentController().performOperation(IFragmentController.START_RUN, true);
                return true;
            }
        });
        return v;
    }


}

