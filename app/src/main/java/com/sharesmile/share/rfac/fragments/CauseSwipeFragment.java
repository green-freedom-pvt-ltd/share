package com.sharesmile.share.rfac.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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
import com.sharesmile.share.core.base.BaseFragment;
import com.sharesmile.share.home.settings.UnitsManager;
import com.sharesmile.share.rfac.models.CauseData;
import com.sharesmile.share.core.Logger;
import com.sharesmile.share.core.ShareImageLoader;
import com.sharesmile.share.utils.Utils;

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

    @BindView(R.id.iv_cause_completed)
    ImageView causeCompletedImage;

    @BindView(R.id.card_container)
    View cardContainer;

    @BindView(R.id.amount_remaining_percent)
    TextView amountCompletedPercent;

    @BindView(R.id.amount_raised_progress_bar)
    View amountRaisedProgress;

    @BindView(R.id.num_impact_runs)
    TextView numImpactRuns;

    @BindView(R.id.tv_goal_amount)
    TextView goalAmount;


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
                R.layout.cause_card, container, false);
        ButterKnife.bind(this, view);
        if (cause.isCompleted()){
            renderCauseCompletedImage();
        }else {
            renderCardContainer();
        }
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Logger.d(TAG, "onStart");
    }

    public boolean isCompleted(){
        return cause.isCompleted();
    }

    private void renderCauseCompletedImage(){
        cardContainer.setVisibility(View.GONE);
        ShareImageLoader.getInstance().loadImage(cause.getCauseCompletedImage(), causeCompletedImage,
                ContextCompat.getDrawable(getContext(), R.drawable.placeholder_thankyou_image));
        causeCompletedImage.setOnClickListener(this);
        causeCompletedImage.setVisibility(View.VISIBLE);
    }

    private void renderCardContainer() {
        causeCompletedImage.setVisibility(View.GONE);
        mDescription.setText(cause.getDetailText());
        mTitle.setText(cause.getTitle());
        mCategory.setText(cause.getCategory());
        if (cause.getExecutor() != null) {
            if(cause.getExecutor().getType().equalsIgnoreCase("ngo")){
                mSponsor.setText("with " + cause.getExecutor().getPartnerNgo() + " & " + cause.getSponsor().getName());
            }else {
                mSponsor.setText("with " + cause.getExecutor().getPartnerCompany());
            }
        }

        mCardView.setOnClickListener(this);

        float targetAmount = cause.getTargetAmount();
        float amountRaised = cause.getAmountRaised();
        goalAmount.setText(UnitsManager.formatRupeeToMyCurrency(targetAmount));

        float percent = (targetAmount > 0f) ? (amountRaised / targetAmount) : 0;
        if (percent > 1){
            percent = 1;
        }
        int completedPercent = Math.round((percent)*100);
        amountCompletedPercent.setText(completedPercent+"%");

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) amountRaisedProgress.getLayoutParams();
        params.weight = percent;
        amountRaisedProgress.setLayoutParams(params);

        int numRuns = cause.getTotalRuns();
        numImpactRuns.setText(Utils.formatCommaSeparated(numRuns));

        //load image
        ShareImageLoader.getInstance().loadImage(cause.getImageUrl(), mCauseImage,
                ContextCompat.getDrawable(getContext(), R.drawable.cause_image_placeholder));
        cardContainer.setVisibility(View.VISIBLE);
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
            case R.id.iv_cause_completed:
                showCauseInfoFragment();
                AnalyticsEvent.create(Event.ON_CLICK_CAUSE_COMPLETED_CARD)
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
