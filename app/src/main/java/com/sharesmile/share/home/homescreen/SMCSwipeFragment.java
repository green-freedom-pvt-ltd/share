package com.sharesmile.share.home.homescreen;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sharesmile.share.R;
import com.sharesmile.share.core.Logger;
import com.sharesmile.share.core.application.MainApplication;
import com.sharesmile.share.core.base.BaseFragment;
import com.sharesmile.share.core.event.UpdateEvent;
import com.sharesmile.share.login.UserDetails;
import com.sharesmile.share.refer_program.ReferProgramFragment;
import com.sharesmile.share.refer_program.model.ReferProgram;
import com.sharesmile.share.views.FontCache;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SMCSwipeFragment extends BaseFragment {
    public static final String ARG_OBJECT = "object";
    private static final String TAG = "SMCSwipeFragment";
    @BindView(R.id.smc_swipe_layout)
    LinearLayout smcSwipeLayout;
    @BindView(R.id.close)
    TextView close;
    @BindView(R.id.share_a_meal_desc)
    TextView shareAMealDesc;
    @BindView(R.id.share_a_meal_total_meals)
    TextView shareATotalMeals;
    @BindView(R.id.smc_title)
    TextView smcTitle;
    @BindView(R.id.powered_by_tv)
    TextView poweredByTv;
    @BindView(R.id.bacha_plate)
    ImageView bachaPlate;

    public static Fragment getInstance() {
        Fragment fragment = new SMCSwipeFragment();
        /*Bundle arg = new Bundle();
        arg.putSerializable(ARG_OBJECT, causeData);
        fragment.setArguments(arg);*/
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
                R.layout.smc_card, container, false);
        ButterKnife.bind(this, view);
        EventBus.getDefault().register(this);
        return view;
    }

    @Override
    public void onDestroyView() {
        EventBus.getDefault().unregister(this);
        super.onDestroyView();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        close.setVisibility(View.GONE);
        smcSwipeLayout.setBackgroundResource(R.drawable.greenboard);
        smcTitle.setTextSize(20.4f);
        shareAMealDesc.setLineSpacing(10, 1);
        Typeface myTypeface = FontCache.get("fonts/Lato-Bold.ttf", getContext());
        shareAMealDesc.setTypeface(myTypeface);
        shareAMealDesc.setTextSize(12);
        shareATotalMeals.setTypeface(myTypeface);
        shareATotalMeals.setTextSize(12);
        shareATotalMeals.setText(getResources().getString(R.string.total_meals_by_you) + " " + MainApplication.getInstance().getUserDetails().getMealsShared());

        shareAMealDesc.setText("Invite a friend and feed hungry kids.\nYes! It’s that simple!");
        poweredByTv.setTextSize(11);
        poweredByTv.setText(getResources().getString(R.string.powered_by) + " " + ReferProgram.getReferProgramDetails().getSponsoredBy());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        bachaPlate.setLayoutParams(layoutParams);
    }

    @Override
    public void onStart() {
        super.onStart();
        Logger.d(TAG, "onStart");
    }


    @OnClick(R.id.card_view)
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.card_view:
                showShareScreenFragment();
                /*AnalyticsEvent.create(Event.ON_CLICK_CAUSE_CARD)
                        .put("cause_title", cause.getTitle())
                        .put("cause_id", cause.getId())
                        .buildAndDispatch();*/
                break;
            default:
        }
    }

    private void showShareScreenFragment() {
        getFragmentController().replaceFragment(ReferProgramFragment.getInstance(1), true);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(UpdateEvent.OnMealAdded onMealAdded) {
        UserDetails userDetails = MainApplication.getInstance().getUserDetails();
        shareATotalMeals.setText(getResources().getString(R.string.total_meals_by_you) + " " + MainApplication.getInstance().getUserDetails().getMealsShared());
    }
}
