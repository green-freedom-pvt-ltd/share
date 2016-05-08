package com.sharesmile.share.rfac.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;

import com.sharesmile.share.R;
import com.sharesmile.share.core.BaseFragment;
import com.sharesmile.share.core.IFragmentController;
import com.sharesmile.share.gps.models.WorkoutData;
import com.sharesmile.share.rfac.models.CauseData;

/**
 * Created by apurvgandhwani on 4/5/2016.
 */
public class ShareFragment extends BaseFragment {

    public static final String WORKOUT_DATA = "workout_data";
    public static final String BUNDLE_CAUSE_DATA = "bundle_cause_data";
    private static final String TAG = "ShareFragment";
    private CauseData mCauseData;

    public static ShareFragment newInstance(WorkoutData data, CauseData causeData) {
        ShareFragment fragment = new ShareFragment();
        Bundle args = new Bundle();
        args.putParcelable(WORKOUT_DATA, data);
        args.putSerializable(BUNDLE_CAUSE_DATA, causeData);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arg = getArguments();
        mCauseData=(CauseData)arg.getSerializable(BUNDLE_CAUSE_DATA);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_share, null);
        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        Button btn_share = (Button) v.findViewById(R.id.btn_share_screen);
        Button btn_share_skip = (Button) v.findViewById(R.id.btn_share_screen_skip_share);
        btn_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentController().performOperation(IFragmentController.SAY_THANK_YOU, mCauseData.getCauseThankYouImage());
            }
        });

        btn_share_skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentController().performOperation(IFragmentController.SAY_THANK_YOU, mCauseData.getCauseThankYouImage());
            }
        });

        //TODO: Populate all textViews using workout data

        return v;
    }
}



