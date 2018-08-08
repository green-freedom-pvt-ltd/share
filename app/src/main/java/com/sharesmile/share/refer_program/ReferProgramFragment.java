package com.sharesmile.share.refer_program;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sharesmile.share.R;
import com.sharesmile.share.core.application.MainApplication;
import com.sharesmile.share.core.base.BaseFragment;
import com.sharesmile.share.leaderboard.referprogram.ReferLeaderBoardFragment;
import com.sharesmile.share.login.UserDetails;
import com.sharesmile.share.utils.ShareUtils;
import com.sharesmile.share.utils.Utils;

import java.io.IOException;
import java.io.InputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ReferProgramFragment extends BaseFragment{

    private static final String TAG = "ReferProgramFragment";

    @BindView(R.id.share_code)
    TextView shareCode;
    @BindView(R.id.share_code_layout)
    LinearLayout shareCodeLayout;
    @BindView(R.id.share_facebook)
    TextView shareFacebook;
    @BindView(R.id.share_whatsapp)
    TextView shareWhatsapp;
    @BindView(R.id.share_twitter)
    TextView shareTwitter;
    @BindView(R.id.share_gmail)
    TextView shareGmail;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_share_n_feed, null);
        ButterKnife.bind(this, v);
//        EventBus.getDefault().register(this);
        return v;
    }

    @Override
    public void onDestroyView() {
//        EventBus.getDefault().unregister(this);
        super.onDestroyView();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupToolbar();
        init();
    }

    private void init() {
        UserDetails userDetails = MainApplication.getInstance().getUserDetails();
        shareCode.setText(userDetails.getMyReferCode());
    }

    private void setupToolbar() {
        setHasOptionsMenu(true);
        setToolbarTitle(getResources().getString(R.string.share_a_meal_challenge));
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_share_n_feed, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.share_n_feed_leadearboard:
                getFragmentController().replaceFragment(new ReferLeaderBoardFragment(),true);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @OnClick({R.id.share_facebook, R.id.share_whatsapp, R.id.share_twitter, R.id.share_gmail, R.id.share_code_layout})
    public void onShare(View view)
    {
        switch (view.getId())
        {
            case R.id.share_facebook :
                ShareUtils.shareOnFb(getActivity(),"Testing", Uri.parse("onelink.to/impact"));
                break;
            case R.id.share_whatsapp :
                if(ShareUtils.appInstalledOrNot(getContext(),"com.whatsapp"))
                startActivity(ShareUtils.shareOnWhatsAppIntent("Testing", null));
                else
                    MainApplication.showToast("Whatsapp is not installed, cannot share");
                break;
            case R.id.share_twitter :
                startActivity(ShareUtils.shareOnTwitter("Testing Use this code : " + shareCode.getText().toString()));
                break;
            case R.id.share_gmail :
                try {
                    startActivity(ShareUtils.shareOnGmail("Testing Use this code : " + shareCode.getText().toString()));
                }catch (Exception e)
                {
                    MainApplication.showToast("Some error occurred, Please try after some time.");
                    e.printStackTrace();
                }
                break;
            case R.id.share_code_layout:
                AssetManager assetManager = getContext().getAssets();
                InputStream istr;
                Bitmap bitmap = null;
                try {
                    istr = assetManager.open("images/share_image_2.jpg");
                    bitmap = BitmapFactory.decodeStream(istr);
                } catch (IOException e) {
                    // handle exception
                }
                Utils.share(getContext(), Utils.getLocalBitmapUri(bitmap, getContext()),
                        getString(R.string.share_msg) + " Use this code : " + shareCode.getText().toString());
                break;
        }
    }
}
