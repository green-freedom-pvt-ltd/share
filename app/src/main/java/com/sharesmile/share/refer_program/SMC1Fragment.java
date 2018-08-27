package com.sharesmile.share.refer_program;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sharesmile.share.R;
import com.sharesmile.share.core.Logger;
import com.sharesmile.share.core.base.BaseFragment;

import butterknife.ButterKnife;

/**
 * Created by apurvgandhwani on 3/22/2016.
 */
public class SMC1Fragment extends BaseFragment {
    public static final String ARG_OBJECT = "object";
    private static final String TAG = "SMC1Fragment";

    public static Fragment getInstance() {
        Fragment fragment = new SMC1Fragment();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(
                R.layout.fragment_share_n_feed_2, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init();
    }

    private void init() {
    }

    @Override
    public void onStart() {
        super.onStart();
        Logger.d(TAG, "onStart");
    }
}
