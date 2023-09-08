package com.example.myapplication.model;

import com.google.android.gms.maps.model.LatLng;

public class EarthquakeModel {
    private String name;
    private LatLng latlong;
    private double richter;

    public EarthquakeModel(String name, LatLng latlong, double richter) {
        this.name = name == null?"Earthquake":name;
        this.latlong = latlong;
        this.richter = richter;
    }

    public LatLng getLatlong() {
        return latlong;
    }

    public EarthquakeModel(LatLng latlong, double richter) {
        this.name = "Earthquake";
        this.latlong = latlong;
        this.richter = richter;
    }

    public void setLatlong(LatLng latlong) {
        this.latlong = latlong;
    }

    public double getRichter() {
        return richter;
    }

    public void setRichter(double richter) {
        this.richter = richter;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
