package com.sharesmile.share.onboarding.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
        commonActions = ((OnBoardingActivity)getActivity());
        commonActions.setExplainText("","");
        commonActions.setBackAndContinue(TAG, getResources().getString(R.string.continue_without_code_txt));
    }

    @OnFocusChange(R.id.referral_code_et)
    public void onFocusChange(View view, boolean hasFocus) {
        if (hasFocus) {
            referralCodeTil.setHint(getResources().getString(R.string.enter_referral_code));
            referralCode.setHint("");
        } else {
            referralCodeTil.setHint("");
            referralCode.setHint(getResources().getString(R.string.have_a_referral_code));
        }
    }

    @OnClick(R.id.submit_referral_tv)
    public void onClickSubmit() {
        EventBus.getDefault().post(new UpdateEvent.OnCodeVerify());
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
            JSONObject jsonObject = new JSONObject(onCodeVerified.jsonObject.toString());
            if (jsonObject.getInt("code") == Constants.SUCCESS_POST) {
                referralCode.setFocusable(false);
                JSONObject result = jsonObject.getJSONObject("result");
//                MainApplication.showToast("You were refered by : "+result.getString("referrer_name"));
                UserDetails userDetails = MainApplication.getInstance().getUserDetails();
                userDetails.setReferalId(result.getInt("referrer_user_id"));
                userDetails.setReferalName(result.getString("referrer_name"));
                userDetails.setReferCodeUsed(referralCode.getText().toString());
                MainApplication.getInstance().setUserDetails(userDetails);
//                commonActions.setBackAndContinue(TAG, getContext().getResources().getString(R.string.continue_txt));
                SharedPrefsManager.getInstance().setBoolean(Constants.PREF_SHOW_SMC_MATCH_DIALOG, true);
                getFragmentController().replaceFragment(new FragmentSomethingIsCooking(), true);
//                showSomethingCookingDialog();
            } else {
                MainApplication.showToast(jsonObject.getString("result"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }


}
