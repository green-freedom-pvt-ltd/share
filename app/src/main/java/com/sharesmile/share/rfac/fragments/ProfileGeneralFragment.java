package com.sharesmile.share.rfac.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.sharesmile.share.MainApplication;
import com.sharesmile.share.R;
import com.sharesmile.share.User;
import com.sharesmile.share.UserDao;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by apurvgandhwani on 3/29/2016.
 */
public class ProfileGeneralFragment extends Fragment implements RadioGroup.OnCheckedChangeListener {
    @BindView(R.id.et_profile_general_birthday)
    EditText mBirthday;

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
        List<User> userList = mUserDao.queryBuilder().where(UserDao.Properties.Id.eq(1)).list();
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


    }

    @Override
    public void onStop() {
        super.onStop();

    }
}


