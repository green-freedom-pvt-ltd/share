package com.sharesmile.share.rfac;

import com.sharesmile.share.rfac.models.Cause;
import com.sharesmile.share.rfac.models.CausesPage;
import com.sharesmile.share.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ankitmaheshwari1 on 09/03/16.
 */
public class CausesDataStore {

    private static final String TAG = "CausesDataStore";

    private Map<Integer, List<Cause>> causePageWiseMap;
    private List<Cause> causesList;
    private int totalCount;

    public CausesDataStore(){
        this.causePageWiseMap = new HashMap<>();
        this.causesList = new ArrayList<>();
        totalCount = 0;
    }

    public void addPageData(CausesPage data){
        int pgNum = data.getPageNum();
        if (causePageWiseMap.containsKey(pgNum)){
            //Already there, do nothing
            return;
        }
        List<Cause> causesInPage = data.getCauses();
        if (!Utils.isCollectionFilled(causesInPage)){
            // Nothing to do, return
            return;
        }

        causePageWiseMap.put(pgNum, causesInPage);
        for (Cause cause : causesInPage){
            causesList.add(cause);
        }
        totalCount = causesList.size();
    }

    public int getTotalCauses(){
        return  totalCount;
    }

    public List<Cause> getCausesList(){
        return causesList;
    }

    public Cause get(int index){
        return causesList.get(index);
    }
}
