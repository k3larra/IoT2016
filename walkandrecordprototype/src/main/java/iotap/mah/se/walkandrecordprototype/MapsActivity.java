package iotap.mah.se.walkandrecordprototype;

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
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.DateFormat;
import java.util.Date;
import java.util.Random;

import iotap.mah.se.walkandrecordprototype.audio.Recorder;
import iotap.mah.se.walkandrecordprototype.audio.StreamingPlayer;
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

    private final static int MAP_ZOOM = 18;

    //Implementation stuff
    protected static final String TAG = "MapsActivity";
   // private ArrayList<Point> points = new ArrayList<Point>();
    public long millisInToRecording;
    Random r = new Random();
    protected String mLastUpdateTime;
    private Recorder recorder;
    private StreamingPlayer player;
    protected SoundTrack activeTrack;
    
    // Keys for storing activity state in the Bundle.
    private final static String SOUNDTRACKREF = "soundTrackRef";
    private final static String WALK_INITIATED = "walkinitiated";
    private boolean recordWalkInitiated = false;
    private final static String IS_RECORDING = "isrecording";
    private boolean isRecording = false;
    private final static String WALKINGMODE = "walkingmode";
    public boolean walkingMode = true;  //If false recordingmode
    private final static String STARTED_PROGRAM = "startedprogram";
    public boolean startedProgram = false;
    private final static String CREATING_NEW_WALK = "creatingnewwalk";
    public boolean creatingNewWalk = false;



    //Activity stuff
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Firebase.setAndroidContext(this);
        myFirebaseRef = new Firebase("https://soundtrackmalmo.firebaseio.com//");
        if (savedInstanceState != null) {
            // Restore value of members from saved state
            recordWalkInitiated = savedInstanceState.getBoolean(WALK_INITIATED);
            isRecording = savedInstanceState.getBoolean(IS_RECORDING);
            walkingMode = savedInstanceState.getBoolean(WALKINGMODE);
            startedProgram = savedInstanceState.getBoolean(STARTED_PROGRAM);
            creatingNewWalk = savedInstanceState.getBoolean(CREATING_NEW_WALK);
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
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(WALK_INITIATED, recordWalkInitiated);
        savedInstanceState.putBoolean(IS_RECORDING, isRecording);
        savedInstanceState.putBoolean(WALKINGMODE, walkingMode);
        savedInstanceState.putBoolean(STARTED_PROGRAM, startedProgram);
        savedInstanceState.putBoolean(CREATING_NEW_WALK, creatingNewWalk);
        savedInstanceState.putString(SOUNDTRACKREF,soundTrackRef.getKey());
        super.onSaveInstanceState(savedInstanceState);
        //restore active walk here!!!!!
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
        if (player!=null) {
            player.pause();
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
                    if (creatingNewWalk){
                        startRecordingNewWalk();
                    }else{
                        new StartStop().show(getSupportFragmentManager(), "hepp");
                    }
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
            //Log.i(TAG, "At: lat: " + mCurrentLocation.getLatitude() + " long " + mCurrentLocation.getLongitude());
            //Log.i(TAG, "At: time: " + mCurrentLocation.getTime() + " \n\tAccuracy: " + mCurrentLocation.getAccuracy()+" \n\tSpeed: " + mCurrentLocation.getSpeed());
            int time = (int) (System.currentTimeMillis() - millisInToRecording);
            Point point = new Point(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), time, mCurrentLocation.getAccuracy(),mCurrentLocation.getSpeed());
            soundTrackRef.child("SoundTrack").push().setValue(point);
        }else{
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current, MAP_ZOOM));
        }
        if(walkingMode) {
            Log.i(TAG,"-------NEW_POSITION-------");
            Log.i(TAG, "Arrived at point: lat: " + mCurrentLocation.getLatitude() + " long " + mCurrentLocation.getLongitude());
            if(activeTrack!=null) {
                if (activeTrack.hasWalkStarted()) {
                    Log.i(TAG, "Next point is nbr: " + activeTrack.getTheActivePointIndex() + " at lat: " + activeTrack.getCurrentPoint().getLatitude() + " long " + activeTrack.getCurrentPoint().getLongitude());
                    Log.i(TAG, "Distance to next point: " + (float) Math.round(location.distanceTo(activeTrack.getCurrentPoint().getLocation())) * 100f / 100 + "m away");
                }
                //3mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current, MAP_ZOOM));
                //points.
                //Check if walk should start and if true actitate it
                if (location.distanceTo(activeTrack.getFirstPoint().getLocation()) < Constants.whatIsClose) {
                    if (!activeTrack.hasWalkStarted() && !activeTrack.hasWalkEnded()) {
                        //Ok start the walk
                        activeTrack.setNextPointActive();
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(activeTrack.getCurrentPoint().getLatLng(), MAP_ZOOM));
                        //Play next part
                        Log.i(TAG, "Walk initiated");
                        Log.i(TAG, "**Playing from: " + activeTrack.getCurrentPoint().getMillisIntoSound() + " to " + activeTrack.getNextPoint().getMillisIntoSound() + " with duration " + Math.round((activeTrack.getNextPoint().getMillisIntoSound() - activeTrack.getCurrentPoint().getMillisIntoSound()) * 100) / 100000f);
                        player.playFromTo(activeTrack.getCurrentPoint().getMillisIntoSound(), activeTrack.getNextPoint().getMillisIntoSound());
                    }

                }
                if (activeTrack.hasWalkStarted()) {
                    Log.i(TAG, "Ongoing walk");

                }

                if (activeTrack.hasWalkStarted()) {
                    if (location.distanceTo(activeTrack.getNextPoint().getLocation()) < Constants.whatIsClose) {
                        if (activeTrack.hasWalkStarted() && !activeTrack.hasWalkEnded()) {
                            //Ok next point reached
                            activeTrack.setNextPointActive();
                            Log.i(TAG, "The next point reached, move over to point: " + activeTrack.getTheActivePointIndex());
                            Log.i(TAG, "New point to reach at index: " + activeTrack.getTheActivePointIndex() + " distance " + (float) Math.round(location.distanceTo(activeTrack.getCurrentPoint().getLocation())) * 100f / 100 + " meters at lat: " + activeTrack.getCurrentPoint().getLatitude() + " long " + activeTrack.getCurrentPoint().getLongitude());
                            //Move Camera in position
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(activeTrack.getCurrentPoint().getLatLng(), MAP_ZOOM));
                            //Play next part

                            if (!activeTrack.isLastPoint()) {
                                Log.i(TAG, "**Playing " +activeTrack.getTheActivePointIndex()+ " from: " + activeTrack.getCurrentPoint().getMillisIntoSound() + " to " + activeTrack.getNextPoint().getMillisIntoSound() + " with duration " + Math.round((activeTrack.getNextPoint().getMillisIntoSound() - activeTrack.getCurrentPoint().getMillisIntoSound()) * 100) / 100000f);
                                player.playFromTo(activeTrack.getCurrentPoint().getMillisIntoSound(), activeTrack.getNextPoint().getMillisIntoSound());
                            } else { //On the last point
                                Log.i(TAG, "**Playing " +activeTrack.getTheActivePointIndex()+"+ from: " + activeTrack.getCurrentPoint().getMillisIntoSound() + " to the end");
                                player.playFrom(activeTrack.getCurrentPoint().getMillisIntoSound());
                            }
                        }

                    }
                }
                if (activeTrack.hasWalkEnded()) {
                    Log.i(TAG, "Walk really ended");
                    player.trashPlayer();
                }
                Log.i(TAG, "-------END_POSITION_UPDATE-------");
            }
        }

    }

    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }


    //My Stuff
    private void drawSelectedWalk() {
            //soundTrackRef = myFirebaseRef.child("-KFsyDa2mILtqdaoR-MN");
            soundTrackRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    activeTrack.getPoints().clear();
                    Log.i(TAG,dataSnapshot.child("author").getValue().toString());
                    Log.i(TAG,"Antal"+dataSnapshot.child("SoundTrack").getChildrenCount());
                    for (DataSnapshot postSnapshot: dataSnapshot.child("SoundTrack").getChildren()) {
                        Point point = postSnapshot.getValue(Point.class);
                        activeTrack.getPoints().add(point);
                    }
                    //activeTrack.setPoints(points);
                    drawTrack();

                    //Logging
                    for (int i =0; i<activeTrack.getPoints().size();i++) {
                        Point p = activeTrack.getPoints().get(i);
                        Log.i(TAG,"Point nbr: "+i+ " lat: "+ p.getLatitude()+" long: "+p.getLongitude());
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
        recorder = new Recorder(soundTrackRef.getKey(),this);
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
        //Upload file
        if(!walkingMode) {
            if (recordWalkInitiated) {
                mRequestingLocationUpdates = false;
                stopLocationUpdates();
            }
            if (isRecording) {

                recorder.stopRecording();
                recorder.saveRecordingToCloud();
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
                    startRecordingNewWalk();
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

    public void startRecordingNewWalk() {
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
        Log.i(TAG,"StartWalking"+walkingMode+ isRecording);
        drawSelectedWalk();

        if(walkingMode) {
            if (!isRecording) {
                //recorder = new Recorder(soundTrackRef.getKey());
                //recorder.startPlaying();
                //recorder.startStreaming();
                player = new StreamingPlayer(soundTrackRef.getKey(),this);
            }
        }
    }

    private void stopWalk() {
        Log.i(TAG,"Stopwalking"+walkingMode+ isRecording);
        startedProgram = false;
        mMap.clear();
        //recorder.stopPlaying();
        if(player!=null){
            player.trashPlayer();
        }

        showStartDialog();
    }

    public void drawTrack(){
        if (activeTrack!=null) {
            for (int i = 1, j = 0; i < activeTrack.getPoints().size(); i++, j++) {
                Polyline line = mMap.addPolyline(new PolylineOptions()
                        .add(new LatLng(activeTrack.getPoints().get(j).getLatitude(), activeTrack.getPoints().get(j).getLongitude()),
                                new LatLng(activeTrack.getPoints().get(i).getLatitude(), activeTrack.getPoints().get(i).getLongitude()))
                        .width(5)
                        .color(getResources().getColor(R.color.colorColorPlaying)));
            }
        }
    }

}
