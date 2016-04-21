package iotap.mah.se.walkandrecordprototype.model;

/**
 * Created by K3LARA on 2016-04-20.
 */
public class Point {
    private double latitude;
    private double longitude;
    private int millisIntoSound;

    public Point() {
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


}
