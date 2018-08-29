package com.sharesmile.share.onboarding.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.sharesmile.share.R;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.core.SharedPrefsManager;
import com.sharesmile.share.core.application.MainApplication;
import com.sharesmile.share.core.base.BaseFragment;
import com.sharesmile.share.core.config.Urls;
import com.sharesmile.share.core.event.UpdateEvent;
import com.sharesmile.share.login.UserDetails;
import com.sharesmile.share.network.NetworkDataProvider;
import com.sharesmile.share.network.NetworkException;
import com.sharesmile.share.onboarding.CommonActions;
import com.sharesmile.share.onboarding.OnBoardingActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnFocusChange;

public class FragmentWelcome extends BaseFragment {

    public static final String TAG = "FragmentWelcome";
    CommonActions commonActions;
    @BindView(R.id.referral_code_til)
    TextInputLayout referralCodeTil;
    @BindView(R.id.referral_code_et)
    EditText referralCode;
    @BindView(R.id.submit_referral_tv)
    TextView submitReferral;
    @BindView(R.id.referral_progress_bar)
    ProgressBar progressBar;
    @BindView(R.id.referral_code_layout)
    RelativeLayout referralCodeLayout;
    @BindView(R.id.invite_layout)
    LinearLayout inviteLayout;
    @BindView(R.id.invited_by_name)
    TextView invitedByName;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_onboarding_welcome, null);
        ButterKnife.bind(this, v);
        EventBus.getDefault().register(this);
        return v;
    }

    @Override
    public void onDestroyView() {
        EventBus.getDefault().unregister(this);
        super.onDestroyView();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        commonActions = ((OnBoardingActivity) getActivity());
        commonActions.setExplainText("", "");
        commonActions.setBackAndContinue(TAG, getResources().getString(R.string.continue_without_code_txt));
        init();
    }

    private void init() {
        UserDetails userDetails = MainApplication.getInstance().getUserDetails();
        boolean b = userDetails.getReferCodeUsed().length() > 0;
        if (b) {
            referralCodeLayout.setVisibility(View.GONE);
            inviteLayout.setVisibility(View.VISIBLE);
            invitedByName.setText(userDetails.getReferalName() + "");
            commonActions.setBackAndContinue(TAG, getResources().getString(R.string.continue_txt));
            SharedPrefsManager.getInstance().setBoolean(Constants.PREF_SHOW_SMC_MATCH_DIALOG, true);
        } else {
            inviteLayout.setVisibility(View.GONE);
            referralCodeLayout.setVisibility(View.VISIBLE);
            submitReferral.setVisibility(View.GONE);
            referralCode.setText("");
            referralCode.setHint(getResources().getString(R.string.have_a_referral_code));
            referralCodeTil.setHint("");
        }
    }

    @OnFocusChange(R.id.referral_code_et)
    public void onFocusChange(View view, boolean hasFocus) {
        if (hasFocus) {
            referralCodeTil.setHint(getResources().getString(R.string.enter_referral_code));
            referralCode.setHint("");
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(referralCode, InputMethodManager.SHOW_IMPLICIT);
            submitReferral.setVisibility(View.VISIBLE);
        } else {
            referralCodeTil.setHint("");
            referralCode.setHint(getResources().getString(R.string.have_a_referral_code));
        }
    }

    @OnClick(R.id.submit_referral_tv)
    public void onClickSubmit() {
        /*if(MainApplication.getInstance().getUserDetails().getReferCodeUsed().length()>0)
        {
         getFragmentController().replaceFragment(new FragmentGender(),true);
        }else {*/
        progressBar.setVisibility(View.VISIBLE);
        submitReferral.setVisibility(View.GONE);
        EventBus.getDefault().post(new UpdateEvent.OnCodeVerify());
        /*}*/
    }

    private void verifyCode() {
        String referCodeString = referralCode.getText().toString();
        if (referCodeString.length() != 10) {
            MainApplication.showToast("Invalid code, please enter valid code.");
        } else {
            JSONObject requestObject = new JSONObject();
            try {
                requestObject.put("refer_code_used", referCodeString);
                requestObject.put("referral_program_id", 2);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                JsonObject jsonObject = NetworkDataProvider.doPostCall(Urls.getVerifyReferralCodeUrl(), requestObject, JsonObject.class/*new NetworkAsyncCallback<String>() {
                        @Override
                        public void onNetworkFailure(NetworkException ne) {
                            Logger.e(TAG, "Couldn't refer code: " + ne);
                            ne.printStackTrace();
                        }

                        @Override
                        public void onNetworkSuccess(String list) {
                            JsonObject jsonObject = new JsonObject();
                            Logger.d(TAG, "Successfully verified code");
                            EventBus.getDefault().post(new UpdateEvent.OnCodeVerified(jsonObject)));
                        }
                    }*/);
                EventBus.getDefault().post(new UpdateEvent.OnCodeVerified(jsonObject));
            } catch (NetworkException e) {
                e.printStackTrace();
            }

        }

    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onEvent(UpdateEvent.OnCodeVerify onCodeVerify) {
        verifyCode();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(UpdateEvent.OnCodeVerified onCodeVerified) {
        try {
            progressBar.setVisibility(View.GONE);
            JSONObject result = new JSONObject(onCodeVerified.jsonObject.toString());
            UserDetails userDetails = MainApplication.getInstance().getUserDetails();
            userDetails.setReferalId(result.getInt("referrer_user_id"));
            userDetails.setReferalName(result.getString("referrer_name"));
            userDetails.setReferCodeUsed(referralCode.getText().toString());
            MainApplication.getInstance().setUserDetails(userDetails);
            SharedPrefsManager.getInstance().setBoolean(Constants.PREF_SHOW_SMC_MATCH_DIALOG, true);
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
            //Find the currently focused view, so we can grab the correct window token from it.
            View view = getActivity().getCurrentFocus();
            //If no view currently has focus, create a new one, just so we can grab a window token from it
            if (view == null) {
                view = new View(getContext());
            }
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            getFragmentController().replaceFragment(new FragmentSomethingIsCooking(), true);

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }


}
