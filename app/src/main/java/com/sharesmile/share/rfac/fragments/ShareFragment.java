package com.sharesmile.share.rfac.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;

import com.sharesmile.share.R;
import com.sharesmile.share.rfac.activities.ThankYouActivity;

/**
 * Created by apurvgandhwani on 4/5/2016.
 */
public class ShareFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_share, null);
        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        Button btn_share = (Button) v.findViewById(R.id.btn_share_screen);
        Button btn_share_skip = (Button) v.findViewById(R.id.btn_share_screen_skip_share);
        btn_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getActivity(), ThankYouActivity.class);
                startActivity(intent);
            }
        });

        btn_share_skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getActivity(), ThankYouActivity.class);
                startActivity(intent);
            }
        });

        return v;
    }
}



