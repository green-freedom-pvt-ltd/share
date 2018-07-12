package com.sharesmile.share.share_n_feed;

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
import com.sharesmile.share.utils.ShareUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ShareNFeedFragment extends BaseFragment{

    private static final String TAG = "ShareNFeedFragment";

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

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @OnClick({R.id.share_facebook,R.id.share_whatsapp,R.id.share_twitter,R.id.share_gmail})
    public void onShare(View view)
    {
        switch (view.getId())
        {
            case R.id.share_facebook :
                ShareUtils.shareOnFb(getActivity(),"Testing", Uri.parse("onelink.to/impact"));
                break;
            case R.id.share_whatsapp :
                startActivity(ShareUtils.shareOnWhatsAppIntent("Testing", null));
                break;
            case R.id.share_twitter :
                startActivity(ShareUtils.shareOnTwitter("Testing"));
                break;
            case R.id.share_gmail :
                try {
                    startActivity(ShareUtils.shareOnGmail("Testing"));
                }catch (Exception e)
                {
                    MainApplication.showToast("Some error occurred, Please try after some time.");
                    e.printStackTrace();
                }
                break;
        }
    }
}
