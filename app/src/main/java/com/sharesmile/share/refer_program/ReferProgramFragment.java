package com.sharesmile.share.refer_program;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sharesmile.share.R;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.core.SharedPrefsManager;
import com.sharesmile.share.core.application.MainApplication;
import com.sharesmile.share.core.base.BaseFragment;
import com.sharesmile.share.leaderboard.referprogram.ReferLeaderBoardFragment;
import com.sharesmile.share.login.UserDetails;
import com.sharesmile.share.refer_program.model.ReferProgram;
import com.sharesmile.share.utils.Utils;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ReferProgramFragment extends BaseFragment{

    private static final String TAG = "ReferProgramFragment";

    @BindView(R.id.share_code)
    TextView shareCode;
    @BindView(R.id.share_code_layout)
    LinearLayout shareCodeLayout;
    @BindView(R.id.smc_leaderboard)
    ImageView smcLeaderboard;
    @BindView(R.id.back)
    ImageView back;
    @BindView(R.id.smc_viewpager)
    ViewPager smcViewpager;
    @BindView(R.id.powered_by_tv)
    TextView poweredByTv;

    SMCPageAdapter smcPageAdapter;

    @BindView(R.id.dot_indicator)
    LinearLayout dotIndicator;


    public static ReferProgramFragment getInstance(int position) {
        ReferProgramFragment referProgramFragment = new ReferProgramFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("position", position);
        referProgramFragment.setArguments(bundle);
        return referProgramFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_share_n_feed, null);
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
        getFragmentController().hideToolbar();
        init();
        Bundle bundle = getArguments();
        if (bundle != null && bundle.containsKey("position")) {
            smcViewpager.setCurrentItem(1);
        }
        Utils.setStausBarColor(getActivity().getWindow(), R.color.clr_328f6c);
    }

    private void init() {
        UserDetails userDetails = MainApplication.getInstance().getUserDetails();
        shareCode.setText(userDetails.getMyReferCode());
        if (SharedPrefsManager.getInstance().getBoolean(Constants.PREF_SHOW_SMC_LEADERBOARD_NOTI, true)) {
            smcLeaderboard.setImageResource(R.drawable.smc_leaderboard_icon2);
        } else {
            smcLeaderboard.setImageResource(R.drawable.smc_leaderboard_icon);
        }
        smcPageAdapter = new SMCPageAdapter(getChildFragmentManager());
        smcViewpager.setAdapter(smcPageAdapter);
        smcViewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                setIndicator(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        setIndicator(0);
        poweredByTv.setText(getResources().getString(R.string.powered_by) + " " + ReferProgram.getReferProgramDetails().getSponsoredBy());
    }

    private void setIndicator(int position) {
        dotIndicator.removeAllViews();
        for (int i = 0; i <= 1; i++) {
            ImageView imageView = new ImageView(getContext());
            imageView.setPadding(8, 0, 8, 0);
            if (i == position) {
                imageView.setImageResource(R.drawable.carousel_indicator_circle_selected_white);
            } else {
                imageView.setImageResource(R.drawable.carousel_indicator_circle_not_selected_white);
            }
            dotIndicator.addView(imageView);
        }
    }


    @OnClick({R.id.back, R.id.smc_leaderboard})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back:
                getFragmentController().goBack();
                break;
            case R.id.smc_leaderboard:
                SharedPrefsManager.getInstance().setBoolean(Constants.PREF_SHOW_SMC_LEADERBOARD_NOTI, false);
                getFragmentController().replaceFragment(ReferLeaderBoardFragment.getInstance(), true);
                break;
        }
    }

    @OnClick({/*R.id.share_facebook, R.id.share_whatsapp, R.id.share_twitter, R.id.share_gmail,*/ R.id.share_code_layout})
    public void onShare(View view)
    {
        switch (view.getId())
        {
            /*case R.id.share_facebook :
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
                break;*/
            case R.id.share_code_layout:
                /*AssetManager assetManager = getContext().getAssets();
                InputStream istr;
                Bitmap bitmap = null;
                try {
                    istr = assetManager.open("images/share_image_2.jpg");
                    bitmap = BitmapFactory.decodeStream(istr);
                } catch (IOException e) {
                    // handle exception
                }*/

                Utils.share(getActivity(),
                        String.format(getString(R.string.smc_share_more_meals),
                                MainApplication.getInstance().getUserDetails().getMyReferCode()));

                break;
        }
    }

}
