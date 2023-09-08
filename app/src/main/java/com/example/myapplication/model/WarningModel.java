package com.example.myapplication.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;

public class WarningModel {
    private String id;
    private String title;
    private Timestamp time;
    private double mag;
    private GeoPoint myLocation;
    private GeoPoint eqLocation;

    public WarningModel(String id, String title, Timestamp time, double mag, GeoPoint myLocation, GeoPoint eqLocation) {
        this.id = id;
        this.title = title;
        this.time = time;
        this.mag = mag;
        this.myLocation = myLocation;
        this.eqLocation = eqLocation;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }

    public double getMag() {
        return mag;
    }

    public void setMag(double mag) {
        this.mag = mag;
    }

    public GeoPoint getMyLocation() {
        return myLocation;
    }

    public void setMyLocation(GeoPoint myLocation) {
        this.myLocation = myLocation;
    }

    public GeoPoint getEqLocation() {
        return eqLocation;
    }

    public void setEqLocation(GeoPoint eqLocation) {
        this.eqLocation = eqLocation;
    }
}
