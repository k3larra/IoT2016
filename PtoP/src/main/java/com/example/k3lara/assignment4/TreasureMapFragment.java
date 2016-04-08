package com.example.k3lara.assignment4;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.k3lara.assignment4.model.Assignment;
import com.example.k3lara.assignment4.model.Point;
import com.example.k3lara.assignment4.model.Track;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

public class TreasureMapFragment extends SupportMapFragment implements
        OnMapReadyCallback,
        LocationListener,
        GoogleApiClient.ConnectionCallbacks{


    public static final String TAG = "TreasureMapFragment";
    private GoogleMap mMap;
    private Marker currentMarker;
    private  MediaPlayer mediaPlayer;
    public static final String PREFS_NAME = "MySavedLocations";
    public static int posRecordNbr = 0;
    public static int thisRecordBatch = 0;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private boolean mRequestingLocationUpdates=false;
    ///save state!!!! https://developer.android.com/training/location/receive-location-updates.html


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Set the track should that happen here????
        if (Track.getTrackState()== Track.TRACKSTATE.NOT_STARTED) {
            Track.getInstance().loadTrack();
            Track.setTrackState(Track.TRACKSTATE.STARTED);
        }
        //And get the map
        getMapAsync(this);
        //Since it is a SupportMapFragment the xml is already there, this method waits for the map to load and then calls onMapReady
        //With google LocationListener and not Android No need for LocationService or LocationManager at all.
        //But we need this!!
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onPause() {
        super.onPause();
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        mRequestingLocationUpdates = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected() && !mRequestingLocationUpdates) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            mRequestingLocationUpdates = true;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        mMap.setMyLocationEnabled(true);
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        setMarker();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Track.getInstance().getCurrentPoint().get_latLong(), 18));
    }

    private void setMarker() {
        if (mMap!=null) {
            if (currentMarker!=null) {
                currentMarker.remove();
            }
            Point p = Track.getInstance().getCurrentPoint();
            if (p != null) {
                currentMarker = mMap.addMarker(new MarkerOptions()
                                .position(Track.getInstance().getCurrentPoint().get_latLong())
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_package_variant_closed_red_18dp))
                                        //.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
                                .title("Melbourne")
                                .alpha(1)
                );
            }
        }
    }


    @Override
    public void onLocationChanged(Location location) {
        if(Constants.DEBUG){
            Log.i(TAG, "TRACKSTATE: " +Track.getTrackState() );
            List<Point> track =Track.getInstance().getTrack();
            for (Point p: Track.getInstance().getTrack())
            {
                Log.i(TAG,"Points: Order: " + p.getOrder() +" STATE: " +p.getAssignment().getState() );
            }
        }

        if (Track.getTrackState()==Track.TRACKSTATE.NOT_STARTED){
            setMarker();
            Track.setTrackState(Track.TRACKSTATE.STARTED);
        }

        LatLng newLatLng = new LatLng(location.getLatitude(), location.getLongitude()); //convert between LatLng and Location
        //Is last Assignment finished??
        if (Track.getInstance().solvedAllAssignmentsSoFar()){ //Next point
            Track.getInstance().getNextPoint();
            Log.i(TAG, "Next Point");
            setMarker();
        }
        //Have we reached the goal
        if (Track.getInstance().reachedGoal()){  //GOAL
            Log.i(TAG, "Reached Goal");
            FragmentManager fm = getActivity().getSupportFragmentManager();
            Fragment prev = fm.findFragmentByTag("goal");
            if (prev == null) {
                FragmentTransaction ft = fm.beginTransaction();
                ft.addToBackStack(null);
                // Create and show the dialog.
                GoalDialog goalDialog = new GoalDialog();
                goalDialog.show(ft, "goal");
            }
        }
        //
        Point p = Track.getInstance().getCurrentPoint();
        if (p!=null) {
            if(Track.getTrackState() != Track.TRACKSTATE.FAILED){
                if (p.getAssignment().getState()== Assignment.STATE.FAILED) {
                    Track.setTrackState(Track.TRACKSTATE.FAILED); //OK fail all
                    Log.i(TAG, "Restart");
                    FragmentManager fm = getActivity().getSupportFragmentManager();
                    Fragment prev = fm.findFragmentByTag("restart");
                    if (prev == null) {
                        FragmentTransaction ft = fm.beginTransaction();
                        ft.addToBackStack(null);
                        // Create and show the dialog.
                        RestartDialog restartDialog = new RestartDialog();
                        restartDialog.show(ft, "restart");
                    }
                }
            }
            //Start New Assignment?
            Log.i(TAG, "PosNbr in onLocationChanged: BEFORE: posNbr: " + Track.getInstance().getPosNbr() + " p.order() " + p.getOrder() + " distance: " + location.distanceTo(p.get_location()));
            if (p.getAssignment().getState() == Assignment.STATE.NOT_STARTED) {  //Already on this
                if (location.distanceTo(p.get_location()) < Constants.TARGET_DISTANCE) {
                    Log.i(TAG, " CLOSE Distance to: point: " + p.getOrder() + " m " + location.distanceTo(p.get_location()));
                    //Bells and whistles
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(p.getLatitude(), p.getLongitude()), 18));
                    p.doYourThing(getActivity());
                    currentMarker.remove();
                }
            }
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "OnConnected");
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        mRequestingLocationUpdates = true;

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "onConnectionSuspended");
    }
}
