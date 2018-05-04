package com.sharesmile.share.profile;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.sharesmile.share.core.application.MainApplication;
import com.sharesmile.share.R;
import com.sharesmile.share.analytics.Analytics;
import com.sharesmile.share.analytics.events.AnalyticsEvent;
import com.sharesmile.share.analytics.events.Event;
import com.sharesmile.share.core.base.BaseFragment;
import com.sharesmile.share.login.UserDetails;
import com.sharesmile.share.core.sync.SyncHelper;
import com.sharesmile.share.core.Logger;
import com.sharesmile.share.profile.editprofiledialogs.EditBirthday;
import com.sharesmile.share.profile.editprofiledialogs.EditHeight;
import com.sharesmile.share.profile.editprofiledialogs.EditWeight;
import com.sharesmile.share.utils.Utils;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by apurvgandhwani on 3/29/2016.
 */
public class EditProfileFragment extends BaseFragment implements DatePickerDialog.OnDateSetListener,
        View.OnClickListener, TextWatcher , SaveData{

    private static final String TAG = "EditProfileFragment";

    @BindView(R.id.et_profile_general_birthday)
    TextView mBirthday;

    @BindView(R.id.et_profile_general_email)
    EditText mEmail;

    @BindView(R.id.et_profile_first_name)
    EditText mFirstName;

    @BindView(R.id.et_profile_last_name)
    EditText mLastName;

    @BindView(R.id.et_profile_general_number)
    EditText mNumber;

    @BindView(R.id.et_body_weight_kg)
    EditText bodyWeightKgs;

    @BindView(R.id.et_body_height)
    EditText bodyHeight;

    @BindView(R.id.et_body_height_unit)
    EditText bodyHeightUnit;

    @BindView(R.id.rb_share_male)
    TextView mMaleRadioBtn;

    @BindView(R.id.rb_share_female)
    TextView mFemaleRadioBtn;

    @BindView(R.id.lt_body_height)
    LinearLayout bodyHeightLayout;

    int gender = -1;

    private UserDetails userDetails;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userDetails = MainApplication.getInstance().getUserDetails();
        Gson gson = new Gson();
        Logger.d(TAG, "onCreate, MemberDetails: " + gson.toJson(userDetails));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_edit_profile, null);
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
        boolean b = checkUser();
        if(b)
        {
            Utils.setMenuText(menu.findItem(R.id.item_save_profile),getContext(),getString(R.string.save),getResources().getColor(R.color.black_38));
        }else
        {
            Utils.setMenuText(menu.findItem(R.id.item_save_profile),getContext(),getString(R.string.save),getResources().getColor(R.color.colorPrimaryDark));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_save_profile:
                if(!checkUser()) {
                    if (validateUserDetails()) {
                        saveUserDetails();
                        isEdited = false;
                        getActivity().onBackPressed();
                    }
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void init() {
        bodyHeightLayout.setOnClickListener(this);
        mFemaleRadioBtn.setOnClickListener(this);
        mMaleRadioBtn.setOnClickListener(this);
        mBirthday.setOnClickListener(this);
        bodyWeightKgs.setOnClickListener(this);
        fillUserDetails();
        setupToolbar();
        setTextWatcher();
    }

    private void setupToolbar() {
        setHasOptionsMenu(true);
        setToolbarTitle(getResources().getString(R.string.edit_profile));
    }

    private void setTextWatcher(){
        mFirstName.addTextChangedListener(this);
        mLastName.addTextChangedListener(this);
        mNumber.addTextChangedListener(this);
        bodyWeightKgs.addTextChangedListener(this);
        mBirthday.addTextChangedListener(this);
    }

    private void fillUserDetails() {

        if (userDetails == null) {
            return;
        }
        if (!TextUtils.isEmpty(userDetails.getFirstName())) {
            mFirstName.setText(userDetails.getFirstName());
        }

        if (!TextUtils.isEmpty(userDetails.getLastName())) {
            mLastName.setText(userDetails.getLastName());
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

        if (userDetails.getBodyHeight() > 0f) {
            if(userDetails.getBodyHeightUnit() == 0) {
                bodyHeight.setText(userDetails.getBodyHeight()+"");
                bodyHeightUnit.setText("cms");
            }else
            {
                bodyHeight.setText(Utils.cmsToInches(userDetails.getBodyHeight()) + "");
                bodyHeightUnit.setText("");
            }
        }

        if (!TextUtils.isEmpty(userDetails.getBirthday())) {
            mBirthday.setText(userDetails.getBirthday());
        }
        if (!TextUtils.isEmpty(userDetails.getGenderUser())) {
            String gender = MainApplication.getInstance().getUserDetails().getGenderUser();
            if(gender.toLowerCase().startsWith("m"))
            {
                this.gender = 1;
            }else if(gender.toLowerCase().startsWith("f"))
            {
                this.gender = 0;
            }
            setGender();
        }
        setMenuColor();
    }
    void setGender()
    {
        if(gender == 1)
        {
            mMaleRadioBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.radio_selected,0,0,0);
            mFemaleRadioBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.radio_unselected,0,0,0);
        }else if(gender == 0)
        {
            mMaleRadioBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.radio_unselected,0,0,0);
            mFemaleRadioBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.radio_selected,0,0,0);
        }
    }

    private boolean validateUserDetails(){
        if (mFirstName.getText().toString().isEmpty()){
            // Name not valid
            MainApplication.showToast("Please enter a valid name");
            return false;
        }
        if (mLastName.getText().toString().isEmpty()){
            // Name not valid
            MainApplication.showToast("Please enter a valid name");
            return false;
        }
        if (!validatePhoneNumber(mNumber.getText().toString())){
            return false;
        }
        try {
            String inputWeight = bodyWeightKgs.getText().toString();
            if (!TextUtils.isEmpty(inputWeight)){
                // Go for validation only when weight box is empty
                float weight = Float.parseFloat(inputWeight);
                if (weight < 10 || weight > 200){
                    MainApplication.showToast(R.string.enter_actual_weight);
                    return false;
                }
            }
        }catch (Exception e){
            Logger.e(TAG, "Exception while parsing body weight: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void setMenuColor()
    {
        getActivity().invalidateOptionsMenu();
    }

    private boolean checkUser()
    {
        UserDetails userDetails = MainApplication.getInstance().getUserDetails();
        boolean b = true;
        if(!mFirstName.getText().toString().equals(userDetails.getFirstName()))
        {
            b = false;
        }
        if(!mLastName.getText().toString().equals(userDetails.getLastName()))
        {
            b = false;
        }
        if(!mEmail.getText().toString().equals(userDetails.getEmail()==null?"":userDetails.getEmail()))
        {
            b = false;
        }
        if(!mNumber.getText().toString().equals(userDetails.getPhoneNumber()==null?"":userDetails.getPhoneNumber()))
        {
            b = false;
        }
        if(!bodyWeightKgs.getText().toString().equals(userDetails.getBodyWeight()+""))
        {
            b = false;
        }
        int bodyHeight = userDetails.getBodyHeight();
        String bodyHeightString = "";
        if(userDetails.getBodyHeightUnit() == 0)
        {
            bodyHeightString = bodyHeight+"";
        }else
        {
            bodyHeightString = Utils.cmsToInches(bodyHeight);
        }
        if(!this.bodyHeight.getText().toString().equals(bodyHeightString))
        {
            b = false;
        }
        if(!mBirthday.getText().toString().equals(userDetails.getBirthday()))
        {
            b = false;
        }
        String gender = "";
        if(this.gender == 0)
        {
            gender = "f";
        }else if(this.gender == 1)
        {
            gender = "m";
        }
        if(!userDetails.getGenderUser().toLowerCase().startsWith(gender))
        {
            b = false;
        }
        return b;
    }
    private boolean validatePhoneNumber(String number){
        if (TextUtils.isEmpty(number)){
            // Will not perform validation if there is no input for phone number
            return true;
        }
        if ( "IN".equalsIgnoreCase(Utils.getUserCountry(MainApplication.getContext())) ){
            // If it is an Indian Phone Number
            if (!Utils.isValidIndianPhoneNumber(number)){
                MainApplication.showToast("Please enter a valid 10 digit phone number");
                return false;
            }
        }else {
            // International Phone Number
            if (!Utils.isValidInternationalPhoneNumber(number)){
                MainApplication.showToast("Please enter a valid phone number");
                return false;
            }
        }
        return true;
    }

    private void saveUserDetails() {
        if (userDetails == null) {
            return;
        }

        StringBuilder fullNameBuilder = new StringBuilder();

        if (!TextUtils.isEmpty(mFirstName.getText())) {
            userDetails.setFirstName(mFirstName.getText().toString());
            fullNameBuilder.append(mFirstName.getText().toString());
        }

        if (!TextUtils.isEmpty(mLastName.getText())) {
            userDetails.setLastName(mLastName.getText().toString());
            fullNameBuilder.append(" ");
            fullNameBuilder.append(mLastName.getText().toString());
        }

        String fullName = fullNameBuilder.toString();
        if (!TextUtils.isEmpty(fullName)){
            Analytics.getInstance().setUserName(fullName);
            AnalyticsEvent.create(Event.ON_SET_NAME)
                    .put("user_name", fullName)
                    .buildAndDispatch();
        }


        if (!TextUtils.isEmpty(mBirthday.getText().toString())) {
            Logger.d(TAG, "Setting User Birthday as " + mBirthday.getText().toString());
            userDetails.setBirthday(mBirthday.getText().toString());
            AnalyticsEvent.create(Event.ON_SET_BIRTTHDAY)
                    .put("user_birthday", mBirthday.getText().toString())
                    .buildAndDispatch();
        }

        if (!TextUtils.isEmpty(mNumber.getText())) {
            if (Utils.isValidInternationalPhoneNumber(mNumber.getText().toString())){
                userDetails.setPhoneNumber(mNumber.getText().toString());
                Analytics.getInstance().setUserPhone(mNumber.getText().toString());
                AnalyticsEvent.create(Event.ON_SET_PHONE_NUM)
                        .put("user_phone_num", mNumber.getText().toString())
                        .buildAndDispatch();
            }
        }

        if (!TextUtils.isEmpty(bodyWeightKgs.getText())) {
            try {
                float bodyWeightEntered = Float.parseFloat(bodyWeightKgs.getText().toString());
                userDetails.setBodyWeight(bodyWeightEntered);
                Analytics.getInstance().setUserProperty("body_weight", bodyWeightEntered);
                AnalyticsEvent.create(Event.ON_SET_BODY_WEIGHT)
                        .put("body_weight", bodyWeightEntered)
                        .buildAndDispatch();
            }catch (Exception e){
                Logger.e("EditProfileFragment", "Exception while parsing body weight: " + e.getMessage() );
                e.printStackTrace();
            }
        }


            if (gender == 0){
                userDetails.setGenderUser("f");
                Analytics.getInstance().setUserGender("F");
            }else if(gender == 1){
                userDetails.setGenderUser("m");
                Analytics.getInstance().setUserGender("M");
            }

        MainApplication.getInstance().setUserDetails(userDetails);
        SyncHelper.oneTimeUploadUserData();
        MainApplication.showToast("Saved!");
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
//                showDatePicker();
                EditBirthday editBirthday = new EditBirthday(getContext(),this,userDetails);
                editBirthday.show();
                break;
            case R.id.rb_share_male :
                gender = 1;
                setGender();
                setMenuColor();
                break;
            case R.id.rb_share_female :
                gender = 0;
                setGender();
                setMenuColor();
                break;
            case R.id.lt_body_height :
                EditHeight editHeight = new EditHeight(getContext(),bodyHeight.getText().toString(),userDetails.getBodyHeightUnit(),this,userDetails);
                editHeight.show();
                break;
            case R.id.et_body_weight_kg :
                EditWeight editWeight = new EditWeight(getContext(),this,userDetails);
                editWeight.show();
                break;
        }
    }

    @Override
    protected boolean handleBackPress() {
        Logger.d(TAG, "handleBackPress");
        if (isEdited){
            // Show Discard changes dialog
            discardChangesDialog = showDiscardChangesDialog();
            return true;
        }else {
            return super.handleBackPress();
        }
    }

    @Override
    public void onDestroyView() {
        if (discardChangesDialog != null){
            discardChangesDialog.dismiss();
        }
        super.onDestroyView();
    }

    private AlertDialog discardChangesDialog;

    public AlertDialog showDiscardChangesDialog() {
        Logger.d(TAG, "showDiscardChangesDialog");
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(getString(R.string.edit_profile)).setMessage(getString(R.string.discard_changes));
        builder.setPositiveButton("DISCARD", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                isEdited = false;
                getActivity().onBackPressed();
            }
        });
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing
                return;
            }
        });
        discardChangesDialog = builder.create();
        discardChangesDialog.show();
        return discardChangesDialog;
    }

    private boolean isEdited = false;

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        setMenuColor();
    }

    @Override
    public void afterTextChanged(Editable s) {
        // Some text is changed
        Logger.i(TAG, "afterTextChanged: " + s.toString());
        isEdited = true;
    }

    @Override
    public void saveDetails(UserDetails userDetails) {
        this.userDetails = userDetails;
        fillUserDetails();
    }
}


