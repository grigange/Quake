package com.example.myapplication.model;


public class Feature {
    private Properties properties;
    private Geometry geometry;

    public Feature(Properties properties, Geometry geometry) {
        this.properties = properties;
        this.geometry = geometry;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }


}
