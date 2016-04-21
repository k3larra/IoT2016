package iotap.mah.se.walkandrecordprototype;

import android.graphics.Color;
import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import iotap.mah.se.walkandrecordprototype.audio.Recorder;
import iotap.mah.se.walkandrecordprototype.model.Point;
import iotap.mah.se.walkandrecordprototype.model.SoundTrack;

public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        StartStopDialog.OnSelectionListener {

    private GoogleMap mMap;
    Firebase myFirebaseRef;
    Firebase soundTrackRef;
    private final static String SOUNDTRACKREF = "soundTrackRef";
    private ArrayList<Point> points = new ArrayList<Point>();
    //Firebase locRef;
    protected static final String TAG = "MapsActivity";
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    public long millisInToRecording;
    Random r = new Random();

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    // Keys for storing activity state in the Bundle.
    protected final static String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key";
    protected final static String LOCATION_KEY = "location-key";
    protected final static String LAST_UPDATED_TIME_STRING_KEY = "last-updated-time-string-key";
    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;

    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    protected LocationRequest mLocationRequest;

    /**
     * Represents a geographical location.
     */
    protected Location mCurrentLocation;

    /**
     * Tracks the status of the location updates request. Value changes when the user presses the
     * Start Updates and Stop Updates buttons.
     */
    protected Boolean mRequestingLocationUpdates = false;

    /**
     * Time when the location was updated represented as a String.
     */
    protected String mLastUpdateTime;
    private boolean walkInitiated = false;
    private final static String WALK_INITIATED = "walkinitiated";
    private Recorder recorder;
    private boolean isRecording = false;
    private boolean walkingMode = true;  //If false recordingmode

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Firebase.setAndroidContext(this);
        myFirebaseRef = new Firebase("https://soundtrackmalmo.firebaseio.com//");
        if (savedInstanceState != null) {
            // Restore value of members from saved state
            walkInitiated = savedInstanceState.getBoolean(WALK_INITIATED);
            soundTrackRef = myFirebaseRef.child(savedInstanceState.getString(SOUNDTRACKREF));
        } else {
            // Probably initialize members with default values for a new instance
        }

        if (!walkInitiated&&!walkingMode) {
            createWalk("Around Universtitestholmen", "Lars the H");
        }


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
    }

    private void drawWalk() {
        soundTrackRef = myFirebaseRef.child("-KFsyDa2mILtqdaoR-MN");
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

                    Polyline line = mMap.addPolyline(new PolylineOptions()
                            .add(new LatLng(points.get(j).getLatitude(),points.get(j).getLongitude()),
                                    new LatLng(points.get(i).getLatitude(), points.get(i).getLongitude()))
                            .width(5)
                            .color(Color.RED));
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(WALK_INITIATED,walkInitiated);
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
        // Add a marker in Sydney and move the camera
        LatLng annalindh = new LatLng(55.609047, 12.996346);
        mMap.addMarker(new MarkerOptions().position(annalindh).title("Anna Lindhs plats"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(annalindh,0));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(annalindh));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(annalindh,18));
        //allow myLocation
        mMap.setMyLocationEnabled(true);
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                new StartStopDialog().show(getSupportFragmentManager(),"hepp");
            }
        });
        if (walkingMode){
            drawWalk();
        }


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
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        //updateUI();
        //Toast.makeText(this, getResources().getString(R.string.location_updated_message),
        //        Toast.LENGTH_SHORT).show();
        Log.i(TAG,"At: lat: "+mCurrentLocation.getLatitude()+ " long "+mCurrentLocation.getLongitude());
        int time = (int)( System.currentTimeMillis()-millisInToRecording);
        //mCurrentLocation.getTime();   //Bättre
        //mCurrentLocation.getAccuracy(); //I meter 68% säkerhet att man är där 0.0 om man inte vet
        //mCurrentLocation.getSpeed();
        Point point = new Point(mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude(),time);
        //Map<String, Double> post1 = new HashMap<String, Double>();

        //post1.put("millis",r.nextDouble() );
        //post1.put("lat", mCurrentLocation.getLatitude());
        //post1.put("long", mCurrentLocation.getLongitude());
        soundTrackRef.child("SoundTrack").push().setValue(point);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
    }

    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    /**
     * Removes location updates from the FusedLocationApi.
     */
    protected void stopLocationUpdates() {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.

        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    public void createWalk(String name, String author){
        SoundTrack soundTrack = new SoundTrack(name, author,"sv");
        soundTrackRef = myFirebaseRef.push();
        soundTrackRef.setValue(soundTrack);
        recorder = new Recorder(soundTrackRef.getKey());
        walkInitiated = true;
    }

    public void startWalk(){
        //clear all old points
        if(!walkingMode) {
            soundTrackRef.child("SoundTrack").removeValue();
            if (walkInitiated) {
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

    public void stopWalk(){
        if(!walkingMode) {
            if (walkInitiated) {
                mRequestingLocationUpdates = true;
                stopLocationUpdates();
            }
            if (isRecording) {
                recorder.stopRecording();
                isRecording = false;
            }
        }
    }

    @Override
    public void OnSelectionListener(int i) {
        Log.i(TAG, "Selected: "+i);
        switch (i){
            case 0:Log.i(TAG, "Selected: Start");
                    startWalk();
                break;
            case 1:Log.i(TAG, "Selected: Play");
                if(!walkingMode) {
                    if (!isRecording) {
                        recorder.startPlaying();
                    }
                }
                break;
            case 2:Log.i(TAG, "Selected: Stop");
                stopWalk();

                break;

        }
    }
}
