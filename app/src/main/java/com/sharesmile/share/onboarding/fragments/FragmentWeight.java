package com.sharesmile.share.onboarding.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;

import com.sharesmile.share.R;
import com.sharesmile.share.core.application.MainApplication;
import com.sharesmile.share.core.base.BaseFragment;
import com.sharesmile.share.login.UserDetails;
import com.sharesmile.share.onboarding.CommonActions;
import com.sharesmile.share.onboarding.OnBoardingActivity;
import com.sharesmile.share.utils.Utils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FragmentWeight extends BaseFragment implements NumberPicker.OnValueChangeListener {
    public static final String TAG = "FragmentWeight";

    @BindView(R.id.weight_picker)
    NumberPicker weightPicker;

    @BindView(R.id.weight_decimal_picker)
    NumberPicker weightDecimalPicker;

    CommonActions commonActions;

    ArrayList<String> weightStrings;
    ArrayList<String> weightDecimalStrings;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_onboarding_weight, null);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        commonActions = ((OnBoardingActivity) getActivity());
        commonActions.setExplainText(getContext().getResources().getString(R.string.whats_your_current_weight), getContext().getResources().getString(R.string.weight_required_for));
        commonActions.setBackAndContinue(TAG, getResources().getString(R.string.continue_txt));
        setWeights();
        setData();
    }

    private void setData() {
        UserDetails userDetails = MainApplication.getInstance().getUserDetails();
        float userWeight = userDetails.getBodyWeight();
        if (userWeight > 0) {
            if (weightStrings.contains(((int) userWeight) + "")) {
                weightPicker.setValue(weightStrings.indexOf(((int) userWeight) + ""));
            }
            int decimal = (int) ((userWeight * 10) % ((int) userWeight));
            weightDecimalPicker.setValue(decimal);
        }
        setUserWeight(userDetails);

    }

    private void setUserWeight(UserDetails userDetails) {
        float w = weightPicker.getValue() + 40;
        float wd = weightDecimalPicker.getValue() / 10.0f;

        userDetails.setBodyWeight(w + wd);
        MainApplication.getInstance().setUserDetails(userDetails);
    }

    private void setWeights() {
        weightStrings = new ArrayList<>();
        for (int i = 40; i <= 150; i++) {
            weightStrings.add(i + "");
        }
        String s[] = weightStrings.toArray(new String[weightStrings.size() - 1]);
        Utils.setNumberPicker(weightPicker, s, s.length / 2);

        weightDecimalStrings = new ArrayList<>();
        for (int i = 0; i <= 9; i++) {
            weightDecimalStrings.add(i + "");
        }
        String sd[] = weightDecimalStrings.toArray(new String[weightDecimalStrings.size() - 1]);
        Utils.setNumberPicker(weightDecimalPicker, sd, 0);

        weightPicker.setOnValueChangedListener(this);
        weightDecimalPicker.setOnValueChangedListener(this);
    }

    @Override
    public void onValueChange(NumberPicker numberPicker, int i, int i1) {
        UserDetails userDetails = MainApplication.getInstance().getUserDetails();
        setUserWeight(userDetails);
    }
}
