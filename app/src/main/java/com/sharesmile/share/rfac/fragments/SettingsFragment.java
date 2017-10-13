package com.sharesmile.share.rfac.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.sharesmile.share.MainApplication;
import com.sharesmile.share.R;
import com.sharesmile.share.core.BaseFragment;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.core.CurrencyCode;
import com.sharesmile.share.core.DistanceUnit;
import com.sharesmile.share.core.UnitsManager;
import com.sharesmile.share.utils.Logger;
import com.sharesmile.share.utils.SharedPrefsManager;
import com.sharesmile.share.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.sharesmile.share.core.Constants.PREF_DISABLE_ALERTS;
import static com.sharesmile.share.core.Constants.PREF_DISABLE_GPS_UPDATES;


/**
 * Created by shine on 5/15/2016.
 */
public class SettingsFragment extends BaseFragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private static final String TAG = "SettingsFragment";

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

    @BindView(R.id.currency_spinner)
    Spinner currencySpinner;

    @BindView(R.id.measurement_spinner)
    Spinner measurementSpinner;

    @BindView(R.id.gps_speed_updates)
    Switch gpsSpeedUpdates;

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
        gpsSpeedUpdates.setOnCheckedChangeListener(this);
        if (SharedPrefsManager.getInstance().getBoolean(PREF_DISABLE_ALERTS, false)){
            notifToggle.setChecked(false);
        }else {
            notifToggle.setChecked(true);
        }
        if (SharedPrefsManager.getInstance().getBoolean(PREF_DISABLE_GPS_UPDATES, true)){
            gpsSpeedUpdates.setChecked(false);
        }else {
            gpsSpeedUpdates.setChecked(true);
        }
        getFragmentController().updateToolBar(getString(R.string.action_settings), true);
        appVersionText.setText("App Version " + Utils.getAppVersion());
        setCurrencySpinner();
        setMeasurementSpinner();
        return view;

    }

    private void setCurrencySpinner(){
        final List<CurrencySpinnerItem> list = new ArrayList<>();
        int initialPos = 0;
        int count = 0;
        for (CurrencyCode currencyCode : CurrencyCode.values()){
            if (UnitsManager.getCurrencyCode().equals(currencyCode)){
                initialPos = count;
            }
            list.add(new CurrencySpinnerItem(currencyCode.getSymbol(), currencyCode.toString()));
            count++;
        }
        currencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Selected
                Logger.d(TAG, "onItemSelected: " + list.get(position).getCode());
                UnitsManager.setCurrencyCode(CurrencyCode.fromString(list.get(position).getCode()));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        CurrencyAdapter spinnerAdapter = new CurrencyAdapter(this.getContext(),
                R.layout.currency_spinner_layout, R.id.tv_currency_spinner, list);
        currencySpinner.setAdapter(spinnerAdapter);
        currencySpinner.setSelection(initialPos);

    }

    private void setMeasurementSpinner(){
        List<String> list = new ArrayList<>();
        list.add("km");
        list.add("miles");

        measurementSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Logger.d(TAG, "position = " + position);
                if (position == 0){
                    // km
                    UnitsManager.setDistanceUnit(DistanceUnit.KILOMETER);
                }else {
                    // miles
                    UnitsManager.setDistanceUnit(DistanceUnit.MILES);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this.getContext(),
                R.layout.currency_spinner_layout, R.id.tv_currency_spinner, list);
        measurementSpinner.setAdapter(spinnerAdapter);
        if (DistanceUnit.KILOMETER.equals(UnitsManager.getDistanceUnit())){
            measurementSpinner.setSelection(0);
        }else {
            measurementSpinner.setSelection(1);
        }
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
                Logger.i("SettingsFragment", "Clearing all preferences and DB");
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
        switch (buttonView.getId()){
            case R.id.gps_speed_updates:
                if (isChecked){
                    // Enabled GPS speed updates
                    SharedPrefsManager.getInstance().setBoolean(PREF_DISABLE_GPS_UPDATES, false);
                }else {
                    // Disable GPS speed updates
                    SharedPrefsManager.getInstance().setBoolean(PREF_DISABLE_GPS_UPDATES, true);
                }
                break;
            case R.id.notif_toggle:
                if (isChecked){
                    // Enabled Alerts and Reminders
                    SharedPrefsManager.getInstance().setBoolean(PREF_DISABLE_ALERTS, false);
                }else {
                    // Disable Alerts and Reminders
                    SharedPrefsManager.getInstance().setBoolean(PREF_DISABLE_ALERTS, true);
                }
                break;
        }
    }

    public interface FragmentInterface {
        public void updateNavigationMenu();
    }

    class CurrencyAdapter extends ArrayAdapter<CurrencySpinnerItem>{

        public CurrencyAdapter(@NonNull Context context, @LayoutRes int resource, @IdRes int textViewResourceId, @NonNull List<CurrencySpinnerItem> objects) {
            super(context, resource, textViewResourceId, objects);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            return super.getView(position, convertView, parent);
        }

        @Override
        public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null){
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.currency_spinner_list_item, parent, false);
            }
            CurrencySpinnerItem rowItem = getItem(position);
            TextView symbol = (TextView) convertView.findViewById(R.id.tv_currency_spinner_item_symbol);
            TextView code = (TextView) convertView.findViewById(R.id.tv_currency_spinner_item_code);

            symbol.setText(rowItem.label);
            code.setText(rowItem.code);

            return convertView;
        }
    }

    class CurrencySpinnerItem {
        private String label;
        private String code;

        public CurrencySpinnerItem(String label, String code) {
            this.label = label;
            this.code = code;
        }

        public String getLabel() {
            return label;
        }

        public String getCode() {
            return code;
        }

        @Override
        public String toString() {
            return label + " " + code;
        }
    }
}


