package com.sharesmile.share.events;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.sharesmile.share.MainApplication;
import com.sharesmile.share.R;
import com.sharesmile.share.events.models.EventsPageData;
import com.sharesmile.share.network.NetworkAsyncCallback;
import com.sharesmile.share.network.NetworkDataProvider;
import com.sharesmile.share.network.NetworkException;
import com.sharesmile.share.utils.Logger;

/**
 * Use the {@link EventsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EventsFragment extends Fragment {

    private static final String PARAM_TITLE = "param_title";
    private static final String FETCH_EVENT_URL = "https://api.myjson.com/bins/45quf";

    private static final String TAG = "EventsFragment";

    private String title;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private ProgressBar progressBar;
    private EventsDataStore eventsDataStore;


    public EventsFragment() {
        // Required empty public constructor
    }

    public static EventsFragment newInstance(String title) {
        EventsFragment fragment = new EventsFragment();
        Bundle args = new Bundle();
        args.putString(PARAM_TITLE, title);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            title = getArguments().getString(PARAM_TITLE);
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
        View rootView = inflater.inflate(R.layout.fragment_events, container, false);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.events_recycler_view);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        progressBar = (ProgressBar) rootView.findViewById(R.id.events_progress_bar);
        progressBar.setVisibility(View.VISIBLE);
        eventsDataStore = new EventsDataStore();
        fetchPageData(0);
        return rootView;
    }

    private void fetchPageData(int pgNum){
        Logger.d(TAG, "Fetching Events Data");
        NetworkDataProvider.doGetCallAsync(FETCH_EVENT_URL, new NetworkAsyncCallback<EventsPageData>(){
            @Override
            public void onNetworkFailure(NetworkException ne) {
                Logger.e(TAG, "onNetworkFailure: Can't fetch events page data: " + ne.getMessageFromServer(), ne);
                MainApplication.showToast("Unable to fetch events");
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onNetworkSuccess(EventsPageData eventsPageData) {
                Logger.d(TAG, "onNetworkSuccess");
                editDataSet(eventsPageData);
                initAdapter();
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void editDataSet(EventsPageData eventsPageData){
        eventsDataStore.addPageData(eventsPageData);
    }

    private void initAdapter(){
        mAdapter = new EventsAdapter(getActivity(), eventsDataStore);
        mRecyclerView.setAdapter(mAdapter);
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
