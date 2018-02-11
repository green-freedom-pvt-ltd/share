package com.sharesmile.share.rfac;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.sharesmile.share.R;
import com.sharesmile.share.utils.SharedPrefsManager;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.facebook.FacebookSdk.getApplicationContext;
import static com.sharesmile.share.core.config.Config.CHAT_TOOL_TIP_DISPLAY_MAX_COUNT;
import static com.sharesmile.share.core.Constants.PREFS_CHAT_TOOLTIP_DISPLAY_COUNT;

/**
 * Created by ankitmaheshwari on 8/29/17.
 */

public class FeedbackChatContainer {

    @BindView(R.id.tooltip_chat)
    View chatTooltip;

    @BindView(R.id.bt_chat)
    View chatButton;

    private View containerView;

    private ClickListener listener;

    public FeedbackChatContainer(View containerView, ClickListener listener){
        this.containerView = containerView;
        this.listener = listener;
        ButterKnife.bind(this, containerView);
        init();
    }

    private void init(){
        // Step: Decide whether to show tool tip or not
        int displayCount = SharedPrefsManager.getInstance().getInt(PREFS_CHAT_TOOLTIP_DISPLAY_COUNT);
        if (displayCount > CHAT_TOOL_TIP_DISPLAY_MAX_COUNT){
            // Has been displayed more than 3 times, won't display
            chatTooltip.setVisibility(View.GONE);
        }else {
            // Step: Display tooltip
            chatTooltip.setVisibility(View.VISIBLE);
            // Step: Increment display count
            SharedPrefsManager.getInstance().setInt(PREFS_CHAT_TOOLTIP_DISPLAY_COUNT, displayCount + 1);
            // Fade out animation of tooltip
            startFadeOutAnimation();
        }
    }

    public void startFadeOutAnimation() {
        Animation fadeOutAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out_animation);
        fadeOutAnimation.setFillAfter(true);
        chatTooltip.startAnimation(fadeOutAnimation);
    }

    @OnClick(R.id.bt_chat)
    public void chatClick(){
        if (listener != null){
            listener.onChatClicked();
        }
    }


    public interface ClickListener {
        void onChatClicked();
    }

}
