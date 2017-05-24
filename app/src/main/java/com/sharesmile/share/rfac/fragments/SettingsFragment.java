package com.sharesmile.share.rfac.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.sharesmile.share.MainApplication;
import com.sharesmile.share.R;
import com.sharesmile.share.core.BaseFragment;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.utils.Logger;
import com.sharesmile.share.utils.SharedPrefsManager;
import com.sharesmile.share.utils.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.sharesmile.share.core.Constants.PREF_DISABLE_ALERTS;


/**
 * Created by shine on 5/15/2016.
 */
public class SettingsFragment extends BaseFragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    @BindView(R.id.share)
    TextView mShare;
    @BindView(R.id.rate)
    TextView mRate;

    @BindView(R.id.tos)
    TextView mTos;

    @BindView(R.id.logout)
    TextView mLogout;

    @BindView(R.id.tv_app_version)
    TextView appVersionText;

    @BindView(R.id.notif_toggle)
    Switch notifToggle;

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
        notifToggle.setOnCheckedChangeListener(this);
        if (SharedPrefsManager.getInstance().getBoolean(PREF_DISABLE_ALERTS, false)){
            notifToggle.setChecked(false);
        }else {
            notifToggle.setChecked(true);
        }
        getFragmentController().updateToolBar(getString(R.string.action_settings), true);
        String version = "";
        try {
            PackageInfo pInfo = getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0);
            version = pInfo.versionName;
        }catch (Exception e){
            Logger.e("SettingsFragment", e.getMessage());
        }

        appVersionText.setText("App Version " + version);
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
                Utils.redirectToPlayStore(getContext());
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
        builder.setPositiveButton(getString(R.string.logout_button), new DialogInterface.OnClickListener() {
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

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked){
            // Enabled Alerts and Reminders
            SharedPrefsManager.getInstance().setBoolean(PREF_DISABLE_ALERTS, false);
        }else {
            // Disable Alerts and Reminders
            SharedPrefsManager.getInstance().setBoolean(PREF_DISABLE_ALERTS, true);
        }
    }

    public interface FragmentInterface {
        public void updateNavigationMenu();
    }
}


