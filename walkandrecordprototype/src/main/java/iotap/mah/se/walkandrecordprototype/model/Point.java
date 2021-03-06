package iotap.mah.se.walkandrecordprototype.model;

import android.location.Location;
import android.location.LocationManager;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.nearby.sharing.LocalContent;

/**
 * Created by K3LARA on 2016-04-20.
 */

@JsonIgnoreProperties(ignoreUnknown=true)
public class Point {
    private double latitude;
    private double longitude;
    private int millisIntoSound;
    private float accuracy;
    private float speed;


    public Point() {
    }

    public Point(double latitude, double longitude, int millisIntoSound, float accuracy, float speed) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.millisIntoSound = millisIntoSound;
        this.accuracy = accuracy;
        this.speed = speed;
    }

    public Point(double latitude, double longitude, int millisIntoSound) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.millisIntoSound = millisIntoSound;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }


    public int getMillisIntoSound() {
        return millisIntoSound;
    }

    public void setMillisIntoSound(int millisIntoSound) {
        this.millisIntoSound = millisIntoSound;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public Location getLocation() {
        Location l = new Location("");
        l.setLongitude(this.longitude);
        l.setLatitude(this.latitude);
        return l;
    }

    public LatLng getLatLng() {

        return new LatLng(this.latitude, this.longitude);
    }

}
