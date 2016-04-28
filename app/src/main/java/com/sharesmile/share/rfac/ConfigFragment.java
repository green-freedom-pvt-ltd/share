package com.sharesmile.share.rfac;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sharesmile.share.R;
import com.sharesmile.share.core.BaseFragment;
import com.sharesmile.share.core.Config;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ankitm on 26/03/16.
 */
public class ConfigFragment extends BaseFragment {

	private static final String TAG = "ConfigFragment";
	LinearLayout layout;
	Button saveButton;
	Map<Integer, View> configValuesMap;

	public static ConfigFragment newInstance() {
		ConfigFragment fragment = new ConfigFragment();
		Bundle args = new Bundle();
		fragment.setArguments(args);
		return fragment;
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View baseView = inflater.inflate(R.layout.config_params, container, false);
		layout = (LinearLayout) baseView;
		popuateChildViews();
		return baseView;
	}

	private void popuateChildViews(){
		configValuesMap = new HashMap<>();
		for (int i = 0; i < layout.getChildCount(); i++) {
			View v = layout.getChildAt(i);
			int viewId = v.getId();

			switch (viewId){
				case R.id.edit_source_acceptable_accuracy:
					((TextView) v.findViewById(R.id.tv_param_name)).setText("SOURCE_ACCEPTABLE_ACCURACY");
					EditText sac = (EditText) v.findViewById(R.id.et_param_value);
					sac.setText(Config.SOURCE_ACCEPTABLE_ACCURACY + "");
					configValuesMap.put(viewId, sac);
					break;
				case R.id.edit_threshold_accuracy:
					((TextView) v.findViewById(R.id.tv_param_name)).setText("THRESHOLD_ACCURACY");
					EditText ta = (EditText) v.findViewById(R.id.et_param_value);
					ta.setText(Config.THRESHOLD_ACCURACY + "");
					configValuesMap.put(viewId, ta);
					break;
				case R.id.edit_threshold_factor:
					((TextView) v.findViewById(R.id.tv_param_name)).setText("THRESHOLD_FACTOR");
					EditText tf = (EditText) v.findViewById(R.id.et_param_value);
					tf.setText(Config.THRESHOLD_FACTOR + "");
					configValuesMap.put(viewId, tf);
					break;
				case R.id.edit_smallest_displacement:
					((TextView) v.findViewById(R.id.tv_param_name)).setText("SMALLEST_DISPLACEMENT");
					EditText sd = (EditText) v.findViewById(R.id.et_param_value);
					sd.setText(Config.SMALLEST_DISPLACEMENT + "");
					configValuesMap.put(viewId, sd);
					break;
				case R.id.edit_too_slow_check:
					((TextView) v.findViewById(R.id.tv_param_name)).setText("TOO_SLOW_CHECK");
					CheckBox tsc = (CheckBox) v.findViewById(R.id.cb_param_value);
					tsc.setChecked(Config.TOO_SLOW_CHECK);
					configValuesMap.put(viewId, tsc);
					break;
				case R.id.edit_lazy_ass_check:
					((TextView) v.findViewById(R.id.tv_param_name)).setText("LAZY_ASS_CHECK");
					CheckBox lac = (CheckBox) v.findViewById(R.id.cb_param_value);
					lac.setChecked(Config.LAZY_ASS_CHECK);
					configValuesMap.put(viewId, lac);
					break;
				case R.id.edit_usain_bolt_check:
					((TextView) v.findViewById(R.id.tv_param_name)).setText("USAIN_BOLT_CHECK");
					CheckBox ubc = (CheckBox) v.findViewById(R.id.cb_param_value);
					ubc.setChecked(Config.USAIN_BOLT_CHECK);
					configValuesMap.put(viewId, ubc);
					break;

				case R.id.bt_save:
					saveButton = (Button) v;
					saveButton.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							savePreferences();
						}
					});
					break;
			}

		}
	}

	private void savePreferences(){
		if (configValuesMap != null && !configValuesMap.isEmpty()){
			for (int viewId : configValuesMap.keySet()){
				switch (viewId){

					case R.id.edit_source_acceptable_accuracy:
						EditText sac = (EditText) configValuesMap.get(viewId);
						Config.SOURCE_ACCEPTABLE_ACCURACY = Float.parseFloat(sac.getText().toString());
						break;
					case R.id.edit_threshold_accuracy:
						EditText eta = (EditText) configValuesMap.get(viewId);
						Config.THRESHOLD_ACCURACY = Float.parseFloat(eta.getText().toString());
						break;
					case R.id.edit_threshold_factor:
						EditText etf = (EditText) configValuesMap.get(viewId);
						Config.THRESHOLD_FACTOR = Float.parseFloat(etf.getText().toString());
						break;
					case R.id.edit_smallest_displacement:
						EditText esd = (EditText) configValuesMap.get(viewId);
						Config.SMALLEST_DISPLACEMENT = Float.parseFloat(esd.getText().toString());
						break;
					case R.id.edit_too_slow_check:
						CheckBox etsc = (CheckBox) configValuesMap.get(viewId);
						Config.TOO_SLOW_CHECK = etsc.isChecked();
						break;
					case R.id.edit_lazy_ass_check:
						CheckBox elac = (CheckBox) configValuesMap.get(viewId);
						Config.LAZY_ASS_CHECK = elac.isChecked();
						break;
					case R.id.edit_usain_bolt_check:
						CheckBox eubc = (CheckBox) configValuesMap.get(viewId);
						Config.USAIN_BOLT_CHECK = eubc.isChecked();
						break;

				}
			}
			Toast.makeText(getContext(), "Saved!", Toast.LENGTH_LONG).show();
		}
	}
}
