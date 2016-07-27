package com.sharesmile.share.sync;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.sharesmile.share.CauseDao;
import com.sharesmile.share.Events.DBEvent;
import com.sharesmile.share.MainApplication;
import com.sharesmile.share.R;
import com.sharesmile.share.WorkoutDao;
import com.sharesmile.share.network.NetworkAsyncCallback;
import com.sharesmile.share.network.NetworkDataProvider;
import com.sharesmile.share.network.NetworkException;
import com.sharesmile.share.rfac.models.CauseData;
import com.sharesmile.share.rfac.models.CauseList;
import com.sharesmile.share.utils.Logger;
import com.sharesmile.share.utils.Urls;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * helper methods.
 */
public class SyncTaskManger extends IntentService {
    private static final String ACTION_CAUSE = "com.sharesmile.share.sync.action.cause";
    private static final String ACTION_UPDATE_RUN = "com.sharesmile.share.sync.action.updaterundata";


    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "com.sharesmile.share.sync.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "com.sharesmile.share.sync.extra.PARAM2";

    public SyncTaskManger() {
        super("SyncTaskManger");
    }

    public static void startCauseSync(Context context) {
        Intent intent = new Intent(context, SyncTaskManger.class);
        intent.setAction(ACTION_CAUSE);
        context.startService(intent);
    }

    public static void startRunDataUpdate(Context context) {
        Intent intent = new Intent(context, SyncTaskManger.class);
        intent.setAction(ACTION_UPDATE_RUN);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_CAUSE.equals(action)) {
                updateCauses();
            }else if(ACTION_UPDATE_RUN.equals(action)){
                SyncHelper.updateWorkoutData();
            }
        }
    }

    private void updateCauses() {

        try {
            CauseList causeList = NetworkDataProvider.doGetCall(Urls.getCauseListUrl(), CauseList.class);
            CauseDao mCauseDao = MainApplication.getInstance().getDbWrapper().getCauseDao();

            CauseList activeCauseList = new CauseList();
            activeCauseList.setCauses(new ArrayList<CauseData>());
            for (CauseData data : causeList.getCauses()) {
                Picasso.with(this).load(data.getCauseThankYouImage()).fetch();
                Picasso.with(this).load(data.getSponsor().getLogoUrl()).fetch();
                Picasso.with(this).load(data.getImageUrl()).fetch();
                if (data.isActive()) {
                    activeCauseList.getCauses().add(data);
                }
                mCauseDao.insertOrReplace(data.getCauseDbObject());
            }

            EventBus.getDefault().post(new DBEvent.CauseDataUpdated(activeCauseList));

        } catch (NetworkException e) {
            e.printStackTrace();
            return;
        }
    }

}
