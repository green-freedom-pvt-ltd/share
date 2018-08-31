package com.sharesmile.share.leaderboard.referprogram;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sharesmile.share.R;
import com.sharesmile.share.core.Logger;
import com.sharesmile.share.core.application.MainApplication;
import com.sharesmile.share.leaderboard.common.adapter.BannerPagerAdapter;
import com.sharesmile.share.leaderboard.common.container.ImageBannerContainer;
import com.sharesmile.share.refer_program.model.ReferProgram;
import com.sharesmile.share.utils.Utils;

import static com.sharesmile.share.core.application.MainApplication.getContext;

public class SMCLeagueBoardBannerPagerAdapter extends BannerPagerAdapter {

    private static final String TAG = "SMCLeagueBoardBannerPagerAdapter";


    private ImageBannerContainer imageBannerContainer;
    private SMCStatsBannerContainer statsBannerContainer;
    ReferProgram referProgram;
    Activity activity;

    public SMCLeagueBoardBannerPagerAdapter(Activity activity) {
        this.activity = activity;
        referProgram = ReferProgram.getReferProgramDetails();
    }

    @Override
    public int getNumPages() {
        return 2;
    }

    @Override
    protected View getItemView(final int position, ViewGroup container) {
        Logger.d(TAG, "getItemView: " + position);
        switch (position) {
            case 0:
                View imageBanner = LayoutInflater.from(container.getContext())
                        .inflate(R.layout.smc_banner_image_container, container, false);
                TextView poweredBy = imageBanner.findViewById(R.id.powered_by_tv);
                poweredBy.setText("powered by " + ReferProgram.getReferProgramDetails().getSponsoredBy());
                LinearLayout shareCodeLayout = imageBanner.findViewById(R.id.share_code_layout);
                shareCodeLayout.setVisibility(View.VISIBLE);
                TextView shareCode = imageBanner.findViewById(R.id.share_code);
                shareCode.setText(MainApplication.getInstance().getUserDetails().getMyReferCode());
                shareCodeLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Utils.share(activity,
                                String.format(getContext().getString(R.string.smc_share_more_meals), MainApplication.getInstance().getUserDetails().getMyReferCode()));
                    }
                });
                return imageBanner;
            case 1:
                View statsBanner = LayoutInflater.from(container.getContext())
                        .inflate(R.layout.smc_banner_stats_container, container, false);

                statsBannerContainer = new SMCStatsBannerContainer(statsBanner, activity);
                /*statsBanner.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AnalyticsEvent.create(Event.ON_CLICK_LEAGUE_BOARD_BANNER)
                                .put("type", "stats")
                                .buildAndDispatch();
                    }
                });*/
                return statsBanner;
            default:
                throw new IndexOutOfBoundsException("Invalid index " + position + " for LeagueBoardBannerPagerAdapter");
        }
    }

}
