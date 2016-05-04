package com.example;

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



    private ArrayList<Point> points;

    public SoundTrack() {
    }

    public SoundTrack(String name, String author) {
        this.name = name;
        this.author = author;
    }

    public SoundTrack(String name, String author, String language) {
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
}
