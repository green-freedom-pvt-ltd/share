package com.sharesmile.share.rfac.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.sharesmile.share.MainApplication;
import com.sharesmile.share.R;
import com.sharesmile.share.core.BaseFragment;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.utils.SharedPrefsManager;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Created by shine on 5/15/2016.
 */
public class SettingsFragment extends BaseFragment implements View.OnClickListener {

    @BindView(R.id.share)
    TextView mShare;
    @BindView(R.id.rate)
    TextView mRate;

    @BindView(R.id.tos)
    TextView mTos;

    @BindView(R.id.logout)
    TextView mLogout;
    private FragmentInterface mListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_drawer_settings, null);
        ButterKnife.bind(this, view);
        updateSettingItems();
        mShare.setOnClickListener(this);
        mTos.setOnClickListener(this);
        mRate.setOnClickListener(this);
        mLogout.setOnClickListener(this);
        getFragmentController().updateToolBar(getString(R.string.action_settings), true);
        return view;

    }

    private void updateSettingItems() {
        if (SharedPrefsManager.getInstance().getBoolean(Constants.PREF_IS_LOGIN)) {
            mLogout.setVisibility(View.VISIBLE);
        } else {
            mLogout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (FragmentInterface) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.share:
               // share();
                break;
            case R.id.rate:
                rateApp();
                break;
            case R.id.tos:
                Toast.makeText(getContext(), "open Tos", Toast.LENGTH_SHORT).show();
                break;
            case R.id.logout:
                logout();
                break;
        }
    }

    private void logout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(getString(R.string.logout)).setMessage(getString(R.string.logout_msg));
        builder.setPositiveButton(getString(R.string.logout), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MainApplication.getInstance().getDbWrapper().clearAll();
                SharedPrefsManager.getInstance().clearPrefs();
                mListener.updateNavigationMenu();
                updateSettingItems();
                Toast.makeText(getContext(),"Logout",Toast.LENGTH_SHORT).show();

            }
        });
        builder.show();

    }

    private void rateApp() {
        final String appPackageName = getActivity().getPackageName();
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }

    public interface FragmentInterface {
        public void updateNavigationMenu();
    }
}


