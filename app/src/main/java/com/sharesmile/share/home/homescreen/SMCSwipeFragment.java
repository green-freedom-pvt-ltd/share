package com.sharesmile.share.home.homescreen;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sharesmile.share.R;
import com.sharesmile.share.core.Logger;
import com.sharesmile.share.core.base.BaseFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SMCSwipeFragment extends BaseFragment implements View.OnClickListener {
    public static final String ARG_OBJECT = "object";
    private static final String TAG = "SMCSwipeFragment";
    @BindView(R.id.smc_swipe_layout)
    LinearLayout smcSwipeLayout;
    @BindView(R.id.close)
    TextView close;

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
                R.layout.share_a_meal_image_layout, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        close.setVisibility(View.GONE);
        smcSwipeLayout.setBackgroundResource(R.drawable.greenboard);
    }

    @Override
    public void onStart() {
        super.onStart();
        Logger.d(TAG, "onStart");
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.card_view:
             /*   showCauseInfoFragment();
                AnalyticsEvent.create(Event.ON_CLICK_CAUSE_CARD)
                        .put("cause_title", cause.getTitle())
                        .put("cause_id", cause.getId())
                        .buildAndDispatch();*/
                break;
            case R.id.iv_cause_completed:
                /*showCauseInfoFragment();
                AnalyticsEvent.create(Event.ON_CLICK_CAUSE_COMPLETED_CARD)
                        .put("cause_title", cause.getTitle())
                        .put("cause_id", cause.getId())
                        .buildAndDispatch();*/
                break;
            default:
        }
    }

    /*private void showCauseInfoFragment() {
        getFragmentController().replaceFragment(CauseInfoFragment.getInstance(cause), true);
    }*/
}
