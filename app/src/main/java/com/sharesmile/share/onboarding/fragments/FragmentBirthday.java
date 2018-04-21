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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FragmentBirthday extends BaseFragment {
    public static final String TAG = "FragmentBirthday";
    @BindView(R.id.date_picker)
    NumberPicker datePicker;
    @BindView(R.id.month_picker)
    NumberPicker monthPicker;
    @BindView(R.id.year_picker)
    NumberPicker yearPicker;

    String months[] = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};

    CommonActions commonActions;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_onboarding_birthday, null);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        commonActions = ((OnBoardingActivity)getActivity());
        commonActions.setExplainText(getContext().getResources().getString(R.string.when_is_your_birthday),"");
        commonActions.setBackAndContinue(TAG,getResources().getString(R.string.continue_txt));
        setPickers();
        setData();
    }

    private void setData() {
        UserDetails userDetails = MainApplication.getInstance().getUserDetails();
        String birthday = userDetails.getBirthday();
        if(birthday !=null && birthday.length()>0) {
            try {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(simpleDateFormat.parse(birthday).getTime());
                datePicker.setValue(calendar.get(Calendar.DAY_OF_MONTH));
                monthPicker.setValue(calendar.get(Calendar.MONTH));
                yearPicker.setValue(calendar.get(Calendar.YEAR));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    private void setPickers() {
        Utils.setNumberPicker(monthPicker,months,0);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR,-10);
        yearPicker.setMinValue(1900);
        yearPicker.setMaxValue(calendar.get(Calendar.YEAR));
        //implement array string to number picker
        yearPicker.setWrapSelectorWheel(false);
        //disable soft keyboard
        yearPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        yearPicker.setValue(calendar.get(Calendar.YEAR));
        setDatePicker();
        monthPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                setDatePicker();
            }
        });

    }

    private void setDatePicker() {
        int value = datePicker.getValue();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH,monthPicker.getValue());
        calendar.set(Calendar.YEAR,yearPicker.getValue());
        datePicker.setMinValue(calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
        datePicker.setMaxValue(calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        if(value>0)
        datePicker.setValue(value);
        //implement array string to number picker
        datePicker.setWrapSelectorWheel(false);
        //disable soft keyboard
        datePicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
    }
}
