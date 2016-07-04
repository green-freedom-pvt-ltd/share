package com.sharesmile.share.rfac.fragments;

import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sharesmile.share.R;
import com.sharesmile.share.core.BaseFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Shine on 25/06/16.
 */
public class OnBoardingFragment extends BaseFragment {


    private static final String BUNDLE_POSITION = "bundle_position";
    private int type = 0;

    public static OnBoardingFragment getInstance(int position) {

        OnBoardingFragment fragment = new OnBoardingFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(BUNDLE_POSITION, position);
        fragment.setArguments(bundle);
        return fragment;
    }


    @BindView(R.id.title)
    TextView mTitle;

    @BindView(R.id.details)
    TextView mDetails;

    @BindView(R.id.image)
    ImageView mImage;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arg = getArguments();
        type = arg.getInt(BUNDLE_POSITION);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_onboarding, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TypedArray typedArray = getResources().obtainTypedArray(R.array.onboarding_title);
        TypedArray detailsArray = getResources().obtainTypedArray(R.array.onboarding_msg);
        TypedArray drawableArray = getResources().obtainTypedArray(R.array.onboarding_image);
        mImage.setImageDrawable(getResources().getDrawable(drawableArray.getResourceId(type, 0)));
        mTitle.setText(getString(typedArray.getResourceId(type, 0)));
        mDetails.setText(getString(detailsArray.getResourceId(type, 0)));

    }
}
