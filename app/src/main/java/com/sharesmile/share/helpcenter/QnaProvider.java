package com.sharesmile.share.helpcenter;

import java.util.List;

/**
 * Created by ankitmaheshwari on 8/29/17.
 */

public interface QnaProvider {

    void fetchQnas(QnaLoadedCallback callback);

    interface QnaLoadedCallback{
        void onLoadSuccess(List<Qna> qnas);
        void onLoadFailure();
    }
}
