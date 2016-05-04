package com.example;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Timer;

import javax.xml.crypto.Data;

public class MyClass implements Runnable{
    public static Firebase myFirebaseRef;
    public static ArrayList<SoundTrack> soundTracks;
    static BufferedReader in ;
    static boolean quit=false;

    public static void main(String[] args) throws Exception{
        in=new BufferedReader(new InputStreamReader(System.in));

        // creating a new thread to handle the input
        Thread t1=new Thread(new MyClass());
        t1.start();
        System.out.println("press Q THEN ENTER to terminate");


        soundTracks = new ArrayList<SoundTrack>();
        myFirebaseRef = new Firebase("https://soundtrackmalmo.firebaseio.com//");
        System.out.println("Start finding them");
        myFirebaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    SoundTrack track = postSnapshot.getValue(SoundTrack.class);
                    track.setId(postSnapshot.getKey());
                    soundTracks.add(track);

                }
                System.out.println("Got: "+soundTracks.size());
                for (SoundTrack s:soundTracks){
                    s.setPoints(getTrack(s.getId()));
                }

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
        /*while(true) {
            Thread.sleep(1000); //Sleep 10 seconds
        }*/
    }

    private static ArrayList<Point> getTrack(String id) {

            Firebase trackref = myFirebaseRef.child(id).child("SoundTrack");
            final ArrayList<Point> points = new ArrayList<Point>();
            trackref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot snap: dataSnapshot.getChildren()){
                        Point p = snap.getValue(Point.class);
                        points.add(p);
                    }
                    System.out.println("GotIt");
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });
            return points;

    }


    private static void printIt() {
        PrintWriter writer = null;

        for (SoundTrack st:soundTracks){
            try {
                writer = new PrintWriter("C:\\Users\\k3lara\\Documents\\StudioProjects\\IoT2016\\createkml\\kmls\\"+st.getName()+".kml", "UTF-8");
                writer.println(Constants.startCode2);
                writer.println("<coordinates>");
                for (Point p:st.getPoints()) {
                    writer.println(p.getLongitude()+","+p.getLatitude()+",2357");
                }
                writer.println("</coordinates>");
                writer.println(Constants.endCode2);
                writer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        };



    }

    @Override
    public void run() {
        String msg = null;

        // threading is waiting for the key Q to be pressed
        while(true){
            try{
                System.out.println("Reading");
                msg=in.readLine();
            }catch(IOException e){
                e.printStackTrace();
            }

            if(msg.equals("q")) {
                System.out.println("Quitting");
                System.exit(0);
                quit=true;
                break;
            }

            if(msg.equals("p")) {
                System.out.println("Printing");
                printIt();
            }
        }
    }
}
