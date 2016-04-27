package iotap.mah.se.walkandrecordprototype;

import android.provider.SyncStateContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.graphics.Color;
import android.location.Location;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.common.ConnectionResult;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import iotap.mah.se.walkandrecordprototype.audio.Recorder;
import iotap.mah.se.walkandrecordprototype.model.Point;
import iotap.mah.se.walkandrecordprototype.model.SoundTrack;

public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        StartStop.OnSelectionListener{

    //Maps parameters
    private GoogleMap mMap;
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    protected GoogleApiClient mGoogleApiClient;
    protected LocationRequest mLocationRequest;
    protected Location mCurrentLocation;
    protected Boolean mRequestingLocationUpdates = false;

    //Firebase stuff
    public static Firebase myFirebaseRef; //ref for all SoundTracks
    public static Firebase soundTrackRef; //Ref for current track
    private final static String SOUNDTRACKREF = "soundTrackRef";
    private final static int MAP_ZOOM = 18;

    //Implementation stuff
    protected static final String TAG = "MapsActivity";
    private ArrayList<Point> points = new ArrayList<Point>();
    public long millisInToRecording;
    Random r = new Random();
    protected String mLastUpdateTime;
    private boolean recordWalkInitiated = false;
    private Recorder recorder;
    private boolean isRecording = false;
    public boolean walkingMode = true;  //If false recordingmode
    public boolean startedProgram = false;

    // Keys for storing activity state in the Bundle.
    protected final static String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key";
    protected final static String LOCATION_KEY = "location-key";
    protected final static String LAST_UPDATED_TIME_STRING_KEY = "last-updated-time-string-key";
    private final static String WALK_INITIATED = "walkinitiated";

    //Activity stuff
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Firebase.setAndroidContext(this);
        myFirebaseRef = new Firebase("https://soundtrackmalmo.firebaseio.com//");
        if (savedInstanceState != null) {
            // Restore value of members from saved state
            recordWalkInitiated = savedInstanceState.getBoolean(WALK_INITIATED);
            soundTrackRef = myFirebaseRef.child(savedInstanceState.getString(SOUNDTRACKREF));
        } else {
            // Probably initialize members with default values for a new instance
        }

        /*
        if (!recordWalkInitiated &&!walkingMode) {
            createNewWalk("Around Universtitestholmen", "Lars the H");
        }*/



        //locRef = myFirebaseRef.child("demowalk");
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Update values using data stored in the Bundle.
        //https://github.com/googlesamples/android-play-location/blob/master/LocationUpdates/app/src/main/java/com/google/android/gms/location/sample/locationupdates/MainActivity.java
        //updateValuesFromBundle(savedInstanceState);
        // Kick off the process of building a GoogleApiClient and requesting the LocationServices
        // API.
        buildGoogleApiClient();
        showStartDialog();
        //new StartStop().show(getSupportFragmentManager(),"hepp");
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(WALK_INITIATED, recordWalkInitiated);
        savedInstanceState.putString(SOUNDTRACKREF,soundTrackRef.getKey());
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Within {@code onPause()}, we pause location updates, but leave the
        // connection to GoogleApiClient intact.  Here, we resume receiving
        // location updates if the user has requested them.
        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop location updates to save battery, but don't disconnect the GoogleApiClient object.
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }
        if (isRecording) {
            recorder.stopRecording();
        }
    }

   //Maps stuff
    /**
     * Builds a GoogleApiClient. Uses the {@code #addApi} method to request the
     * LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        Log.i(TAG, "Building GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mRequestingLocationUpdates =true;

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                if (startedProgram) {
                    new StartStop().show(getSupportFragmentManager(), "hepp");
                }else{
                    showStartDialog();
                }
            }
        });
        /*if (walkingMode){
            drawSelectedWalk();
        }*/


    }

    @Override
    public void onConnected(Bundle bundle) {
        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
    }

        //Location update stuff
    /**
         * Sets up the location request. Android has two location request settings:
         * {@code ACCESS_COARSE_LOCATION} and {@code ACCESS_FINE_LOCATION}. These settings control
         * the accuracy of the current location. This sample uses ACCESS_FINE_LOCATION, as defined in
         * the AndroidManifest.xml.
         * <p/>
         * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
         * interval (5 seconds), the Fused Location Provider API returns location updates that are
         * accurate to within a few feet.
         * <p/>
         * These settings are appropriate for mapping applications that show real-time location
         * updates.
         */
    protected void createLocationRequest() {
            mLocationRequest = new LocationRequest();

            // Sets the desired interval for active location updates. This interval is
            // inexact. You may not receive updates at all if no location sources are available, or
            // you may receive them slower than requested. You may also receive updates faster than
            // requested if other applications are requesting location at a faster interval.
            mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

            // Sets the fastest rate for active location updates. This interval is exact, and your
            // application will never receive updates faster than this value.
            mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        }
    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        LatLng current = new LatLng(location.getLatitude(), location.getLongitude());
        //mark location:
        //
        if(isRecording) {

            MarkerOptions marker = new MarkerOptions()
                    .position(current)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.here_marker))
                    .title("current");
            mMap.addMarker(marker);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current, MAP_ZOOM));
            //
            mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
            Log.i(TAG, "At: lat: " + mCurrentLocation.getLatitude() + " long " + mCurrentLocation.getLongitude());
            Log.i(TAG, "At: time: " + mCurrentLocation.getTime() + " \n\tAccuracy: " + mCurrentLocation.getAccuracy()+" \n\tSpeed: " + mCurrentLocation.getSpeed());
            int time = (int) (System.currentTimeMillis() - millisInToRecording);
            //mCurrentLocation.getTime();   //Bättre
            //mCurrentLocation.getAccuracy(); //I meter 68% säkerhet att man är där 0.0 om man inte vet
            //mCurrentLocation.getSpeed();
            Point point = new Point(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), time, mCurrentLocation.getAccuracy(),mCurrentLocation.getSpeed());
            soundTrackRef.child("SoundTrack").push().setValue(point);
        }else{
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current, MAP_ZOOM));
        }

    }

    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    protected void stopLocationUpdates() {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.

        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }


    //My Stuff
    private void drawSelectedWalk() {
            //soundTrackRef = myFirebaseRef.child("-KFsyDa2mILtqdaoR-MN");
            soundTrackRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    points.clear();
                    Log.i(TAG,dataSnapshot.child("author").getValue().toString());
                    Log.i(TAG,"Antal"+dataSnapshot.child("SoundTrack").getChildrenCount());
                    for (DataSnapshot postSnapshot: dataSnapshot.child("SoundTrack").getChildren()) {
                        Point point = postSnapshot.getValue(Point.class);
                        points.add(point);
                    }

                    for (int i = 1,j = 0; i<points.size();i++,j++) {
                        Log.i(TAG,"point: "+points.get(j).getLatitude());
                        Polyline line = mMap.addPolyline(new PolylineOptions()
                                .add(new LatLng(points.get(j).getLatitude(),points.get(j).getLongitude()),
                                        new LatLng(points.get(i).getLatitude(), points.get(i).getLongitude()))
                                .width(5)
                                .color(Color.GREEN));
                    }
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });
        }

    public void createNewWalk(String name, String author){
        SoundTrack soundTrack = new SoundTrack(name, author);
        soundTrackRef = myFirebaseRef.push();
        soundTrackRef.setValue(soundTrack);
        recorder = new Recorder(soundTrackRef.getKey());
        recordWalkInitiated = true;
    }

    public void startRecordingWalk(){
        //clear all old points
        Toast.makeText(this,"Clear old values and restart",Toast.LENGTH_SHORT).show();
        mMap.clear();
        if(!walkingMode) {
            Log.i(TAG, "Doing");
            soundTrackRef.child("SoundTrack").removeValue();
            if (recordWalkInitiated) {
                Log.i(TAG, "startlocation");
                mRequestingLocationUpdates = true;
                startLocationUpdates();
            }
            millisInToRecording = System.currentTimeMillis();
            if (!isRecording) {
                recorder.startRecording();
                isRecording = true;
            }
        }
    }

    public void stopRecordingWalk(){
        Toast.makeText(this,"Saved walk",Toast.LENGTH_SHORT).show();
        if(!walkingMode) {
            if (recordWalkInitiated) {
                mRequestingLocationUpdates = false;
                stopLocationUpdates();
            }
            if (isRecording) {
                recorder.stopRecording();
                isRecording = false;
            }
            recordWalkInitiated =false;
        }
        startedProgram = false;
    }

    //DialogFragment calls
    @Override
    public void OnSelectionListener(int i) {

        switch (i){
            case 0:
                Log.i(TAG, "Selected: Record");
                /*if (!recordWalkInitiated &&!walkingMode) {
                    Log.i(TAG, "Inisitalizing Record");
                    startNewWalk();
                    //createNewWalk("Around L1", "Lars the Boy");
                }*/
                if (recordWalkInitiated &&!walkingMode) {
                    Log.i(TAG, "Starting recording");
                    startRecordingWalk();
                }
                break;
            case 1:
                Log.i(TAG, "selected: Stop Recording");
                stopRecordingWalk();
                break;
            case 2:
                Log.i(TAG, "Start walk");
                startWalk();
                break;
            case 3:
                Log.i(TAG, "Stop walk");
                stopWalk();
                break;
        }
    }


    public void showStartDialog() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        // Create and show the dialog.
        DialogFragment newFragment = StartProgramDialog.newInstance(0);
        newFragment.show(ft, "dialog");
    }

    public void startNewWalk() {
        Log.i(TAG,"Spooky: ");
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("newwalk");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        // Create and show the dialog.
        DialogFragment newFragment = CreateNewWalk.newInstance();
        newFragment.show(ft, "newwalk");
    }
    public void setSoundTrackRef(String id){
        Log.i(TAG,"ID: " + id);
        soundTrackRef = myFirebaseRef.child(id);
    }

    public void startWalk(){
        mMap.clear();
        Log.i(TAG,"StartWalking"+walkingMode+isRecording);
        drawSelectedWalk();
        if(walkingMode) {
            if (!isRecording) {
                recorder = new Recorder(soundTrackRef.getKey());
                recorder.startPlaying();
            }
        }
    }

    private void stopWalk() {
        Log.i(TAG,"Stopwalking"+walkingMode+isRecording);
        startedProgram = false;
        mMap.clear();
        recorder.stopPlaying();
        showStartDialog();
    }

}
