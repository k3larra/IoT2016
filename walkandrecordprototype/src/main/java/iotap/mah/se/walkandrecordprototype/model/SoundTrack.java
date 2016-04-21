package iotap.mah.se.walkandrecordprototype.model;

/**
 * Created by K3LARA on 2016-04-21.
 */
public class SoundTrack {
    private String name;
    private String author;
    private String rating;
    private String type;
    private String language;

    public SoundTrack(String name, String author,String languange) {
        this.name = name;
        this.author = author;
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
}
