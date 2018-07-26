package com.sharesmile.share.onboarding.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.sharesmile.share.R;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.core.Logger;
import com.sharesmile.share.core.application.MainApplication;
import com.sharesmile.share.core.base.BaseFragment;
import com.sharesmile.share.core.cause.model.CauseList;
import com.sharesmile.share.core.config.Urls;
import com.sharesmile.share.core.event.UpdateEvent;
import com.sharesmile.share.network.NetworkAsyncCallback;
import com.sharesmile.share.network.NetworkDataProvider;
import com.sharesmile.share.network.NetworkException;
import com.sharesmile.share.onboarding.CommonActions;
import com.sharesmile.share.onboarding.OnBoardingActivity;
import com.sharesmile.share.refer_program.model.ReferCode;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FragmentEnterReferCode extends BaseFragment {
    public static final String TAG = "FragmentEnterReferCode";
    CommonActions commonActions;
    @BindView(R.id.refer_code)
    EditText referCode;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_onboarding_enter_referal_code, null);
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
        commonActions.setExplainText(getContext().getResources().getString(R.string.enter_your_code), getContext().getResources().getString(R.string.refer_code_explain_txt));
        commonActions.setBackAndContinue(TAG, getResources().getString(R.string.continue_txt));
        commonActions.setContinueTextColor(R.color.white_10);
        init();
    }

    private void init() {
        referCode.requestFocus();
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(referCode, 0);
        referCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 10) {
                    commonActions.setContinueTextColor(R.color.white);
                    commonActions.setBackAndContinue(TAG, getContext().getResources().getString(R.string.verify_txt));
                } else {
                    commonActions.setContinueTextColor(R.color.white_10);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void verifyCode() {
        String referCodeString = referCode.getText().toString();
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
                JsonObject jsonObject = NetworkDataProvider.doPostCall(Urls.getVerifyReferralCodeUrl(), requestObject, JsonObject.class/*, new NetworkAsyncCallback<ReferCode>() {
                        @Override
                        public void onNetworkFailure(NetworkException ne) {
                            Logger.e(TAG, "Couldn't refer code: " + ne);
                            ne.printStackTrace();
                        }

                        @Override
                        public void onNetworkSuccess(ReferCode list) {
                            Logger.d(TAG, "Successfully verified code");
                            EventBus.getDefault().post(new UpdateEvent.OnCodeVerify(true));
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
                referCode.setFocusable(false);
                JSONObject result = jsonObject.getJSONObject("result");
                MainApplication.showToast("Verified");
                commonActions.setBackAndContinue(TAG, getContext().getResources().getString(R.string.continue_txt));
            }else
            {
                MainApplication.showToast(jsonObject.getString("result"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }


}
