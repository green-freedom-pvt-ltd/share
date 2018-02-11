package com.sharesmile.share.home.settings;

import android.app.Activity;
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

import com.sharesmile.share.core.application.MainApplication;
import com.sharesmile.share.R;
import com.sharesmile.share.analytics.events.AnalyticsEvent;
import com.sharesmile.share.analytics.events.Event;
import com.sharesmile.share.core.base.BaseFragment;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.core.Logger;
import com.sharesmile.share.core.SharedPrefsManager;
import com.sharesmile.share.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;

import static com.sharesmile.share.core.Constants.PREF_DISABLE_ALERTS;
import static com.sharesmile.share.core.Constants.PREF_DISABLE_GPS_UPDATES;
import static com.sharesmile.share.core.Constants.PREF_DISABLE_VOICE_UPDATES;


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

    @BindView(R.id.notif_voice_updates)
    Switch notifVoiceUpdates;

    @BindView(R.id.currency_spinner)
    Spinner currencySpinner;

    @BindView(R.id.measurement_spinner)
    Spinner measurementSpinner;

    @BindView(R.id.gps_speed_updates)
    Switch gpsSpeedUpdates;

    MaterialShowcaseView materialShowcaseView;

    private static final String SHOW_REMINDER_OVERLAY = "show_reminder_overlay";

    private FragmentInterface mListener;
    private boolean showReminderOverlay;

    public static SettingsFragment newInstance(boolean showReminderOverlay) {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        args.putBoolean(SHOW_REMINDER_OVERLAY, showReminderOverlay);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_drawer_settings, null);
        ButterKnife.bind(this, view);
        updateSettingItems();
        showReminderOverlay = getArguments().getBoolean(SHOW_REMINDER_OVERLAY);
        mShare.setOnClickListener(this);
        mTos.setOnClickListener(this);
        mRate.setOnClickListener(this);
        mLogout.setOnClickListener(this);
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
        if (SharedPrefsManager.getInstance().getBoolean(PREF_DISABLE_VOICE_UPDATES, false)){
            notifVoiceUpdates.setChecked(false);
        }else {
            notifVoiceUpdates.setChecked(true);
        }
        getFragmentController().updateToolBar(getString(R.string.action_settings), true);
        appVersionText.setText("App Version " + Utils.getAppVersion());
        setCurrencySpinner();
        setMeasurementSpinner();

        if (showReminderOverlay){
            SharedPrefsManager.getInstance().setBoolean(PREF_DISABLE_ALERTS, false);
            notifToggle.setChecked(false);
            materialShowcaseView = new MaterialShowcaseView.Builder((Activity) getFragmentController())
                    .setTarget(notifToggle)
                    .setDismissText(getString(R.string.settings_got_it))
                    .setContentText(getString(R.string.settings_you_can_enable_reminders))
                    .setDelay(200) // optional but starting animations immediately in onCreate can make them choppy
                    .setDismissOnTargetTouch(true)
                    .setTargetTouchable(true)
                    .setDismissOnTouch(true)
                    .show();
        }

        notifToggle.setOnCheckedChangeListener(this);
        gpsSpeedUpdates.setOnCheckedChangeListener(this);
        notifVoiceUpdates.setOnCheckedChangeListener(this);

        return view;

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (materialShowcaseView != null && materialShowcaseView.isActivated()){
            materialShowcaseView.hide();
        }
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
        CurrencyAdapter spinnerAdapter = new CurrencyAdapter(this.getContext(),
                R.layout.currency_spinner_layout, R.id.tv_currency_spinner, list);
        currencySpinner.setAdapter(spinnerAdapter);
        currencySpinner.setSelection(initialPos);
        currencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            int check;
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Selected
                if (++check > 1){
                    String currencyCode = list.get(position).getCode();
                    Logger.d(TAG, "onItemSelected: " + currencyCode);
                    UnitsManager.setCurrencyCode(CurrencyCode.fromString(currencyCode));
                    AnalyticsEvent.create(Event.ON_SELECT_CURRENCY)
                            .put("currency_code", currencyCode)
                            .buildAndDispatch();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private void setMeasurementSpinner(){
        List<String> list = new ArrayList<>();
        list.add("km");
        list.add("miles");

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this.getContext(),
                R.layout.currency_spinner_layout, R.id.tv_currency_spinner, list);
        measurementSpinner.setAdapter(spinnerAdapter);
        if (DistanceUnit.KILOMETER.equals(UnitsManager.getDistanceUnit())){
            measurementSpinner.setSelection(0);
        }else {
            measurementSpinner.setSelection(1);
        }

        measurementSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            int check;
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (++check > 1){
                    Logger.d(TAG, "position = " + position);
                    if (position == 0){
                        // km
                        UnitsManager.setDistanceUnit(DistanceUnit.KILOMETER);
                        AnalyticsEvent.create(Event.ON_SELECT_DISTANCE_UNIT)
                                .put("distance_unit", DistanceUnit.KILOMETER.toString())
                                .buildAndDispatch();
                    }else {
                        // miles
                        UnitsManager.setDistanceUnit(DistanceUnit.MILES);
                        AnalyticsEvent.create(Event.ON_SELECT_DISTANCE_UNIT)
                                .put("distance_unit", DistanceUnit.MILES.toString())
                                .buildAndDispatch();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
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
                AnalyticsEvent.create(Event.ON_CLICK_RATE_US_IN_SETTINGS)
                        .buildAndDispatch();
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
                AnalyticsEvent.create(Event.ON_LOGOUT_APP)
                        .put("user_id", MainApplication.getInstance().getUserID())
                        .buildAndDispatch();
                performLogout();
            }
        });
        builder.setNegativeButton(getString(R.string.settings_logout_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();

    }

    private void performLogout(){
        Logger.i("SettingsFragment", "Clearing all preferences and DB");
        MainApplication.getInstance().getDbWrapper().clearAll();
        SharedPrefsManager.getInstance().clearPrefs();
        mListener.updateNavigationMenu();
        updateSettingItems();
        Toast.makeText(getContext(),"Logout",Toast.LENGTH_SHORT).show();

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
                AnalyticsEvent.create(Event.ON_TOGGLE_REMINDER)
                        .put("is_enabled", isChecked)
                        .buildAndDispatch();
                if (isChecked){
                    // Enabled Alerts and Reminders
                    SharedPrefsManager.getInstance().setBoolean(PREF_DISABLE_ALERTS, false);
                }else {
                    // Disable Alerts and Reminders
                    SharedPrefsManager.getInstance().setBoolean(PREF_DISABLE_ALERTS, true);
                }
                break;
            case R.id.notif_voice_updates:
                AnalyticsEvent.create(Event.ON_TOGGLE_VOICE_UPDATE)
                        .put("is_enabled", isChecked)
                        .buildAndDispatch();
                if (isChecked){
                    // Enabled Alerts and Reminders
                    SharedPrefsManager.getInstance().setBoolean(PREF_DISABLE_VOICE_UPDATES, false);
                }else {
                    // Disable Alerts and Reminders
                    SharedPrefsManager.getInstance().setBoolean(PREF_DISABLE_VOICE_UPDATES, true);
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
            TextView symbol = convertView.findViewById(R.id.tv_currency_spinner_item_symbol);
            TextView code = convertView.findViewById(R.id.tv_currency_spinner_item_code);

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
            return label + " " + code ;
        }
    }
}


