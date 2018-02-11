package com.sharesmile.share.home.homescreen;

import android.view.View;

import com.sharesmile.share.utils.Utils;

/**
 * Created by ankitmaheshwari on 2/2/18.
 */

public class ShowOverlayRunnable implements Runnable {

    HomeScreenFragment homescreenFragment;
    private OnboardingOverlay overlay;
    private View target;
    private boolean isRectangular;
    private boolean cancelled;

    public ShowOverlayRunnable(HomeScreenFragment parentFragment, OnboardingOverlay overlay,
                               View target, boolean isRectangular) {
        this.homescreenFragment = parentFragment;
        this.overlay = overlay;
        this.target = target;
        this.isRectangular = isRectangular;
    }

    @Override
    public void run() {
        if (!cancelled
                && homescreenFragment.isAttachedToActivity()
                && homescreenFragment.isResumed()
                && !homescreenFragment.getFragmentController().isDrawerVisible() ){
            if (OnboardingOverlay.LETS_GO.equals(overlay)
                    && (homescreenFragment.getCurrentCause() == null
                        || homescreenFragment.getCurrentCause().isCompleted())){
                // Won't show Let's Go overlay for a completed cause card
                return;
            }
            Utils.showOverlay(overlay, target, homescreenFragment.getActivity(), isRectangular);
        }
    }

    public void cancel(){
        cancelled = true;
        overlay = null;
        target = null;
        homescreenFragment = null;
    }
}
