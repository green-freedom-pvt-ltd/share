package com.sharesmile.share.rfac;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.sharesmile.share.MainApplication;
import com.sharesmile.share.R;
import com.sharesmile.share.core.BaseFragment;
import com.sharesmile.share.network.NetworkAsyncCallback;
import com.sharesmile.share.network.NetworkDataProvider;
import com.sharesmile.share.network.NetworkException;
import com.sharesmile.share.rfac.models.CausesPage;
import com.sharesmile.share.utils.Logger;

/**
 * Created by ankitmaheshwari1 on 09/03/16.
 */
public class CauseSelectionFragment extends BaseFragment{

    private static final String TAG = "CauseSelectionFragment";

    private static final String FETCH_CAUSES_URL = "https://api.myjson.com/bins/2c7gx";


    private ProgressBar progressBar;
    private CausesDataStore causesDataStore;

    public static CauseSelectionFragment newInstance() {
        CauseSelectionFragment fragment = new CauseSelectionFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            //TODO: take data from arguments
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.cause_selection_page, container, false);

        progressBar = (ProgressBar) rootView.findViewById(R.id.cause_selection_progress_bar);
        progressBar.setVisibility(View.VISIBLE);
        causesDataStore = new CausesDataStore();
        fetchPageData(0);
        return rootView;
    }

    private void fetchPageData(int pgNum){
        Logger.d(TAG, "Fetching Causes Data");
        NetworkDataProvider.doGetCallAsync(FETCH_CAUSES_URL, new NetworkAsyncCallback<CausesPage>(){
            @Override
            public void onNetworkFailure(NetworkException ne) {
                Logger.e(TAG, "onNetworkFailure: Can't fetch events page data: " + ne.getMessageFromServer(), ne);
                MainApplication.showToast("Unable to fetch events");
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onNetworkSuccess(CausesPage causesPage) {
                Logger.d(TAG, "onNetworkSuccess");
                editDataSet(causesPage);
                //TODO: Show Data on UI

                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void editDataSet(CausesPage causespage){
        causesDataStore.addPageData(causespage);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
