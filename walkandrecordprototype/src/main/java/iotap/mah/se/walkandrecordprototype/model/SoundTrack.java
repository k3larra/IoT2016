package iotap.mah.se.walkandrecordprototype.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;

/**
 * Created by K3LARA on 2016-04-21.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class SoundTrack {
    private String name;
    private String author;
    private String rating;
    private String type;
    private String language;
    private String id;
    private ArrayList<Point> points = new ArrayList<Point>();
    //
    private int activepoint=-1;
    private boolean hasWalkStarted=false;
    private boolean hasWalkEnded=false;

    public SoundTrack() {
    }

    public SoundTrack(String name, String author) {
        this.name = name;
        this.author = author;
    }

    public SoundTrack(String name, String author,String language) {
        this.name = name;
        this.author = author;
    }

    public SoundTrack(String name, String author, String rating, String type) {
        this.name = name;
        this.author = author;
        this.rating = rating;
        this.type = type;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return this.getName();
    }

    public ArrayList<Point> getPoints() {
        return points;
    }

    public void setPoints(ArrayList<Point> points) {
        this.points = points;
    }

    public Point getFirstPoint() {
        if (points != null) {
            return points.get(0);
        } else {
            return null;
        }
    }

    public Point getNextPoint() {
        if ((activepoint+1)<points.size()){
            return points.get(activepoint+1);
        }if (activepoint==-1){
            return points.get(0);
        }else{
            return null;
        }
    }

    public Point setNextPointActive() {
        if ((activepoint+1)<points.size()) {
            hasWalkStarted = true;
            activepoint = activepoint + 1;
            return points.get(activepoint);
        }else if(((activepoint+1)==points.size())){
            hasWalkEnded=true;
            return null;
        }else{
            return null;
        }
    }

    public boolean hasWalkStarted(){
        return this.hasWalkStarted;
    }

    public boolean hasWalkEnded(){
        return this.hasWalkEnded;
    }

    public Point getCurrentPoint() {
        if (points != null&&activepoint!=-1) {
                return points.get(activepoint);
        } else {
            return null;
        }
    }

    public Point getPrevoiusPoint() {
        if (points != null) {
            if ((activepoint)>0) {
                return points.get(activepoint - 1);
            }
            return points.get(activepoint-1);
        } else {
            return null;
        }
    }

    public boolean isLastPoint(){
        if ((activepoint+1)==points.size()) {
            return true;
        }else{
            return false;
        }
    }

    public boolean isTheFirstPoint(){
        if (activepoint==0) {
            return true;
        }else{
            return false;
        }
    }

    public int getTheActivePointIndex(){
        return activepoint;
    }
}
