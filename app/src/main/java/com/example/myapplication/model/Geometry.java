package com.example.myapplication.model;

import java.util.ArrayList;

public class Geometry{
    private ArrayList<Double> coordinates;

    public Geometry(ArrayList<Double> coordinates) {
        this.coordinates = coordinates;
    }

    public ArrayList<Double> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(ArrayList<Double> coordinates) {
        this.coordinates = coordinates;
    }
}
