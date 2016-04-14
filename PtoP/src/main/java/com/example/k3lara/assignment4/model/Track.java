package com.example.k3lara.assignment4.model;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by K3LARA on 2015-09-05.
 */
public class Track {

    public final static String TAG = "Track";
    private static Track ourInstance = new Track();
    private static List<Point> track = new ArrayList<Point>();
    private static int posNbr = 0;
    private static TRACKSTATE trackState = TRACKSTATE.NOT_STARTED;
    public static TRACKSTATE getTrackState() {
        return trackState;
    }
    public static void setTrackState(TRACKSTATE trackState) {
        Track.trackState = trackState;
    }

    public enum TRACKSTATE {
        NOT_STARTED, STARTED, FAILED, REACHEDGOAL
    }


    public static Track getInstance() {
        return ourInstance;
    }

    private Track() {}

    public void loadTrack(){
        track.clear();
        trackState = TRACKSTATE.NOT_STARTED;
        //setTestTrackNiagara();
        setTestTrackLinero();
    }

    public List<Point> getTrack(){
        return track;
    }


    public Point getNextPoint(){
        Point p;
        if (posNbr < (track.size()-1)){
            posNbr =posNbr+1;
            p = track.get(posNbr);
        }else{
            p=null;
        }
        return p;
    }

    public Point getCurrentPoint(){
        Point p;
        try {
            p = track.get(posNbr);
        }catch(Exception e){
            p=null;
        }
        return p;
    }

    public int getPosNbr(){
        return posNbr;
    }


    public boolean solvedAllAssignmentsSoFar(){
        boolean solvedAllSoFar = true;
        for (int i=0; i <= posNbr; i++){
            if (track.get(i).getAssignment().getState()!= Assignment.STATE.SUCCESFULL){
                solvedAllSoFar = false;
            }
        }
        return solvedAllSoFar;
    }

    //Only true once per round
    public boolean reachedGoal() {
        boolean goal = false;
        Log.i(TAG, "PosNbr: " + posNbr);
        if (trackState != TRACKSTATE.REACHEDGOAL) {
            if (posNbr == (track.size() - 1) && getCurrentPoint().getAssignment().getState() == Assignment.STATE.SUCCESFULL) {
                trackState = TRACKSTATE.REACHEDGOAL;
                goal = true;
            }
        }
        return goal;
    }

    public void resetTrack(){
        for (Point p: track) {
            p.getAssignment().setState(Assignment.STATE.NOT_STARTED);
            posNbr = 0;
        }
        trackState = TRACKSTATE.NOT_STARTED;
    }



    //Different testtracks
    public void setTestTrackLinero(){
        Assignment a = new Assignment("Vilken datum är Maja född","9:e december","6:e december", "3: december",1);
        track.add(new Point(new LatLng(55.699359, 13.236833), true, 0,a));
        //track.add(new Point(new LatLng(55.69951316 , 13.23616105), true, 0,a));
        a = new Assignment("Vilket nummer på Linerovägen bor vi på? ","36", "39", "35",2);
        Point p = new Point(new LatLng(55.699321, 13.237417), false, 1,a);
        track.add(p);
        a = new Assignment("Vilket datum är Alve född? ","30 Oktober", "28 September", "30 September",2);
        track.add(new Point(new LatLng(55.698854, 13.237559), false, 2,a));
        a = new Assignment("Hur många gitarrer har Hugo? ","3", "4", "5",0);
        track.add(new Point(new LatLng(55.698409, 13.237695), false, 3,a));
        a = new Assignment("VAd har vi gör postnummer på Linerovägen? ","224 70", "224 75", "220 35",1);
        track.add(new Point(new LatLng(55.698041, 13.237785), false, 4,a));
        Collections.sort(track);
    }

    public void setTestTrackLineroSmall(){
        Assignment a = new Assignment("Vilken datum är Maja född","9:e december","6:e december", "3: december",1);
        track.add(new Point(new LatLng(55.69951316 , 13.23616105), true, 0,a));
        a = new Assignment("Vilket nummer på Linerovägen bor vi på? ","36", "39", "35",2);
        Point p = new Point(new LatLng(55.699663, 13.235952), false, 1,a);
        track.add(p);
        a = new Assignment("Vilket datum är Alve född? ","30 Oktober", "28 September", "30 September",2);
        track.add(new Point(new LatLng(55.69951316 , 13.23616105), false, 2,a));
        a = new Assignment("Hur många gitarrer har Hugo? ","3", "4", "5",0);
        track.add(new Point(new LatLng(55.699663, 13.235952), false, 3,a));
        a = new Assignment("Vad har vi gör postnummer på Linerovägen? ","224 70", "224 75", "220 35",1);
        track.add(new Point(new LatLng(55.69951316 , 13.23616105), true, 4,a));
        Collections.sort(track);
    }
    public void setTestTrackNiagara(){
        track.add(new Point(new LatLng(55.60860821253092, 12.995177283883095), false, 3,new Assignment()));
        track.add(new Point(new LatLng(55.60892087499589 , 12.995085082948208), false, 2,new Assignment()));
        track.add(new Point(new LatLng(55.60875725303561 , 12.993938773870468), false, 1,new Assignment()));
        track.add(new Point(new LatLng(55.6086449519201  , 12.994105070829391 ), true, 0,new Assignment()));
        Collections.sort(track);
    }

    public void setTestTrackHumlamaden() {
        track.add(new Point(new LatLng(55.62248283737165, 13.51994913071394), false, 0,new Assignment())); //bil
        track.add(new Point(new LatLng(55.62183898407918, 13.520694114267826), false, 1, new Assignment())); //annat
        Collections.sort(track);
    }
}
