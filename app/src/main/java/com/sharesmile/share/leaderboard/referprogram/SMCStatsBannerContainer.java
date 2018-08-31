package com.sharesmile.share.leaderboard.referprogram;

import android.app.Activity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sharesmile.share.R;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.core.Logger;
import com.sharesmile.share.core.SharedPrefsManager;
import com.sharesmile.share.core.application.MainApplication;
import com.sharesmile.share.core.timekeeping.ServerTimeKeeper;
import com.sharesmile.share.refer_program.model.ReferProgram;
import com.sharesmile.share.utils.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import Models.LeagueBoard;
import butterknife.BindView;
import butterknife.ButterKnife;

import static com.sharesmile.share.core.application.MainApplication.getContext;

/**
 * Created by ankitmaheshwari on 9/3/17.
 */

public class SMCStatsBannerContainer {
    final String TAG = "SMCStatsBannerContainer";

    @BindView(R.id.tv_banner_total_impact)
    TextView bannerTotalMealsShared;

    @BindView(R.id.ends_in_tv)
    TextView endsIn;

    @BindView(R.id.share_code_layout)
    LinearLayout shareCodeLayout;

    @BindView(R.id.share_code)
    TextView shareCode;


    private View containerView;

    private LeagueBoard board;
    Activity activity;

    public SMCStatsBannerContainer(View containerView, Activity activity) {
        this.activity = activity;
        this.containerView = containerView;
        this.board = board;
        ButterKnife.bind(this, containerView);
        init();
    }

    private void init() {
        bannerTotalMealsShared.setText
                (SharedPrefsManager.getInstance().getLong(Constants.PREF_SMC_LEADERBOARD_CACHED_TOTAL,
                        0) + "");
        shareCode.setText(MainApplication.getInstance().getUserDetails().getMyReferCode());
        shareCodeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.share(activity,
                        String.format(getContext().getString(R.string.smc_share_more_meals),
                                MainApplication.getInstance().getUserDetails().getMyReferCode()));
            }
        });

        endsIn.setText("Ends in : " + printDifference());


    }

    public String printDifference() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        ReferProgram referProgram = ReferProgram.getReferProgramDetails();
        Date startDate = new Date(ServerTimeKeeper.getServerTimeStampInMillis());
        Date endDate = null;
        try {
            endDate = simpleDateFormat.parse(referProgram.getEndDate());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (endDate != null) {
            //milliseconds
            long different = endDate.getTime() - startDate.getTime();

            Logger.d(TAG, "startDate : " + startDate);
            Logger.d(TAG, "endDate : " + endDate);
            Logger.d(TAG, "different : " + different);

            long secondsInMilli = 1000;
            long minutesInMilli = secondsInMilli * 60;
            long hoursInMilli = minutesInMilli * 60;
            long daysInMilli = hoursInMilli * 24;

            long elapsedDays = different / daysInMilli;
            different = different % daysInMilli;

            long elapsedHours = different / hoursInMilli;
            different = different % hoursInMilli;

            long elapsedMinutes = different / minutesInMilli;
            different = different % minutesInMilli;

            long elapsedSeconds = different / secondsInMilli;

            if (elapsedDays >= 1) {
                return elapsedDays + " days " + elapsedHours + " hours";
            } else {
                return elapsedHours + " hours " + elapsedMinutes + " minutes";
            }
        } else {
            return "";
        }
    }

}
