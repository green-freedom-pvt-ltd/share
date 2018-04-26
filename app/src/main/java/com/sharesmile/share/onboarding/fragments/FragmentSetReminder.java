package com.sharesmile.share.onboarding.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.NumberPicker;
import android.widget.RadioButton;

import com.sharesmile.share.R;
import com.sharesmile.share.core.base.BaseFragment;
import com.sharesmile.share.onboarding.CommonActions;
import com.sharesmile.share.onboarding.OnBoardingActivity;
import com.sharesmile.share.utils.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FragmentSetReminder extends BaseFragment implements NumberPicker.OnValueChangeListener {
    public static final String TAG = "FragmentSetReminder";
    CommonActions commonActions;
    @BindView(R.id.hour_picker)
    NumberPicker hourPicker;
    @BindView(R.id.minute_picker)
    NumberPicker minutePicker;
    @BindView(R.id.am_pm_picker)
    NumberPicker ampmPicker;
    String ampm[] = {"AM","PM"};
    String minutes[];
    boolean isChecked = false;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_onboarding_set_reminder, null);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        commonActions = ((OnBoardingActivity) getActivity());
        commonActions.setExplainText(getContext().getResources().getString(R.string.select_time_for_daily_reminder), getContext().getResources().getString(R.string.select_time_explain_txt));
        commonActions.setBackAndContinue(TAG,getResources().getString(R.string.continue_txt));
        setTimePickers();
    }

    private void setTimePickers() {
        Utils.setNumberPicker(ampmPicker,ampm,0);

        hourPicker.setMinValue(1);
        hourPicker.setMaxValue(12);
        hourPicker.setValue(6);
        Utils.setReminderTime("06:00",getContext());
        hourPicker.setWrapSelectorWheel(false);
        hourPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        minutes = new String[60/5];
        for(int i=0;i<12;i++)
        {
            minutes[i] = (i*5)<10?"0"+i*5:i*5+"";
        }
        Utils.setNumberPicker(minutePicker,minutes,0);
        hourPicker.setWrapSelectorWheel(true);
        minutePicker.setWrapSelectorWheel(true);
        hourPicker.setOnValueChangedListener(this);
        minutePicker.setOnValueChangedListener(this);
        ampmPicker.setOnValueChangedListener(this);
    }

    @Override
    public void onValueChange(NumberPicker numberPicker, int i, int i1) {
        int hour = hourPicker.getValue();
        String minute = minutes[minutePicker.getValue()];
        int ampm = ampmPicker.getValue();

        if(ampm!=0 && hour!=12)
        {
         hour+=12;
        }else if(hour==12 && ampm==0)
        {
            hour = 0;
        }

        String hour_s = hour<10?"0"+hour:hour+"";
        Utils.setReminderTime(hour_s+":"+minute,getContext());
    }
}
