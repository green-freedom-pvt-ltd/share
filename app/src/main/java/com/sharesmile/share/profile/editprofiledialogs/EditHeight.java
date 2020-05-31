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

import java.util.ArrayList;

public class EditHeight extends ParentDialog implements View.OnClickListener {

    NumberPicker heightPicker,heightUnitPicker;
    Context context;
    ArrayList<String> cmsHeight;
    ArrayList<String> inchHeight;
    String cmsArray[];
    String inchArray[];
    String height = "";
    int heightUnit = 0; // 0=cms 1=inchs
    SaveData saveData;
    UserDetails userDetails;
    public EditHeight(@NonNull Context context, String height, int heightUnit, SaveData saveData,UserDetails userDetails) {
        super(context);
        this.context = context;
        this.height = height;
        this.heightUnit = heightUnit;
        this.saveData = saveData;
        this.userDetails = userDetails;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = getLayoutInflater().inflate(R.layout.fragment_onboarding_height,null);
        heightPicker = view.findViewById(R.id.height_picker);
        heightUnitPicker = view.findViewById(R.id.height_unit_picker);
        mainFrameLayout.addView(view);
        setPicker();
        setData();
        continueTv.setOnClickListener(this);
        setDialogTitle("Select your height.");

    }

    private void setData() {
            if(heightUnit==0)
            {
                heightPicker.setValue(cmsHeight.indexOf(height));
            }else
            {
                heightPicker.setValue(inchHeight.indexOf(height));
            }

        heightUnitPicker.setValue(heightUnit);
    }
    private void setPicker() {
        String units[] = context.getResources().getStringArray(R.array.height_units);
        Utils.setNumberPicker(heightUnitPicker, units, 0);
        cmsHeight = new ArrayList<>();
        inchHeight = new ArrayList<>();

        for (int i = 100; i <= 200; i++) {
            cmsHeight.add(i + "");
            String inchesString = Utils.cmsToInches(i);
            if(!inchHeight.contains(inchesString))
                inchHeight.add(inchesString);
        }
        cmsArray = cmsHeight.toArray(new String[cmsHeight.size()]);
        inchArray = inchHeight.toArray(new String[inchHeight.size()]);
        if(heightUnit == 0)
        {
            Utils.setNumberPicker(heightPicker, cmsArray, heightPicker.getValue());
        }else
        {
            Utils.setNumberPicker(heightPicker, inchArray, heightPicker.getValue());
        }
        heightPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {

                if(heightUnitPicker.getValue()==0)
                {
                    userDetails.setBodyHeight(Integer.parseInt(cmsArray[heightPicker.getValue()]));
                }else
                {
                    String inchesFeet = inchArray[heightPicker.getValue()];
                    String cms = Utils.inchesTocms(inchesFeet);
                    int cms_ = Integer.parseInt(cms);
                    if(cms_>200) {
                        cms_ = 200;
                    }
                    userDetails.setBodyHeight(cms_);
                }
                userDetails.setBodyHeightUnit(heightUnitPicker.getValue());
            }
        });
        heightUnitPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                int cms_ = 0;
                if(i1==0)
                {
                    String inchesFeet = inchArray[heightPicker.getValue()];
                    String cms = Utils.inchesTocms(inchesFeet);
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
                    String inchesFeet = Utils.cmsToInches(Integer.parseInt(cms));
                    Utils.setNumberPicker(heightPicker, inchArray, inchHeight.indexOf(inchesFeet));
                }
                userDetails.setBodyHeight(cms_);
                userDetails.setBodyHeightUnit(i1);
            }
        });
    }

    @Override
    public void onClick(View view) {
        saveData.saveDetails(userDetails);
        dismiss();
    }
}
