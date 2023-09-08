package com.example.myapplication.model;

import java.util.ArrayList;

public class UsgsModel {
    private ArrayList<Feature> features;

    public UsgsModel(ArrayList<Feature> features) {
        this.features = features;
    }

    public ArrayList<Feature> getFeatures() {
        return features;
    }

    public void setFeatures(ArrayList<Feature> features) {
        this.features = features;
    }


}




