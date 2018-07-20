package com.sharesmile.share.profile.badges;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sharesmile.share.Badge;
import com.sharesmile.share.BadgeDao;
import com.sharesmile.share.R;
import com.sharesmile.share.Title;
import com.sharesmile.share.TitleDao;
import com.sharesmile.share.analytics.events.AnalyticsEvent;
import com.sharesmile.share.analytics.events.Event;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.core.MainActivity;
import com.sharesmile.share.core.ShareImageLoader;
import com.sharesmile.share.core.application.MainApplication;
import com.sharesmile.share.core.base.BaseFragment;
import com.sharesmile.share.core.config.Urls;
import com.sharesmile.share.profile.badges.model.AchievedBadgesData;
import com.sharesmile.share.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class AchieviedBadgeFragment extends BaseFragment implements View.OnClickListener {

    @BindView(R.id.close_iv)
    ImageView closeIv;
    @BindView(R.id.continue_tv)
    TextView continueTv;
    @BindView(R.id.badge_earned_tv)
    TextView badgeEarnedTv;
    @BindView(R.id.badge_title_header_tv)
    TextView badgeTitleHeaderTv;
    @BindView(R.id.badge_title_tv)
    TextView badgeTitle;
    @BindView(R.id.iv_badge)
    ImageView badgeIv;
    @BindView(R.id.iv_star)
    ImageView starIv;
    @BindView(R.id.badge_layout)
    RelativeLayout badgeLayout;
    @BindView(R.id.badge_amount_raised_tv)
    TextView badgeAmountRaised;
    @BindView(R.id.badge_upgrade_tv)
    TextView badgeUpgrade;
    @BindView(R.id.share_layout)
    LinearLayout shareLayout;
    @BindView(R.id.btn_tell_your_friends)
    Button tellYourFriends;
    long badgeId = 0;
    int from = -1;

    private String TAG = "AchieviedBadgeFragment";
    AchievedBadgesData achievedBadgesData;
    ObjectAnimator animation;
    String shareMessage = "";
    String name = "";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    public static AchieviedBadgeFragment newInstance(AchievedBadgesData achievedBadgesData, String tag) {
        AchieviedBadgeFragment fragment = new AchieviedBadgeFragment();
        Bundle args = new Bundle();
        args.putParcelable(Constants.ACHIEVED_BADGE_DATA, achievedBadgesData);
        args.putString("TAG", tag);
        fragment.setArguments(args);

        return fragment;
    }

    public static AchieviedBadgeFragment newInstance(AchievedBadgesData achievedBadgesData, String tag, int i) {
        AchieviedBadgeFragment fragment = new AchieviedBadgeFragment();
        Bundle args = new Bundle();
        args.putParcelable(Constants.ACHIEVED_BADGE_DATA, achievedBadgesData);
        args.putString("TAG", tag);
        args.putInt("FROM", i);
        fragment.setArguments(args);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_badge, null);
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
        getFragmentController().hideToolbar();
        Bundle bundle = getArguments();
        achievedBadgesData = bundle.getParcelable(Constants.ACHIEVED_BADGE_DATA);
        TAG = bundle.getString("TAG");
        if (bundle.containsKey("FROM"))
            from = bundle.getInt("FROM", -1);
        initUi();
        startPostponedEnterTransition();

    }

    private void initUi() {

        switch (TAG) {
            case Constants.BADGE_TYPE_CHANGEMAKER:
                badgeId = achievedBadgesData.getChangeMakerBadgeAchieved();
                break;
            case Constants.BADGE_TYPE_STREAK:
                badgeId = achievedBadgesData.getStreakBadgeAchieved();
                break;
            case Constants.BADGE_TYPE_CAUSE:
                badgeId = achievedBadgesData.getCauseBadgeAchieved();
                break;
            case Constants.BADGE_TYPE_MARATHON:
                badgeId = achievedBadgesData.getMarathonBadgeAchieved();
                break;
            case Constants.TITLE_TYPE_CAUSE:
                ArrayList<Long> titleIds = achievedBadgesData.getTitleIds();

                badgeId = titleIds.get(0);
                break;
        }
        if(TAG.equalsIgnoreCase(Constants.TITLE_TYPE_CAUSE))
        {
            TitleDao titleDao = MainApplication.getInstance().getDbWrapper().getTitleDao();
            List<Title> titles = titleDao.queryBuilder().where(TitleDao.Properties.TitleId.eq(badgeId)).list();

            if (titles != null && titles.size() > 0) {
                Title title = titles.get(0);
                badgeEarnedTv.setText(title.getWinningMessage());
                badgeTitle.setText(title.getTitle());
                badgeTitleHeaderTv.setVisibility(View.VISIBLE);
                badgeTitleHeaderTv.setText(title.getDescription_1());
                badgeAmountRaised.setText(title.getDescription_2());
                badgeUpgrade.setText(title.getDescription_3());
                shareMessage = title.getShare_message();
                name = title.getTitle();
//                Utils.setStarImage(title.getNoOfStars(), starIv);
                ShareImageLoader.getInstance().loadImage(Urls.getImpactAssetsS3BucketUrl() + title.getImageUrl(), badgeIv,
                        ContextCompat.getDrawable(getContext(), R.drawable.badge_image));
            }
            achievedBadgesData.getTitleIds().remove(0);
        }else {
            BadgeDao badgeDao = MainApplication.getInstance().getDbWrapper().getBadgeDao();
            List<Badge> badges = badgeDao.queryBuilder().where(BadgeDao.Properties.BadgeId.eq(badgeId)).list();

            if (badges != null && badges.size() > 0) {
                Badge badge = badges.get(0);
                badgeEarnedTv.setText(badge.getDescription1());
                badgeTitle.setText(badge.getName());
                badgeTitleHeaderTv.setVisibility(View.GONE);
                badgeAmountRaised.setText(badge.getDescription2());
                badgeUpgrade.setText(badge.getDescription3());
                shareMessage = badge.getShare_badge_content();
                name = badge.getName();
                Utils.setStarImage(badge.getNoOfStars(), starIv,badge.getType());
                ShareImageLoader.getInstance().loadImage(badge.getImageUrl(), badgeIv,
                        ContextCompat.getDrawable(getContext(), R.drawable.badge_image));
            }
        }
        if (from == 0) {
            closeIv.setVisibility(View.VISIBLE);
            continueTv.setVisibility(View.INVISIBLE);
            tellYourFriends.setOnClickListener(AchieviedBadgeFragment.this);
        } else {
            closeIv.setVisibility(View.INVISIBLE);
            continueTv.setVisibility(View.VISIBLE);
            if (TAG.equalsIgnoreCase(Constants.TITLE_TYPE_CAUSE)) {
                Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.zoom_in);

                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        tellYourFriends.setOnClickListener(AchieviedBadgeFragment.this);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                starIv.setImageBitmap(null);
                badgeLayout.setAnimation(animation);


            } else {
                animation = ObjectAnimator.ofFloat(badgeLayout, "rotationY", 0.0f, 720f);
                animation.setDuration(2200);
                animation.setRepeatCount(0);
                animation.setInterpolator(new DecelerateInterpolator());
                animation.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        tellYourFriends.setOnClickListener(AchieviedBadgeFragment.this);
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                });
                animation.start();
            }

        }



    }


    @OnClick({R.id.continue_tv, R.id.close_iv})
    public void continueClick(View v) {
        if (v.getId() == R.id.continue_tv) {
            switch (TAG) {
                case Constants.BADGE_TYPE_CHANGEMAKER:
                    if (achievedBadgesData.getStreakBadgeAchieved() > 0) {
                        getFragmentController().replaceFragment(AchieviedBadgeFragment.newInstance(achievedBadgesData, Constants.BADGE_TYPE_STREAK), true, Constants.BADGE_TYPE_STREAK);
                    } else if (achievedBadgesData.getCauseBadgeAchieved() > 0) {
                        getFragmentController().replaceFragment(AchieviedBadgeFragment.newInstance(achievedBadgesData, Constants.BADGE_TYPE_CAUSE), true, Constants.BADGE_TYPE_CAUSE);
                    } else if (achievedBadgesData.getMarathonBadgeAchieved() > 0) {
                        getFragmentController().replaceFragment(AchieviedBadgeFragment.newInstance(achievedBadgesData, Constants.BADGE_TYPE_MARATHON), true, Constants.BADGE_TYPE_MARATHON);
                    } else if(achievedBadgesData.getTitleIds().size()>0)
                    {
                        getFragmentController().replaceFragment(AchieviedBadgeFragment.newInstance(achievedBadgesData,Constants.TITLE_TYPE_CAUSE), true,Constants.TITLE_TYPE_CAUSE+"_"+achievedBadgesData.getTitleIds().get(0));
                    }else{
                        openHomeActivityAndFinish();
                    }
                    break;
                case Constants.BADGE_TYPE_STREAK:
                    if (achievedBadgesData.getCauseBadgeAchieved() > 0) {
                        getFragmentController().replaceFragment(AchieviedBadgeFragment.newInstance(achievedBadgesData, Constants.BADGE_TYPE_CAUSE), true, Constants.BADGE_TYPE_CAUSE);
                    } else if (achievedBadgesData.getMarathonBadgeAchieved() > 0) {
                        getFragmentController().replaceFragment(AchieviedBadgeFragment.newInstance(achievedBadgesData, Constants.BADGE_TYPE_MARATHON), true, Constants.BADGE_TYPE_MARATHON);
                    } else if(achievedBadgesData.getTitleIds().size()>0)
                    {
                        getFragmentController().replaceFragment(AchieviedBadgeFragment.newInstance(achievedBadgesData,Constants.TITLE_TYPE_CAUSE), true,Constants.TITLE_TYPE_CAUSE+"_"+achievedBadgesData.getTitleIds().get(0));
                    }else{
                        openHomeActivityAndFinish();
                    }
                    break;
                case Constants.BADGE_TYPE_CAUSE:
                    if (achievedBadgesData.getMarathonBadgeAchieved() > 0) {
                        getFragmentController().replaceFragment(AchieviedBadgeFragment.newInstance(achievedBadgesData, Constants.BADGE_TYPE_MARATHON), true, Constants.BADGE_TYPE_MARATHON);
                    } else if(achievedBadgesData.getTitleIds().size()>0)
                    {
                        getFragmentController().replaceFragment(AchieviedBadgeFragment.newInstance(achievedBadgesData,Constants.TITLE_TYPE_CAUSE), true,Constants.TITLE_TYPE_CAUSE+"_"+achievedBadgesData.getTitleIds().get(0));
                    }else{
                        openHomeActivityAndFinish();
                    }
                    break;
                case Constants.BADGE_TYPE_MARATHON:
                case Constants.TITLE_TYPE_CAUSE:
                    if(achievedBadgesData.getTitleIds().size()>0)
                    {
                        getFragmentController().replaceFragment(AchieviedBadgeFragment.newInstance(achievedBadgesData,Constants.TITLE_TYPE_CAUSE), true,Constants.TITLE_TYPE_CAUSE+"_"+achievedBadgesData.getTitleIds().get(0));
                    }else {
                        openHomeActivityAndFinish();
                    }
                    break;
            }
        } else {
            openHomeActivityAndFinish();
        }
    }

    public void tellYourFriends() {
        Bitmap toShare = Utils.getBitmapFromLiveView(shareLayout);
        Utils.share(getContext(), Utils.getLocalBitmapUri(toShare, getContext()),
                String.format(shareMessage,name));
        AnalyticsEvent.create(Event.ON_SELECT_SHARE_BADGE).put("badgeId", badgeId)
                .buildAndDispatch();
    }

    private void openHomeActivityAndFinish() {
        if (from != 0 && getActivity() != null) {

            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra(Constants.BUNDLE_SHOW_RUN_STATS, true);
            startActivity(intent);

            getActivity().finish();
        } else {
            getFragmentController().goBack();
        }
    }

    @Override
    public void onClick(View view) {
        tellYourFriends();
    }
}
