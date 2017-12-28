package com.sharesmile.share;

import android.content.Context;

import com.sharesmile.share.Events.DBEvent;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.core.UnitsManager;
import com.sharesmile.share.network.NetworkAsyncCallback;
import com.sharesmile.share.network.NetworkDataProvider;
import com.sharesmile.share.network.NetworkException;
import com.sharesmile.share.rfac.models.CauseData;
import com.sharesmile.share.rfac.models.CauseList;
import com.sharesmile.share.utils.Logger;
import com.sharesmile.share.utils.SharedPrefsManager;
import com.sharesmile.share.utils.Urls;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by ankitmaheshwari on 10/11/17.
 */

public class CauseDataStore {

    private static final String TAG = "CauseDataStore";

    private static CauseDataStore uniqueInstance;

    private CauseList causeList;
    private CauseList lastVisibleCauseList;

    private CauseDataStore(){
        causeList = SharedPrefsManager.getInstance().getObject(Constants.KEY_CAUSE_LIST, CauseList.class);
        lastVisibleCauseList = SharedPrefsManager.getInstance().getObject(Constants.KEY_LAST_VISIBLE_CAUSE_LIST, CauseList.class);
    }

    /**
     Throws IllegalStateException if this class is not initialized

     @return unique CauseDataStore instance
     */
    public static CauseDataStore getInstance() {
        if (uniqueInstance == null) {
            throw new IllegalStateException(
                    "CauseDataStore is not initialized, call initialize(applicationContext) " +
                            "static method first");
        }
        return uniqueInstance;
    }

    /**
     Initialize this class using application Context,
     should be called once in the beginning by any application Component

     @param appContext application context
     */
    public static void initialize(Context appContext) {
        if (appContext == null) {
            throw new NullPointerException("Provided application context is null");
        }
        if (uniqueInstance == null) {
            synchronized (CauseDataStore.class) {
                if (uniqueInstance == null) {
                    uniqueInstance = new CauseDataStore();
                }
            }
        }
    }

    public CauseList getCauseList(){
        return causeList;
    }

    public void updateCauseList(CauseList updated){
        this.causeList = updated;
        SharedPrefsManager.getInstance().setObject(Constants.KEY_CAUSE_LIST, updated);
        UnitsManager.setExchangeRates(updated.getExchangeRates());
        if (causeList != null){
            for (CauseData causeData : causeList.getCauses()) {
                if (causeData.getApplicationUpdate() != null) {
                    int latestVersion = SharedPrefsManager.getInstance().getInt(Constants.PREF_LATEST_APP_VERSION, 0);
                    if (latestVersion < causeData.getApplicationUpdate().app_version
                            && causeData.getApplicationUpdate().app_version > BuildConfig.VERSION_CODE) {
                        SharedPrefsManager.getInstance().setBoolean(Constants.PREF_SHOW_APP_UPDATE_DIALOG, true);
                        SharedPrefsManager.getInstance().setBoolean(Constants.PREF_FORCE_UPDATE, causeData.getApplicationUpdate().force_update);
                        SharedPrefsManager.getInstance().setInt(Constants.PREF_LATEST_APP_VERSION, causeData.getApplicationUpdate().app_version);
                        SharedPrefsManager.getInstance().setString(Constants.PREF_APP_UPDATE_MESSAGE, causeData.getApplicationUpdate().message);
                    }
                }
            }
        }
    }

    public boolean isCauseAvailableForRun(CauseData causeData){
        for (CauseData cause : causeList.getCauses()) {
            if (cause.getId() == causeData.getId()){
                return cause.isActive() && !cause.isCompleted();
            }
        }
        return false;
    }

    public CauseData getFirstCause(){
        return getCausesToShow().get(0);
    }

    public void registerCauseSelection(CauseData selectedCause){
        SharedPrefsManager.getInstance().setObject(Constants.KEY_LAST_CAUSE_SELECTED, selectedCause);
    }

    public void sortCauses(List<CauseData> causes){
        CauseData lastSelectedCause = SharedPrefsManager.getInstance()
                .getObject(Constants.KEY_LAST_CAUSE_SELECTED, CauseData.class);
        final int lastSelectedCauseId = (lastSelectedCause == null) ? -1 : (int)lastSelectedCause.getId();
        Collections.sort(causes, new Comparator<CauseData>() {
            @Override
            public int compare(CauseData lhs, CauseData rhs) {
                if (lhs.getId() == lastSelectedCauseId){
                    return -1;
                }else if (rhs.getId() == lastSelectedCauseId){
                    return 1;
                } else {
                    return lhs.getOrderPriority() - rhs.getOrderPriority();
                }
            }
        });
    }


    public List<CauseData> getCausesToShow(){
        List<CauseData> activeCauses = new ArrayList<>();
        if (causeList != null){
            for (CauseData causeData : causeList.getCauses()) {
                if (causeData.isActive()) {
                    activeCauses.add(causeData);
                }else if (causeData.isCompleted()){
                    activeCauses.add(causeData);
                }
            }
        }
        return activeCauses;
    }

    public void registerVisibleCauses(){
        lastVisibleCauseList = causeList;
        SharedPrefsManager.getInstance().setObject(Constants.KEY_LAST_VISIBLE_CAUSE_LIST, lastVisibleCauseList);
    }

    public void updateCauseData() {
        NetworkDataProvider.doGetCallAsync(Urls.getCauseListUrl(), new NetworkAsyncCallback<CauseList>() {
            @Override
            public void onNetworkFailure(NetworkException ne) {
                Logger.e(TAG, "Couldn't fetch CauseList data: " + ne);
                ne.printStackTrace();
            }

            @Override
            public void onNetworkSuccess(CauseList list) {
                Logger.d(TAG, "Successfully fetched CauseList data");
                updateCauseList(list);
                EventBus.getDefault().post(new DBEvent.CauseDataUpdated(causeList));
            }
        });
    }

    public int getOverallImpact(){
        return (int) getOverallImpact(causeList);
    }

    public int getLastSeenOverallImpact(){
        if (lastVisibleCauseList != null){
            return (int) getOverallImpact(lastVisibleCauseList);
        }else {
            return 0;
        }
    }

    public boolean isNewUpdateAvailable(){
        if (causeList != null){
            if (lastVisibleCauseList != null){
                return (getOverallImpact(causeList) > getOverallImpact(lastVisibleCauseList)
                        || causeList.getCauses().size() != causeList.getCauses().size());
            }else {
                return true;
            }
        }
        return false;

    }

    private double getOverallImpact(CauseList causeList){
        return causeList.getOverallImpactInRupees();
    }


}
