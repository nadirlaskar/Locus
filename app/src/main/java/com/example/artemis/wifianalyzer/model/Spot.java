package com.example.artemis.wifianalyzer.model;

public class Spot {
    private String _id;
    private String name;

    public Spot(String id, String name) {
        this._id = id;
        this.name = name;
    }

    public String getId() {
        return _id;
    }

    public void setId(String id) {
        this._id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
