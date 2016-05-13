package com.sharesmile.share.rfac.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.sharesmile.share.R;
import com.sharesmile.share.core.BaseFragment;
import com.sharesmile.share.core.IFragmentController;
import com.sharesmile.share.rfac.models.CauseData;
import com.sharesmile.share.utils.Logger;
import com.sharesmile.share.views.MLTextView;
import com.sharesmile.share.views.MRTextView;
import com.squareup.picasso.Picasso;

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
        View view = inflater.inflate(R.layout.fragmant_cause_info, null);
        ButterKnife.bind(this, view);
        init();
        return view;
    }

    private void init() {


        mRunButton.setOnClickListener(this);
        mRunButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Logger.d(TAG, "onClick of Begin Run, will start Tracker Activity flow");
                getFragmentController().performOperation(IFragmentController.START_RUN_TEST, cause);
                return true;
            }
        });

        mDescription.setText(cause.getCauseDescription());
        mTitle.setText(cause.getTitle());
        mCategory.setText(cause.getCategory());
        if (cause.getSponsor() != null) {
            mSponsor.setText("by " + cause.getSponsor().getName());
        }

        //load image
        Picasso.with(getContext()).load(cause.getImageUrl()).placeholder(R.drawable.cause_image_placeholder).into(mCauseImage);

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
                break;
            default:
        }
    }


}

