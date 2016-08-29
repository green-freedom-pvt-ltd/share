package com.sharesmile.share.gps;

import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.sharesmile.share.R;
import com.sharesmile.share.core.BaseFragment;
import com.sharesmile.share.gps.models.WorkoutData;
import com.sharesmile.share.utils.Logger;
import com.sharesmile.share.utils.Utils;

import java.util.List;

/**
 * Created by ankitmaheshwari1 on 25/02/16.
 */
public class RunPathFragment extends BaseFragment implements OnMapReadyCallback {

    private static final String TAG = "RunPathFragment";
    private static final String WORKOUT_DATA_KEY = "workout_data_key";

    View baseView;
    WorkoutData workoutData;
    GoogleMap googleMap;
    Polyline polyline;

    public RunPathFragment() {
        // Required empty public constructor
    }

    public static RunPathFragment newInstance(WorkoutData data) {
        RunPathFragment fragment = new RunPathFragment();
        Bundle args = new Bundle();
        args.putParcelable(WORKOUT_DATA_KEY, data);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null){
            workoutData = getArguments().getParcelable(WORKOUT_DATA_KEY);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    public boolean isFragmentAttachedToActivity(){
        return (getActivity() != null);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        baseView = inflater.inflate(R.layout.fragment_run_path, container, false);
        MapFragment mapFragment = (MapFragment) getActivity().getFragmentManager()
                                        .findFragmentById(R.id.path_map);
        mapFragment.getMapAsync(this);
        return baseView;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        boolean isWorkoutDataPresent = workoutData != null && Utils.isCollectionFilled(workoutData.getPoints());
        if (isWorkoutDataPresent){
            List<LatLng> points = workoutData.getPoints();
//            List<LatLng> points = Constants.SAMPLE_POINTS_LIST;
            Logger.d(TAG, "onMapReady: WorkoutData received = " + workoutData.toString());

            int size = points.size();
            for (int i = 0; i < size; i++){
                if (i == 0){
                    // Start point
                    MarkerOptions sMarkerOptions = new MarkerOptions().position(points.get(0))
                            .title("Start");
                    googleMap.addMarker(sMarkerOptions);
                }
                else if (i == (size - 1)){
                    // End Point
                    MarkerOptions eMarkerOptions = new MarkerOptions()
                            .position(points.get(size -1))
                            .title("End");
                    googleMap.addMarker(eMarkerOptions);
                }else{
                    // Points in middle
                    MarkerOptions mMarkerOptions = new MarkerOptions()
                            .position(points.get(i)).icon(BitmapDescriptorFactory
                                    .fromResource(R.drawable.ic_pin_drop_black_18dp));
                    googleMap.addMarker(mMarkerOptions);
                }
            }

            PolylineOptions options = new PolylineOptions().addAll(points)
                    .width(6).color(Color.GREEN);
            polyline = googleMap.addPolyline(options);
            CameraPosition cameraPosition = new CameraPosition.Builder().target(points.get(size/2))
                    .zoom(14.0f).build();
            CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
            googleMap.moveCamera(cameraUpdate);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        FragmentManager fragmentManager = getActivity().getFragmentManager();
        MapFragment googleMapFragment = (MapFragment) fragmentManager.findFragmentById(R.id.path_map);
        if (googleMapFragment != null) {
            fragmentManager.beginTransaction().remove(googleMapFragment).commit();
            fragmentManager.executePendingTransactions();
        }
    }
}
