package com.sharesmile.share.home.homescreen;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sharesmile.share.core.cause.CauseDataStore;
import com.sharesmile.share.R;
import com.sharesmile.share.analytics.events.AnalyticsEvent;
import com.sharesmile.share.analytics.events.Event;
import com.sharesmile.share.core.base.BaseFragment;
import com.sharesmile.share.core.base.IFragmentController;
import com.sharesmile.share.core.cause.model.CauseData;
import com.sharesmile.share.core.ShareImageLoader;
import com.sharesmile.share.utils.Utils;
import com.sharesmile.share.views.MLTextView;
import com.sharesmile.share.views.MRTextView;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by apurvgandhwani on 3/28/2016.
 */
public class CauseInfoFragment extends BaseFragment implements View.OnClickListener {

    private static final String TAG = "CauseInfoFragment";

    public static final String BUNDLE_CAUSE_OBJECT = "bundle_cause_object";

    @BindView(R.id.begin_run)
    View mRunButton;

    @BindView(R.id.tv_lets_run)
    TextView mRunButtonText;

    @BindView(R.id.iv_lets_run)
    View mRunButtonImage;

    @BindView(R.id.run_screen_sponser)
    MLTextView mSponsor;

    @BindView(R.id.run_screen_title)
    MRTextView mTitle;
    @BindView(R.id.category)
    MLTextView mCategory;


    @BindView(R.id.run_screen_description)
    MLTextView mDescription;

    @BindView(R.id.image_run)
    ImageView mCauseImage;
    private CauseData cause;


    public static BaseFragment getInstance(CauseData cause) {

        BaseFragment fragment = new CauseInfoFragment();
        Bundle arg = new Bundle();
        arg.putSerializable(BUNDLE_CAUSE_OBJECT, cause);
        fragment.setArguments(arg);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arg = getArguments();
        cause = (CauseData) arg.getSerializable(BUNDLE_CAUSE_OBJECT);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cause_info, null);
        ButterKnife.bind(this, view);
        init();
        setHasOptionsMenu(true);
        return view;
    }

    private void init() {

        mRunButton.setOnClickListener(this);
        String description = cause.isCompleted() ? cause.getCauseCompletedReport() : cause.getCauseDescription();
        mDescription.setText(description);

        mTitle.setText(cause.getTitle());
        mCategory.setText(cause.getCategory());

        if (cause.getExecutor() != null) {
            if (cause.getExecutor().getType().equalsIgnoreCase("ngo")) {
                mSponsor.setText("with " + cause.getExecutor().getPartnerNgo());
            } else {
                mSponsor.setText("with " + cause.getExecutor().getPartnerCompany());
            }
        }

        String imageUrl = cause.isCompleted() ? cause.getCauseCompletedDescriptionImage() : cause.getImageUrl();
        ShareImageLoader.getInstance().loadImage(imageUrl, mCauseImage,
                ContextCompat.getDrawable(getContext(), R.drawable.cause_image_placeholder));

        setLetsRunButton(cause.isCompleted());

        updateActionbar();
    }

    private void updateActionbar() {
        String toolbarTitle = cause.isCompleted() ? getString(R.string.thank_you) : getString(R.string.overview);
        getFragmentController().updateToolBar(toolbarTitle, true);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.begin_run:
                if (cause.isCompleted()){
                    Utils.shareImageWithMessage(getContext(), cause.getCauseCompletedImage(),
                            cause.getCauseCompletedShareMessageTemplate());
                }else {
                    CauseDataStore.getInstance().registerCauseSelection(cause);
                    getFragmentController().performOperation(IFragmentController.START_RUN, cause);
                    AnalyticsEvent.create(Event.ON_CLICK_BEGIN_RUN)
                            .addBundle(cause.getCauseBundle())
                            .buildAndDispatch();
                }
                break;
            default:
        }
    }

    private void setLetsRunButton(boolean isCauseCompleted){
        if (isCauseCompleted){
            mRunButtonText.setText(getString(R.string.tell_your_friends));
            mRunButtonImage.setVisibility(View.GONE);
        } else {
            mRunButtonText.setText(getString(R.string.let_go));
            mRunButtonImage.setVisibility(View.VISIBLE);
        }
    }


}

