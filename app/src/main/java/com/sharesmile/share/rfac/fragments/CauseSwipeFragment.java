package com.sharesmile.share.rfac.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sharesmile.share.R;
import com.sharesmile.share.analytics.events.AnalyticsEvent;
import com.sharesmile.share.analytics.events.Event;
import com.sharesmile.share.core.BaseFragment;
import com.sharesmile.share.rfac.models.CauseData;
import com.sharesmile.share.utils.Logger;
import com.sharesmile.share.utils.Utils;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by apurvgandhwani on 3/22/2016.
 */
public class CauseSwipeFragment extends BaseFragment implements View.OnClickListener {
    public static final String ARG_OBJECT = "object";
    private static final String TAG = "CauseSwipeFragment";

    private CauseData cause;

    @BindView(R.id.run_screen_sponsor)
    TextView mSponsor;

    @BindView(R.id.run_screen_title)
    TextView mTitle;
    @BindView(R.id.category)
    TextView mCategory;


    @BindView(R.id.run_screen_description)
    TextView mDescription;

    @BindView(R.id.img_run)
    ImageView mCauseImage;

    @BindView(R.id.card_view)
    CardView mCardView;

    @BindView(R.id.amount_raised_rupees)
    TextView amountRaisedRupees;

    @BindView(R.id.amount_raised_percent)
    TextView amountRaisedPercent;

    @BindView(R.id.amount_raised_progress_bar)
    View amountRaisedProgress;

    @BindView(R.id.num_impact_runs)
    TextView numImpactRuns;


    public static Fragment getInstance(CauseData causeData) {
        Fragment fragment = new CauseSwipeFragment();
        Bundle arg = new Bundle();
        arg.putSerializable(ARG_OBJECT, causeData);
        fragment.setArguments(arg);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arg = getArguments();
        cause = (CauseData) arg.getSerializable(ARG_OBJECT);
    }


    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(
                R.layout.swipe_layout, container, false);
        ButterKnife.bind(this, view);
        init();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Logger.d(TAG, "onStart");
    }

    private void init() {
        Logger.d(TAG, "init with cause: " + cause.getTitle() +", and amount raised: " + cause.getAmountRaised());
        mDescription.setText(cause.getDetailText());
        mTitle.setText(cause.getTitle());
        mCategory.setText(cause.getCategory());
        if (cause.getExecutor() != null) {
            if(cause.getExecutor().getType().equalsIgnoreCase("ngo")){
                mSponsor.setText("with " + cause.getExecutor().getPartnerNgo());
            }else {
                mSponsor.setText("with " + cause.getExecutor().getPartnerCompany());
            }
        }

        mCardView.setOnClickListener(this);

        float targetAmount = cause.getTargetAmount();
        float amountRaised = cause.getAmountRaised();
        amountRaisedRupees.setText(getString(R.string.amount_raised_rupees,
                Utils.formatIndianCommaSeparated(amountRaised)));

        float percent = (targetAmount > 0f) ? (amountRaised / targetAmount) : 0;
        amountRaisedPercent.setText((int) (percent*100) + "%");

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) amountRaisedProgress.getLayoutParams();
        params.weight = percent;
        amountRaisedProgress.setLayoutParams(params);

        int numRuns = cause.getTotalRuns();
        numImpactRuns.setText(getString(R.string.num_impact_runs, numRuns));

        //load image
        Picasso.with(getContext()).load(cause.getImageUrl()).placeholder(R.drawable.cause_image_placeholder).into(mCauseImage);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.card_view:
                showCauseInfoFragment();
                AnalyticsEvent.create(Event.ON_CLICK_CAUSE_CARD)
                        .put("cause_title", cause.getTitle())
                        .put("cause_id", cause.getId())
                        .buildAndDispatch();
                break;
            default:
        }
    }

    private void showCauseInfoFragment() {
        getFragmentController().replaceFragment(CauseInfoFragment.getInstance(cause), true);
    }
}
