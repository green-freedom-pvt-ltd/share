package com.sharesmile.share.profile.editprofiledialogs;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.NumberPicker;

import com.sharesmile.share.R;
import com.sharesmile.share.core.application.MainApplication;
import com.sharesmile.share.login.UserDetails;
import com.sharesmile.share.profile.SaveData;
import com.sharesmile.share.utils.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class EditBirthday extends ParentDialog implements View.OnClickListener, NumberPicker.OnValueChangeListener {

    Context context;
    SaveData saveData;
    NumberPicker datePicker;
    NumberPicker monthPicker;
    NumberPicker yearPicker;
    UserDetails userDetails;

    String months[] = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};


    public EditBirthday(@NonNull Context context, SaveData saveData, UserDetails userDetails) {
        super(context);
        this.context = context;
        this.saveData = saveData;
        this.userDetails = userDetails;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = getLayoutInflater().inflate(R.layout.fragment_onboarding_birthday,null);
        datePicker = view.findViewById(R.id.date_picker);
        monthPicker = view.findViewById(R.id.month_picker);
        yearPicker = view.findViewById(R.id.year_picker);
        mainFrameLayout.addView(view);
        continueTv.setOnClickListener(this);
        setPickers();
        setData();
        setDialogTitle("Select your birthday.");

    }

    private void setData() {
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
        monthPicker.setOnValueChangedListener(this);
        datePicker.setOnValueChangedListener(this);
        yearPicker.setOnValueChangedListener(this);

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

    @Override
    public void onValueChange(NumberPicker numberPicker, int i, int i1) {
        if(numberPicker.getId() == monthPicker.getId())
        {
            setDatePicker();
        }
        String mm = monthPicker.getValue()<10?"0"+(monthPicker.getValue()+1):(monthPicker.getValue()+1)+"";
        String dd = datePicker.getValue()<10?"0"+datePicker.getValue():datePicker.getValue()+"";

        String yymmdd = (yearPicker.getValue())+"-"+mm+"-"+dd;
        userDetails.setBirthday(yymmdd);

    }

    @Override
    public void onClick(View view) {
        saveData.saveDetails(userDetails);
        dismiss();
    }
}
