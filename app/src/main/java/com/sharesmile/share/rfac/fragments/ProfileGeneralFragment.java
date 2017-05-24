package com.sharesmile.share.rfac.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.OneoffTask;
import com.google.android.gms.gcm.Task;
import com.sharesmile.share.MainApplication;
import com.sharesmile.share.R;
import com.sharesmile.share.analytics.Analytics;
import com.sharesmile.share.core.BaseFragment;
import com.sharesmile.share.gcm.SyncService;
import com.sharesmile.share.gcm.TaskConstants;
import com.sharesmile.share.rfac.models.UserDetails;
import com.sharesmile.share.utils.Logger;
import com.sharesmile.share.utils.Utils;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by apurvgandhwani on 3/29/2016.
 */
public class ProfileGeneralFragment extends BaseFragment implements RadioGroup.OnCheckedChangeListener, DatePickerDialog.OnDateSetListener, View.OnClickListener {
    @BindView(R.id.et_profile_general_birthday)
    TextView mBirthday;

    @BindView(R.id.et_profile_general_email)
    EditText mEmail;

    @BindView(R.id.et_profile_general_name)
    EditText mName;

    @BindView(R.id.et_profile_general_number)
    EditText mNumber;

    @BindView(R.id.et_body_weight_kg)
    EditText bodyWeightKgs;

    @BindView(R.id.gender_group)
    RadioGroup mRadioGroup;

    @BindView(R.id.rb_share_male)
    RadioButton mMaleRadioBtn;

    @BindView(R.id.rb_share_female)
    RadioButton mFemaleRadioBtn;

    private UserDetails userDetails;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userDetails = MainApplication.getInstance().getUserDetails();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_profile_general, null);
        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        ButterKnife.bind(this, v);
        init();
        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_profile_save, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_save_profile:
                getActivity().onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void init() {
        mRadioGroup.setOnCheckedChangeListener(this);
        mBirthday.setOnClickListener(this);
        fillUserDetails();
        setupToolbar();
    }

    private void setupToolbar() {
        setHasOptionsMenu(true);
        setToolbarTitle(getResources().getString(R.string.edit_profile));
    }

    private void fillUserDetails() {

        if (userDetails == null) {
            return;
        }
        if (!TextUtils.isEmpty(userDetails.getFirstName())) {
            mName.setText(userDetails.getFirstName());
        }

        if (!TextUtils.isEmpty(userDetails.getEmail())) {
            mEmail.setText(userDetails.getEmail());
        }

        if (!TextUtils.isEmpty(userDetails.getPhoneNumber())) {
            mNumber.setText(userDetails.getPhoneNumber());
        }

        if (userDetails.getBodyWeight() > 0f) {
            bodyWeightKgs.setText(userDetails.getBodyWeight() + "");
        }

        if (!TextUtils.isEmpty(userDetails.getBirthday())) {
            mBirthday.setText(userDetails.getBirthday());
        }
        if (!TextUtils.isEmpty(userDetails.getGenderUser())) {
            if (userDetails.getGenderUser().equalsIgnoreCase("m")) {
                mMaleRadioBtn.setChecked(true);
            } else {
                mFemaleRadioBtn.setChecked(true);
            }
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {

    }


    @Override
    public void onPause() {
        super.onPause();
        saveUserDetails();

    }

    private void saveUserDetails() {
        if (userDetails == null) {
            return;
        }
        if (!TextUtils.isEmpty(mName.getText())) {
            userDetails.setFirstName(mName.getText().toString());
            Analytics.getInstance().setUserName(mName.getText().toString());
        }

        if (!TextUtils.isEmpty(mBirthday.getText())) {
            userDetails.setBirthday(mBirthday.getText().toString());
        }

        if (!TextUtils.isEmpty(mNumber.getText())) {
            if (Utils.isValidPhoneNumber(mNumber.getText().toString())){
                userDetails.setPhoneNumber(mNumber.getText().toString());
                Analytics.getInstance().setUserPhone(mNumber.getText().toString());
            }
        }

        if (!TextUtils.isEmpty(bodyWeightKgs.getText())) {
            try {
                float bodyWeightEntered = Float.parseFloat(bodyWeightKgs.getText().toString());
                userDetails.setBodyWeight(bodyWeightEntered);
                Analytics.getInstance().setUserProperty("body_weight", bodyWeightEntered);
            }catch (Exception e){
                Logger.e("ProfileGeneralFragment", "Exception while parsing body weight: " + e.getMessage() );
                e.printStackTrace();
            }
        }

        if (mFemaleRadioBtn.isChecked() || mMaleRadioBtn.isChecked()) {
            if (mFemaleRadioBtn.isChecked()){
                userDetails.setGenderUser("f");
                Analytics.getInstance().setUserGender("F");
            }else {
                userDetails.setGenderUser("m");
                Analytics.getInstance().setUserGender("M");
            }
        }

        MainApplication.getInstance().setUserDetails(userDetails);

        syncUserData();

    }

    @Override
    public void onStop() {
        super.onStop();

    }

    public void showDatePicker() {

        Calendar calendar = Calendar.getInstance();
        int calender_month = calendar.get(Calendar.MONTH);
        int calender_year = calendar.get(Calendar.YEAR);
        int calender_day = calendar.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog mDatePickerDialog = new DatePickerDialog(getActivity(), this, calender_year, calender_month, calender_day);
        mDatePickerDialog.show();


    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

        mBirthday.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.et_profile_general_birthday:
                showDatePicker();
        }
    }

    private void syncUserData() {
        OneoffTask task = new OneoffTask.Builder()
                .setService(SyncService.class)
                .setTag(TaskConstants.UPLOAD_USER_DATA)
                .setExecutionWindow(0L, 3600L)
                .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED).setPersisted(true)
                .build();

        GcmNetworkManager mGcmNetworkManager = GcmNetworkManager.getInstance(getContext().getApplicationContext());
        mGcmNetworkManager.schedule(task);
    }
}


