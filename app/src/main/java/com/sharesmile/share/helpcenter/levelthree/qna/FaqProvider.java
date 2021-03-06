package com.sharesmile.share.helpcenter.levelthree.qna;

import com.sharesmile.share.core.event.UpdateEvent;
import com.sharesmile.share.core.application.MainApplication;
import com.sharesmile.share.core.sync.SyncService;
import com.sharesmile.share.core.Logger;
import com.sharesmile.share.helpcenter.levelthree.qna.model.Qna;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Created by ankitmaheshwari on 8/29/17.
 */

public class FaqProvider implements QnaProvider {

    private static final String TAG = "FaqProvider";

    private WeakReference<QnaLoadedCallback> callbackWeakReference;

    @Override
    public void fetchQnas(QnaLoadedCallback callback){
        this.callbackWeakReference = new WeakReference<>(callback);
        List<Qna> qnas = MainApplication.getInstance().getFaqsToShow();
        if (!qnas.isEmpty()){
            callback.onLoadSuccess(qnas);
        }else {
            EventBus.getDefault().register(this);
            EventBus.getDefault().post(new TriggerFaqSync());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(UpdateEvent.FaqsUpdated faqsUpdated) {
        Logger.d(TAG, "onEvent: FaqsUpdated");
        EventBus.getDefault().unregister(this);
        if (callbackWeakReference != null && callbackWeakReference.get() != null){
            if (faqsUpdated.isSuccess()){
                // Return the faqList to Caller
                Logger.d(TAG, "FAQs loaded successfully");
                List<Qna> qnas = MainApplication.getInstance().getFaqsToShow();
                callbackWeakReference.get().onLoadSuccess(qnas);
            }else {
                // Notify caller about failure
                callbackWeakReference.get().onLoadFailure();
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onEvent(TriggerFaqSync triggerSync) {
        Logger.d(TAG, "onEvent: TriggerFaqSync");
        SyncService.updateFaqs();
    }

    private static class TriggerFaqSync {
    }

}
