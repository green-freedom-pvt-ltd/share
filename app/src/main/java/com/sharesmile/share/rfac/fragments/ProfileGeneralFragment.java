package com.sharesmile.share.rfac.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
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
import com.sharesmile.share.User;
import com.sharesmile.share.UserDao;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.gcm.SyncService;
import com.sharesmile.share.gcm.TaskConstants;
import com.sharesmile.share.utils.SharedPrefsManager;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by apurvgandhwani on 3/29/2016.
 */
public class ProfileGeneralFragment extends Fragment implements RadioGroup.OnCheckedChangeListener, DatePickerDialog.OnDateSetListener, View.OnClickListener {
    @BindView(R.id.et_profile_general_birthday)
    TextView mBirthday;

    @BindView(R.id.et_profile_general_email)
    EditText mEmail;

    @BindView(R.id.et_profile_general_name)
    EditText mName;

    @BindView(R.id.et_profile_general_number)
    EditText mNumber;

    @BindView(R.id.gender_group)
    RadioGroup mRadioGroup;

    @BindView(R.id.rb_share_male)
    RadioButton mMaleRadioBtn;

    @BindView(R.id.rb_share_female)
    RadioButton mFemaleRadioBtn;
    private User mUser;
    private UserDao mUserDao;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUserDao = MainApplication.getInstance().getDbWrapper().getDaoSession().getUserDao();
        int user_id = SharedPrefsManager.getInstance().getInt(Constants.PREF_USER_ID);
        List<User> userList = mUserDao.queryBuilder().where(UserDao.Properties.Id.eq(user_id)).list();
        if (userList != null && !userList.isEmpty()) {
            mUser = userList.get(0);
        }

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

    private void init() {
        mRadioGroup.setOnCheckedChangeListener(this);
        mBirthday.setOnClickListener(this);
        fillUserDetails();
    }

    private void fillUserDetails() {

        if (mUser == null) {
            return;
        }
        if (!TextUtils.isEmpty(mUser.getName())) {
            mName.setText(mUser.getName());
        }

        if (!TextUtils.isEmpty(mUser.getEmailId())) {
            mEmail.setText(mUser.getEmailId());
        }

        if (!TextUtils.isEmpty(mUser.getMobileNO())) {
            mNumber.setText(mUser.getMobileNO());
        }

        if (!TextUtils.isEmpty(mUser.getBirthday())) {
            mBirthday.setText(mUser.getBirthday());
        }
        if (!TextUtils.isEmpty(mUser.getGender())) {
            if (mUser.getGender().equalsIgnoreCase("m")) {
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
        if (mUser == null) {
            return;
        }
        if (!TextUtils.isEmpty(mName.getText())) {
            mUser.setName(mName.getText().toString());
        }

        if (!TextUtils.isEmpty(mBirthday.getText())) {
            mUser.setBirthday(mBirthday.getText().toString());
        }

        if (!TextUtils.isEmpty(mNumber.getText())) {
            mUser.setMobileNO(mNumber.getText().toString());
        }

        if (mFemaleRadioBtn.isChecked() || mMaleRadioBtn.isChecked()) {
            mUser.setGender(mFemaleRadioBtn.isChecked() ? "f" : "m");
        }

        mUserDao.insertOrReplace(mUser);
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

        mBirthday.setText(dayOfMonth + "-" + monthOfYear + "-" + year);
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


