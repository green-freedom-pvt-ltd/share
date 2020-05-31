package com.sharesmile.share.profile.editprofiledialogs;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.NumberPicker;

import com.sharesmile.share.R;
import com.sharesmile.share.login.UserDetails;
import com.sharesmile.share.profile.SaveData;
import com.sharesmile.share.utils.Utils;

import java.util.ArrayList;

public class EditWeight extends ParentDialog implements View.OnClickListener, NumberPicker.OnValueChangeListener {

    NumberPicker weightPicker;
    NumberPicker weightDecimalPicker;
    SaveData saveData;
    UserDetails userDetails;

    ArrayList<String> weightStrings;
    ArrayList<String> weightDecimalStrings;
    float userWeight;
    Context context;

    public EditWeight(@NonNull Context context, SaveData saveData, UserDetails userDetails) {
        super(context);
        this.context = context;
        this.userWeight = userDetails.getBodyWeight();
        this.saveData = saveData;
        this.userDetails = userDetails;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = getLayoutInflater().inflate(R.layout.fragment_onboarding_weight,null);
        weightPicker = view.findViewById(R.id.weight_picker);
        weightDecimalPicker = view.findViewById(R.id.weight_decimal_picker);
        mainFrameLayout.addView(view);
        setWeights();
        setData();
        continueTv.setOnClickListener(this);
        setDialogTitle("Select your weight.");

    }

    private void setData() {
        if(weightStrings.contains(((int)userWeight)+""))
        {
            weightPicker.setValue(weightStrings.indexOf(((int)userWeight)+""));
        }
        int decimal = (int) ((userWeight*10)%((int)userWeight));
        weightDecimalPicker.setValue(decimal);
    }

    private void setWeights() {
        weightStrings = new ArrayList<>();
        for(int i=40;i<=150;i++)
        {
            weightStrings.add(i+"");
        }
        String s[] = weightStrings.toArray(new String[weightStrings.size()-1]);
        Utils.setNumberPicker(weightPicker,s,s.length/2);

        weightDecimalStrings = new ArrayList<>();
        for(int i=0;i<=9;i++)
        {
            weightDecimalStrings.add(i+"");
        }
        String sd[] = weightDecimalStrings.toArray(new String[weightDecimalStrings.size()-1]);
        Utils.setNumberPicker(weightDecimalPicker,sd,0);

        weightPicker.setOnValueChangedListener(this);
        weightDecimalPicker.setOnValueChangedListener(this);
    }

    @Override
    public void onClick(View view) {
        saveData.saveDetails(userDetails);
        dismiss();
    }

    @Override
    public void onValueChange(NumberPicker numberPicker, int i, int i1) {
        float w = weightPicker.getValue()+40;
        float wd = weightDecimalPicker.getValue()/10.0f;
        userDetails.setBodyWeight(w+wd);
    }
}
