package com.sharesmile.share.rfac.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.sharesmile.share.R;
import com.sharesmile.share.core.BaseFragment;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Created by apurvgandhwani on 3/26/2016.
 */
public class FeedbackFragment extends BaseFragment implements View.OnClickListener {

    @BindView(R.id.btn_feedback)
    Button mSubmitButton;

    @BindView(R.id.et_feedback_text)
    EditText mFeedbackText;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_drawer_feedback, null);
        ButterKnife.bind(this, view);
        mSubmitButton.setOnClickListener(this);
        getFragmentController().updateToolBar(getString(R.string.feedback), true);
        return view;
    }

    @Override
    public void onClick(View v) {

        //submit feedback to server

        getFragmentController().addFragment(new OnScreenFragment(),true);
    }
}
