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

import com.sharesmile.share.R;

/**
 * Created by apurvgandhwani on 3/28/2016.
 */
public class CauseInfoFragment extends Fragment {


    FragmentManager mFragmentManager;
    Button letsRun;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragmant_cause_info, null);
        letsRun = (Button) v.findViewById(R.id.begin_run);
        mFragmentManager = getFragmentManager();
        letsRun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.drawerLayout, new RunProgress()).addToBackStack("tag").commit();
            }
        });
        return v;
    }




    }

