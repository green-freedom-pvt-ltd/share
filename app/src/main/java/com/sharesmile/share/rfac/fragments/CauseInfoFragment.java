package com.sharesmile.share.rfac.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.sharesmile.share.R;
import com.sharesmile.share.analytics.events.AnalyticsEvent;
import com.sharesmile.share.analytics.events.Event;
import com.sharesmile.share.core.BaseFragment;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.core.IFragmentController;
import com.sharesmile.share.rfac.models.CauseData;
import com.sharesmile.share.utils.ShareImageLoader;
import com.sharesmile.share.utils.SharedPrefsManager;
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
    Button mRunButton;

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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_toolbar, menu);
        MenuItem messageItem = menu.findItem(R.id.item_message);

        RelativeLayout badge = (RelativeLayout) messageItem.getActionView();
        View badgeIndicator = badge.findViewById(R.id.badge_indicator);
        boolean hasUnreadMessage = SharedPrefsManager.getInstance().getBoolean(Constants.PREF_UNREAD_MESSAGE, false);
        badgeIndicator.setVisibility(hasUnreadMessage ? View.VISIBLE : View.GONE);

        badge.setOnClickListener(this);
    }

    private void init() {


        mRunButton.setOnClickListener(this);

//        mRunButton.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                Logger.d(TAG, "onClick of Begin Run, will start Tracker Activity flow");
//                getFragmentController().performOperation(IFragmentController.START_RUN_TEST, cause);
//                return true;
//            }
//        });

        mDescription.setText(cause.getCauseDescription());
        mTitle.setText(cause.getTitle());
        mCategory.setText(cause.getCategory());
        if (cause.getExecutor() != null) {
            if (cause.getExecutor().getType().equalsIgnoreCase("ngo")) {
                mSponsor.setText("with " + cause.getExecutor().getPartnerNgo());
            } else {
                mSponsor.setText("with " + cause.getExecutor().getPartnerCompany());
            }
        }

        ShareImageLoader.getInstance().loadImage(cause.getImageUrl(), mCauseImage,
                ContextCompat.getDrawable(getContext(), R.drawable.cause_image_placeholder));

        updateActionbar();
    }

    private void updateActionbar() {
        getFragmentController().updateToolBar(getString(R.string.overview), true);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.begin_run:
                getFragmentController().performOperation(IFragmentController.START_RUN, cause);
                AnalyticsEvent.create(Event.ON_CLICK_BEGIN_RUN)
                        .addBundle(cause.getCauseBundle())
                        .buildAndDispatch();
                break;
            case R.id.badge_layout:
                getFragmentController().performOperation(IFragmentController.SHOW_MESSAGE_CENTER, null);
                break;
            default:
        }
    }


}

