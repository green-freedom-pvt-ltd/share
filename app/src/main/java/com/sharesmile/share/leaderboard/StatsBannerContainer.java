package com.sharesmile.share.leaderboard;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.sharesmile.share.R;
import com.sharesmile.share.home.settings.UnitsManager;
import com.sharesmile.share.core.ShareImageLoader;
import com.sharesmile.share.utils.Utils;

import Models.LeagueBoard;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ankitmaheshwari on 9/3/17.
 */

public class StatsBannerContainer {

    @BindView(R.id.iv_banner_logo)
    ImageView bannerLogo;

    @BindView(R.id.tv_banner_total_impact)
    TextView bannerTotalImpact;

    @BindView(R.id.tv_banner_num_runs)
    TextView bannerNumRuns;

    @BindView(R.id.tv_banner_num_members)
    TextView bannerNumMembers;

    private View containerView;

    private LeagueBoard board;

    public StatsBannerContainer(View containerView, LeagueBoard board){
        this.containerView = containerView;
        this.board = board;
        ButterKnife.bind(this, containerView);
        init();
    }

    private void init(){
        if (!TextUtils.isEmpty(board.getLeagueLogo())) {
            ShareImageLoader.getInstance().loadImage(board.getLeagueLogo(), bannerLogo);
            bannerLogo.setVisibility(View.VISIBLE);
        }
        bannerTotalImpact.setText(UnitsManager.formatRupeeToMyCurrency(board.getTotalImpact()));
        bannerNumRuns.setText(Utils.formatCommaSeparated((long) board.getTotalRuns()));
        bannerNumMembers.setText(String.valueOf(board.getTotalMembers()));
    }

}
