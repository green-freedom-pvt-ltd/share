package com.sharesmile.share.rfac.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.sharesmile.share.R;

/**
 * Created by apurvgandhwani on 3/29/2016.
 */
public class ProfileHistoryFragment extends Fragment {
    ListView lv;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_profile_history, null);
        lv = (ListView) v.findViewById(R.id.lv_profile_history);

        return v;
    }
}




