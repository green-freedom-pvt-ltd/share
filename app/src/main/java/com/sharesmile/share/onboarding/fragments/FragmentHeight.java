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

public class FragmentHeight extends BaseFragment {
    public static final String TAG = "FragmentHeight";
    @BindView(R.id.height_picker)
    NumberPicker heightPicker;

    @BindView(R.id.height_unit_picker)
    NumberPicker heightUnitPicker;

    CommonActions commonActions;

    ArrayList<String> cmsHeight;
    ArrayList<String> inchHeight;
    String cmsArray[];
    String inchArray[];

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_onboarding_height, null);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        commonActions = ((OnBoardingActivity) getActivity());
        commonActions.setExplainText(getContext().getResources().getString(R.string.whats_your_current_height), "");
        commonActions.setBackAndContinue(TAG,getResources().getString(R.string.continue_txt));
        setPicker();
        setData();
    }

    private void setData() {
        UserDetails userDetails = MainApplication.getInstance().getUserDetails();
        int height = userDetails.getBodyHeight();
        int height_unit = userDetails.getBodyHeightUnit();

        if(height==0)
        {
            heightPicker.setValue(cmsHeight.size()/2);
        }else
        {
        if(height_unit==0)
        {
            heightPicker.setValue(cmsHeight.indexOf(height+""));
        }else
        {
            heightPicker.setValue(inchHeight.indexOf(cmsToInches(height)));
        }
        }
        heightUnitPicker.setValue(height_unit);
    }

    private void setPicker() {
        String units[] = getResources().getStringArray(R.array.height_units);
        Utils.setNumberPicker(heightUnitPicker, units, 0);
        cmsHeight = new ArrayList<>();
        inchHeight = new ArrayList<>();

        for (int i = 100; i <= 200; i++) {
            cmsHeight.add(i + "");
            String inchesString = cmsToInches(i);
            if(!inchHeight.contains(inchesString))
            inchHeight.add(inchesString);
        }
        cmsArray = cmsHeight.toArray(new String[cmsHeight.size()]);
        inchArray = inchHeight.toArray(new String[inchHeight.size()]);
        if(MainApplication.getInstance().getUserDetails().getBodyHeightUnit() == 0)
        {
            Utils.setNumberPicker(heightPicker, cmsArray, heightPicker.getValue());
        }else
        {
            Utils.setNumberPicker(heightPicker, inchArray, heightPicker.getValue());
        }
        heightPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                UserDetails userDetails = MainApplication.getInstance().getUserDetails();

                if(heightUnitPicker.getValue()==0)
                {
                    userDetails.setBodyHeight(Integer.parseInt(cmsArray[heightPicker.getValue()]));
                }else
                {
                    String inchesFeet = inchArray[heightPicker.getValue()];
                    String cms = inchesTocms(inchesFeet);
                    int cms_ = Integer.parseInt(cms);
                    if(cms_>200) {
                        cms_ = 200;
                    }
                    userDetails.setBodyHeight(cms_);
                }
                userDetails.setBodyHeightUnit(heightUnitPicker.getValue());
                MainApplication.getInstance().setUserDetails(userDetails);
            }
        });
        heightUnitPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                int cms_ = 0;
                if(i1==0)
                {
                    String inchesFeet = inchArray[heightPicker.getValue()];
                    String cms = inchesTocms(inchesFeet);
                    cms_ = Integer.parseInt(cms);
                    if(cms_>200) {
                        cms_ = 200;
                        cms = "200";
                    }
                    Utils.setNumberPicker(heightPicker, cmsArray, cmsHeight.indexOf(cms));
                }else if(i1==1)
                {
                    String cms = cmsArray[heightPicker.getValue()];
                    cms_ = Integer.parseInt(cms);
                    String inchesFeet = cmsToInches(Integer.parseInt(cms));
                    Utils.setNumberPicker(heightPicker, inchArray, inchHeight.indexOf(inchesFeet));
                }
                UserDetails userDetails = MainApplication.getInstance().getUserDetails();
                userDetails.setBodyHeight(cms_);
                userDetails.setBodyHeightUnit(i1);
                MainApplication.getInstance().setUserDetails(userDetails);
            }
        });
    }

    String cmsToInches(int cms)
    {
        int feet = (int) (cms/30.48);
        int inches = (int) Math.round((cms%30.48) * 0.393701);
        inches = inches==12?11:inches;
        return feet + "' "+inches+"\"";
    }

    String inchesTocms(String i)
    {
        String[] feetInches = i.split(" ");
        int feet = Integer.parseInt(feetInches[0].substring(0,feetInches[0].length()-1));
        int inches = Integer.parseInt(feetInches[1].substring(0,feetInches[1].length()-1));
        int cms = (int) Math.round((feet*30.48) + (inches/0.393701));
        return cms+"";
    }
}
