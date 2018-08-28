package com.sharesmile.share.refer_program;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sharesmile.share.R;
import com.sharesmile.share.core.Logger;
import com.sharesmile.share.core.base.BaseFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SMC0Fragment extends BaseFragment {
    public static final String ARG_OBJECT = "object";
    private static final String TAG = "SMC0Fragment";
    @BindView(R.id.title_layout)
    RelativeLayout titleLayout;
    @BindView(R.id.powered_by_tv)
    TextView poweredByTv;
    @BindView(R.id.share_a_meal_desc)
    TextView shareAMealDesc;
    @BindView(R.id.share_a_meal_total_meals)
    TextView shareAMealTotalMeals;

    public static Fragment getInstance() {
        Fragment fragment = new SMC0Fragment();
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
                R.layout.share_a_meal_image_layout, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init();
    }

    private void init() {
        titleLayout.setVisibility(View.GONE);
        poweredByTv.setVisibility(View.GONE);
        shareAMealTotalMeals.setVisibility(View.GONE);
    }

    @Override
    public void onStart() {
        super.onStart();
        Logger.d(TAG, "onStart");
    }
}
