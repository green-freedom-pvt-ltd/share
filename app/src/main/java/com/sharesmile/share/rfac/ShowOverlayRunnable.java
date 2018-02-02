package com.sharesmile.share.rfac;

import android.view.View;

import com.sharesmile.share.core.BaseFragment;
import com.sharesmile.share.utils.Utils;

/**
 * Created by ankitmaheshwari on 2/2/18.
 */

public class ShowOverlayRunnable implements Runnable {

    BaseFragment parentFragment;
    private OnboardingOverlay overlay;
    private View target;
    private boolean isRectangular;
    private boolean cancelled;

    public ShowOverlayRunnable(BaseFragment parentFragment, OnboardingOverlay overlay,
                               View target, boolean isRectangular) {
        this.parentFragment = parentFragment;
        this.overlay = overlay;
        this.target = target;
        this.isRectangular = isRectangular;
    }

    @Override
    public void run() {
        if (!cancelled
                && parentFragment.isAttachedToActivity()
                && parentFragment.isResumed()
                && !parentFragment.getFragmentController().isDrawerVisible()){
            Utils.showOverlay(overlay, target, parentFragment.getActivity(), isRectangular);
        }
    }

    public void cancel(){
        cancelled = true;
        overlay = null;
        target = null;
        parentFragment = null;
    }
}
