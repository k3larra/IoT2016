package com.example.k3lara.assignment4.model;

import android.content.Context;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.example.k3lara.assignment4.QuissDialogFragment;
import com.example.k3lara.assignment4.R;
import com.example.k3lara.assignment4.RestartDialog;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by K3LARA on 2015-08-25.
 */
public class Point implements Comparable<Point>{
    private LatLng _latLong;
    private boolean _visible;
    private int sound = R.raw.elephant;
    private int order;
    public static final String TAG = "Point";
    private Assignment assignment;


    public Point(LatLng _latLong, boolean _visible, int order, Assignment assignment) {
        this._latLong = _latLong;
        this._visible = _visible;
        this.order = order;
        this.assignment = assignment;
    }


    public Assignment getAssignment(){
        return assignment;
    }

    public LatLng get_latLong() {
        return _latLong;
    }

    public int getOrder() {
        return order;
    }

    public double getLatitude(){
        return _latLong.latitude;
    }

    public double getLongitude(){
        return _latLong.longitude;
    }

    public Location get_location(){
        Location l = new Location("One");
        l.setLatitude(_latLong.latitude);
        l.setLongitude(_latLong.longitude);
        return l;
    }

    @Override
    public int compareTo(Point another) {
        return getOrder()-another.getOrder();
    }

    public void doYourThing(Context c) {
        Log.i(TAG, "DoYour thing for Pos: " + Track.getInstance().getPosNbr());
        assignment.setState(Assignment.STATE.STARTED);
        showDialog(c);

    }

    void showDialog(Context c) {
        if (c instanceof AppCompatActivity){
            AppCompatActivity a = (AppCompatActivity)c;
            FragmentManager fm = a.getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            Fragment prev = fm.findFragmentByTag("quiss");
            if (prev != null) {
                ft.remove(prev);
            }
            ft.addToBackStack(null);
            QuissDialogFragment newQuiss = new QuissDialogFragment();
            newQuiss.show(ft, "quiss");
        }
    }


    public int getSound() {
        return sound;
    }
    public int playSequence() {
        return sound;
    }
}
