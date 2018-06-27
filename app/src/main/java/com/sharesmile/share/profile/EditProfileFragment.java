package com.sharesmile.share.profile;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3Client;
import com.google.gson.Gson;
import com.sharesmile.share.AchievedTitle;
import com.sharesmile.share.AchievedTitleDao;
import com.sharesmile.share.R;
import com.sharesmile.share.Title;
import com.sharesmile.share.TitleDao;
import com.sharesmile.share.analytics.Analytics;
import com.sharesmile.share.analytics.events.AnalyticsEvent;
import com.sharesmile.share.analytics.events.Event;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.core.Logger;
import com.sharesmile.share.core.ShareImageLoader;
import com.sharesmile.share.core.application.MainApplication;
import com.sharesmile.share.core.base.BaseFragment;
import com.sharesmile.share.core.config.Urls;
import com.sharesmile.share.core.event.UpdateEvent;
import com.sharesmile.share.core.sync.SyncHelper;
import com.sharesmile.share.login.UserDetails;
import com.sharesmile.share.network.NetworkUtils;
import com.sharesmile.share.profile.editprofiledialogs.EditBirthday;
import com.sharesmile.share.profile.editprofiledialogs.EditHeight;
import com.sharesmile.share.profile.editprofiledialogs.EditWeight;
import com.sharesmile.share.utils.Utils;
import com.sharesmile.share.views.CircularImageView;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.List;

import base.BaseDialog;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.app.Activity.RESULT_OK;


/**
 * Created by apurvgandhwani on 3/29/2016.
 */
public class EditProfileFragment extends BaseFragment implements DatePickerDialog.OnDateSetListener,
        View.OnClickListener, TextWatcher, SaveData {

    private static final String TAG = "EditProfileFragment";

    @BindView(R.id.et_profile_general_birthday)
    TextView mBirthday;
    @BindView(R.id.tv_edit_profile_birthday_error)
    TextView mBirthdayError;

    @BindView(R.id.et_profile_general_email)
    EditText mEmail;
    @BindView(R.id.tv_edit_profile_email_error)
    TextView mEmailError;

    @BindView(R.id.et_profile_first_name)
    EditText mFirstName;
    @BindView(R.id.tv_edit_profile_first_name_error)
    TextView mFirstNameError;

    @BindView(R.id.et_profile_last_name)
    EditText mLastName;
    @BindView(R.id.tv_edit_profile_last_name_error)
    TextView mLastNameError;

    @BindView(R.id.et_profile_general_number)
    EditText mNumber;
    @BindView(R.id.tv_edit_profile_phone_number_error)
    TextView mNumberError;

    @BindView(R.id.et_body_weight_kg)
    TextView bodyWeightKgs;
    @BindView(R.id.tv_edit_profile_body_weight_error)
    TextView bodyWeightKgsError;

    @BindView(R.id.et_body_height)
    TextView bodyHeight;
    @BindView(R.id.et_body_height_unit)
    TextView bodyHeightUnit;
    @BindView(R.id.tv_edit_profile_body_height_error)
    TextView bodyHeightError;


    @BindView(R.id.et_profile_title1)
    TextView profileTitle1;
    @BindView(R.id.spinner_profile_title1)
    Spinner spinnerProfileTitle1;
    @BindView(R.id.tv_edit_profile_title_1_error)
    TextView profileTitle1Error;

    @BindView(R.id.et_profile_title2)
    TextView profileTitle2;
    @BindView(R.id.spinner_profile_title2)
    Spinner spinnerProfileTitle2;
    @BindView(R.id.tv_edit_profile_title_2_error)
    TextView profileTitle2Error;


    @BindView(R.id.rb_share_male)
    TextView mMaleRadioBtn;
    @BindView(R.id.rb_share_female)
    TextView mFemaleRadioBtn;
    @BindView(R.id.tv_edit_profile_gender_error)
    TextView genderError;

    @BindView(R.id.lt_body_height)
    LinearLayout bodyHeightLayout;

    @BindView(R.id.img_profile)
    CircularImageView imgProfile;

    @BindView(R.id.edit_profile_img)
    ImageView editProfileImg;
    @BindView(R.id.progress_bar)
    RelativeLayout progressBar;

    File tempPhotoFile;
    File photoFile;
    private String profilePicUrl;

    int gender = -1;

    private UserDetails userDetails;
    EditProfileImageDialog editProfileImageDialog;
    boolean isImageLoaded = false;

    List<AchievedTitle> achievedTitles;
    int pos1=-1,pos2=-1;


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
        View v = inflater.inflate(R.layout.fragment_edit_profile_v2, null);
        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        ButterKnife.bind(this, v);
        EventBus.getDefault().register(this);

        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_profile_save, menu);
        super.onCreateOptionsMenu(menu, inflater);
        boolean b = checkUser();
        if (b) {
            Utils.setMenuText(menu.findItem(R.id.item_save_profile), getContext(), getString(R.string.save), getResources().getColor(R.color.black_38));
        } else {
            Utils.setMenuText(menu.findItem(R.id.item_save_profile), getContext(), getString(R.string.save), getResources().getColor(R.color.colorPrimaryDark));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_save_profile:
                if (progressBar.getVisibility() == View.VISIBLE) {
                    MainApplication.showToast("Upload in process, please wait.");
                } else if (NetworkUtils.isNetworkConnected(getContext())) {
                    if (!checkUser()) {
                        if (validateUserDetails()) {
                            isEdited = false;
                            if (photoFile != null)
                                uploadWithTransferUtility();
                            else
                                saveUserDetails();

                        }
                    }
                } else {
                    MainApplication.showToast(getResources().getString(R.string.connect_to_internet));
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void init() {
        bodyHeightLayout.setOnClickListener(this);
        bodyHeight.setOnClickListener(this);
        bodyHeightUnit.setOnClickListener(this);
        mFemaleRadioBtn.setOnClickListener(this);
        mMaleRadioBtn.setOnClickListener(this);
        mBirthday.setOnClickListener(this);
        bodyWeightKgs.setOnClickListener(this);
        profileTitle1.setOnClickListener(this);
        profileTitle2.setOnClickListener(this);
        getTitleInList();
        setSpinner();
        fillUserDetails();
        setupToolbar();
        setTextWatcher();
    }

    private void setSpinner() {
        //TODO : Check for toString() method in AchievedTitle
        ArrayAdapter<AchievedTitle> achievedTitleArrayAdapter1 = new ArrayAdapter<AchievedTitle>(getContext(),android.R.layout.simple_list_item_1,achievedTitles);
        spinnerProfileTitle1.setAdapter(achievedTitleArrayAdapter1);
        spinnerProfileTitle1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if(position == pos2)
                {
                    MainApplication.showToast("Title 1 & 2 cannot be same");
                }else
                {
                    long titleId = achievedTitles.get(position).getId();
                    pos1 = position;
                    profileTitle1.setText(achievedTitles.get(position).getTitle());
                }
                setMenuColor();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        ArrayAdapter<AchievedTitle> achievedTitleArrayAdapter2 = new ArrayAdapter<AchievedTitle>(getContext(),android.R.layout.simple_list_item_1,achievedTitles);
        spinnerProfileTitle2.setAdapter(achievedTitleArrayAdapter2);
        spinnerProfileTitle2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if(position == pos1)
                {
                    MainApplication.showToast("Title 1 & 2 cannot be same");
                }else
                {
                    long titleId = achievedTitles.get(position).getId();
                    pos2 = position;
                    profileTitle2.setText(achievedTitles.get(position).getTitle());
                }
                setMenuColor();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void getTitleInList() {
        AchievedTitleDao achievedTitleDao = MainApplication.getInstance().getDbWrapper().getAchievedTitleDao();
        achievedTitles = achievedTitleDao.queryBuilder()
                .where(AchievedTitleDao.Properties.UserId.eq(MainApplication.getInstance().getUserID())).list();
    }

    private void setupToolbar() {
        setHasOptionsMenu(true);
        setToolbarTitle(getResources().getString(R.string.edit_profile));
    }

    private void setTextWatcher() {
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

        if(userDetails.getTitle1()>0 || userDetails.getTitle2()>0)
        {
            for(int i=0;i<achievedTitles.size();i++)
            {
                if(userDetails.getTitle1()>0)
                {
                    if(achievedTitles.get(i).getTitleId() == userDetails.getTitle1())
                    {
                        pos1 = i;
                        profileTitle1.setText(achievedTitles.get(i).getTitle());
                    }
                }

                if(userDetails.getTitle2()>0)
                {
                    if(achievedTitles.get(i).getTitleId() == userDetails.getTitle2())
                    {
                        pos2 = i;
                        profileTitle2.setText(achievedTitles.get(i).getTitle());
                    }
                }
            }
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
            if (userDetails.getBodyHeightUnit() == 0) {
                bodyHeight.setText(userDetails.getBodyHeight() + "");
                bodyHeightUnit.setText("cms");
            } else {
                bodyHeight.setText(Utils.cmsToInches(userDetails.getBodyHeight()) + "");
                bodyHeightUnit.setText("");
            }
        }

        if (!TextUtils.isEmpty(userDetails.getBirthday())) {
            mBirthday.setText(userDetails.getBirthday());
        }
        if (!TextUtils.isEmpty(userDetails.getGenderUser())) {
            String gender = MainApplication.getInstance().getUserDetails().getGenderUser();
            if (gender.toLowerCase().startsWith("m")) {
                this.gender = 1;
            } else if (gender.toLowerCase().startsWith("f")) {
                this.gender = 0;
            }
            setGender();
        }
        setMenuColor();

        if (isImageLoaded) {
            Picasso.with(getContext()).load(photoFile).into(imgProfile);
            isImageLoaded = false;
        } else if (!TextUtils.isEmpty(userDetails.getProfilePicture())) {
            ShareImageLoader.getInstance().loadImage(Urls.getImpactProfileS3BucketUrl() + userDetails.getProfilePicture(), imgProfile,
                    ContextCompat.getDrawable(getContext(), R.drawable.placeholder_profile));
        } else if (!TextUtils.isEmpty(userDetails.getSocialThumb())) {
            ShareImageLoader.getInstance().loadImage(userDetails.getSocialThumb(), imgProfile,
                    ContextCompat.getDrawable(getContext(), R.drawable.placeholder_profile));
        }

    }

    void setGender() {
        if (gender == 1) {
            mMaleRadioBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.radio_selected, 0, 0, 0);
            mFemaleRadioBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.radio_unselected, 0, 0, 0);
        } else if (gender == 0) {
            mMaleRadioBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.radio_unselected, 0, 0, 0);
            mFemaleRadioBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.radio_selected, 0, 0, 0);
        }
    }

    private boolean validateUserDetails() {
        boolean validDetails = true;
        if (mFirstName.getText().toString().isEmpty()) {
            // Name not valid
            mFirstNameError.setText("*Please enter a valid first name");
            validDetails = false;
        } else {
            mFirstNameError.setText("");
        }
        if (mLastName.getText().toString().isEmpty()) {
            // Name not valid
            mLastNameError.setText("*Please enter a valid last name");
            validDetails = false;
        } else {
            mLastNameError.setText("");
        }
        if (!validatePhoneNumber(mNumber.getText().toString())) {
            mNumberError.setText("*Please enter a valid number");
            validDetails = false;
        } else {
            mNumberError.setText("");
        }
        return validDetails;
    }

    private void setMenuColor() {
        getActivity().invalidateOptionsMenu();
    }

    private boolean checkUser() {
        UserDetails userDetails = MainApplication.getInstance().getUserDetails();
        boolean b = true;
        if (photoFile != null) {
            b = false;
        }
        if (!mFirstName.getText().toString().equals(userDetails.getFirstName())) {
            b = false;
        }
        if (!mLastName.getText().toString().equals(userDetails.getLastName())) {
            b = false;
        }
        if (!mEmail.getText().toString().equals(userDetails.getEmail() == null ? "" : userDetails.getEmail())) {
            b = false;
        }
        if (!mNumber.getText().toString().equals(userDetails.getPhoneNumber() == null ? "" : userDetails.getPhoneNumber())) {
            b = false;
        }
        if (!bodyWeightKgs.getText().toString().equals(userDetails.getBodyWeight() + "")) {
            b = false;
        }
        int bodyHeight = userDetails.getBodyHeight();
        String bodyHeightString = "";
        if (userDetails.getBodyHeightUnit() == 0) {
            bodyHeightString = bodyHeight + "";
        } else {
            bodyHeightString = Utils.cmsToInches(bodyHeight);
        }
        if (!this.bodyHeight.getText().toString().equals(bodyHeightString)) {
            b = false;
        }
        if (!mBirthday.getText().toString().equals(userDetails.getBirthday())) {
            b = false;
        }
        String gender = "";
        if (this.gender == 0) {
            gender = "f";
        } else if (this.gender == 1) {
            gender = "m";
        }
        if (!userDetails.getGenderUser().toLowerCase().startsWith(gender)) {
            b = false;
        }
        if(achievedTitles.size()>=3)
        {
            if(userDetails.getTitle1()!=achievedTitles.get(pos1).getTitleId() ||
                    userDetails.getTitle2()!=achievedTitles.get(pos2).getTitleId())
            {
                b = false;
            }
        }
        return b;
    }

    private boolean validatePhoneNumber(String number) {
        if (TextUtils.isEmpty(number)) {
            // Will not perform validation if there is no input for phone number
            return true;
        }
        if ("IN".equalsIgnoreCase(Utils.getUserCountry(MainApplication.getContext()))) {
            // If it is an Indian Phone Number
            if (!Utils.isValidIndianPhoneNumber(number)) {
                mNumberError.setText("*Please enter a valid 10 digit phone number");
                return false;
            }
        } else {
            // International Phone Number
            if (!Utils.isValidInternationalPhoneNumber(number)) {
                mNumberError.setText("*Please enter a valid phone number");
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
        if (!TextUtils.isEmpty(profilePicUrl)) {
            userDetails.setProfilePicture(profilePicUrl);
            AnalyticsEvent.create(Event.ON_UPDATE_PROFILEPIC)
                    .put("profile_pic_url", profilePicUrl)
                    .buildAndDispatch();
        }

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
        if (!TextUtils.isEmpty(fullName)) {
            Analytics.getInstance().setUserName(fullName);
            AnalyticsEvent.create(Event.ON_SET_NAME)
                    .put("user_name", fullName)
                    .buildAndDispatch();
        }


        if (!TextUtils.isEmpty(mBirthday.getText().toString())) {
            Logger.d(TAG, "Setting User Birthday as " + mBirthday.getText().toString());
            userDetails.setBirthday(mBirthday.getText().toString());
            AnalyticsEvent.create(Event.ON_SET_BIRTHDAY)
                    .put("user_birthday", mBirthday.getText().toString())
                    .buildAndDispatch();
        }

        if (!TextUtils.isEmpty(mNumber.getText())) {
            if (Utils.isValidInternationalPhoneNumber(mNumber.getText().toString())) {
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
            } catch (Exception e) {
                Logger.e("EditProfileFragment", "Exception while parsing body weight: " + e.getMessage());
                e.printStackTrace();
            }
        }


        if (gender == 0) {
            userDetails.setGenderUser("f");
            Analytics.getInstance().setUserGender("F");
        } else if (gender == 1) {
            userDetails.setGenderUser("m");
            Analytics.getInstance().setUserGender("M");
        }

        MainApplication.getInstance().setUserDetails(userDetails);
        SyncHelper.oneTimeUploadUserData();
        MainApplication.showToast("Saved!");
        getActivity().onBackPressed();
    }

    @Override
    public void onStop() {
        super.onStop();

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
                EditBirthday editBirthday = new EditBirthday(getContext(), this, userDetails);
                editBirthday.show();
                break;
            case R.id.rb_share_male:
                gender = 1;
                setGender();
                setMenuColor();
                break;
            case R.id.rb_share_female:
                gender = 0;
                setGender();
                setMenuColor();
                break;
            case R.id.lt_body_height:
            case R.id.et_body_height:
            case R.id.et_body_height_unit:
                EditHeight editHeight = new EditHeight(getContext(), bodyHeight.getText().toString(), userDetails.getBodyHeightUnit(), this, userDetails);
                editHeight.show();
                break;
            case R.id.et_body_weight_kg:
                EditWeight editWeight = new EditWeight(getContext(), this, userDetails);
                editWeight.show();
                break;
            case R.id.et_profile_title1 :
                if(userDetails.getTitle1()>0)
                {

                }else
                {
                    MainApplication.showToast(getResources().getString(R.string.charity_overview_title_header_earn_stars_to_get_title));
                }
                break;
            case R.id.et_profile_title2 :
                if(userDetails.getTitle2()>0)
                {

                }else
                {
                    MainApplication.showToast(getResources().getString(R.string.charity_overview_title_header_earn_stars_to_get_title));
                }
                break;
        }
    }

    @Override
    protected boolean handleBackPress() {
        Logger.d(TAG, "handleBackPress");
        if (isEdited) {
            // Show Discard changes dialog
            discardChangesDialog = showDiscardChangesDialog();
            return true;
        } else {
            return super.handleBackPress();
        }
    }

    @Override
    public void onDestroyView() {
        if (discardChangesDialog != null) {
            discardChangesDialog.dismiss();
        }
        EventBus.getDefault().unregister(this);
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

    @OnClick(R.id.edit_profile_img)
    void editProfileImg() {
        if (editProfileImageDialog != null) {
            editProfileImageDialog.dismiss();
        }
        editProfileImageDialog = new EditProfileImageDialog(getActivity(), R.style.BackgroundDimDialog);
        editProfileImageDialog.setListener(new BaseDialog.Listener() {
            @Override
            public void onPrimaryClick(BaseDialog dialog) {
                // TakePhoto
                dialog.dismiss();
                takePhotoPermission();
            }

            @Override
            public void onSecondaryClick(BaseDialog dialog) {
                // Choose existing photo
                dialog.dismiss();
                chooseExistingPhotoPermission();
            }
        });
        editProfileImageDialog.show();
    }

    public void takePhotoPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if ((ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) &&
                    (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED)) {
                getActivity().requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, Constants.CODE_REQUEST_IMAGE_CAPTURE_PERMISSION);
            } else {
                dispatchTakePictureIntent();
            }
        } else {
            dispatchTakePictureIntent();
        }
    }

    public void chooseExistingPhotoPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                getActivity().requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Constants.CODE_REQUEST_IMAGE_FROM_GALLERY_PERMISSION);
            } else {
                dispatchGetPictureFromGalleryIntent();
            }
        } else {
            dispatchGetPictureFromGalleryIntent();
        }
    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getContext().getPackageManager()) != null) {

            try {
                tempPhotoFile = Utils.createImageFile(getContext());
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (tempPhotoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(getContext(), Utils.getFileProvider(getContext()), tempPhotoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                getActivity().startActivityForResult(takePictureIntent, Constants.REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private void dispatchGetPictureFromGalleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        getActivity().startActivityForResult(Intent.createChooser(intent, "Select Picture"), Constants.REQUEST_IMAGE_CAPTURE);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(UpdateEvent.EditImagePermissionGranted editImagePermissionGranted) {
        if (editImagePermissionGranted.getRequestCode() == Constants.CODE_REQUEST_IMAGE_CAPTURE_PERMISSION) {
            dispatchTakePictureIntent();
        } else if (editImagePermissionGranted.getRequestCode() == Constants.CODE_REQUEST_IMAGE_FROM_GALLERY_PERMISSION) {
            dispatchGetPictureFromGalleryIntent();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(UpdateEvent.ImageCapture imageCapture) {
        if(imageCapture.getResultCode() == RESULT_OK) {
            if (imageCapture.getData() == null) {

//                Picasso.with(getContext()).load(tempPhotoFile).into(imgProfile);


            } else {
                setImageFromGallery(imageCapture.getData());
            }
            cropImage();
            setMenuColor();
        }
    }

    private void cropImage() {
        CropImageFragment cropImageFragment = new CropImageFragment();
        cropImageFragment.setTargetFragment(EditProfileFragment.this, 100);
        Bundle bundle = new Bundle();
        bundle.putString("image_path", tempPhotoFile.getAbsolutePath());
        cropImageFragment.setArguments(bundle);
        getFragmentController().replaceFragment(cropImageFragment, true);
//        CropImage.activity(Uri.fromFile(photoFile)).start(getActivity());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            if (data.hasExtra("imagePath")) {
                String imagePath = data.getStringExtra("imagePath");
                if (imagePath != null) {
                    photoFile = new File(imagePath);
                    isImageLoaded = true;
                } else {
                    if (photoFile!=null && !photoFile.exists()) {
                        photoFile = null;
                    }else if(photoFile!=null)
                    {
                        isImageLoaded = true;
                    }
                }
            }
        }
    }

    public void setImageFromGallery(Intent data) {
        Uri uri = data.getData();
        String path = "";
        if (android.os.Build.VERSION.SDK_INT <= 18) {
            path = Utils.getRealPathFromURI_API11to18(getContext(), uri);
        } else {
            path = Utils.getRealPathFromURI_API19(getContext(), uri);
        }
        tempPhotoFile = new File(path);
//        Picasso.with(getContext()).load(photoFile).into(imgProfile);
    }

    public void uploadWithTransferUtility() {

        TransferUtility transferUtility =
                TransferUtility.builder()
                        .context(getContext().getApplicationContext())
                        .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                        .s3Client(new AmazonS3Client(AWSMobileClient.getInstance().getCredentialsProvider()))
                        .build();
        profilePicUrl = "uploads/profile_pic/" + MainApplication.getInstance().getUserID() + "/profile_pic.jpg";
        TransferObserver uploadObserver =
                transferUtility.upload(profilePicUrl
                        , photoFile);
        progressBar.setVisibility(View.VISIBLE);
        // Attach a listener to the observer to get state update and progress notifications
        uploadObserver.setTransferListener(new TransferListener() {

            @Override
            public void onStateChanged(int id, TransferState state) {
                System.out.println("TESTING : " + state.name());
                if (TransferState.COMPLETED == state) {
                    // Handle a completed upload.
                    saveUserDetails();
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                float percentDonef = ((float) bytesCurrent / (float) bytesTotal) * 100;
                int percentDone = (int) percentDonef;

                Logger.d(TAG, "ID:" + id + " bytesCurrent: " + bytesCurrent
                        + " bytesTotal: " + bytesTotal + " " + percentDone + "%");
            }

            @Override
            public void onError(int id, Exception ex) {
                // Handle errors
                ex.printStackTrace();
                MainApplication.showToast("Some error occured while uploading image, Please try again after some time.");
                progressBar.setVisibility(View.GONE);
            }

        });

        // If you prefer to poll for the data, instead of attaching a
        // listener, check for the state and progress in the observer.
        if (TransferState.COMPLETED == uploadObserver.getState()) {
            // Handle a completed upload.
        }

        Logger.d(TAG, "Bytes Transferrred: " + uploadObserver.getBytesTransferred());
        Logger.d(TAG, "Bytes Total: " + uploadObserver.getBytesTotal());
    }


}


