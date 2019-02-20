package com.example.artemis.wifianalyzer.model;

import java.util.ArrayList;

public class Fingerprint {

    private String spotId;
    private ArrayList<AccessPoint> accessPoints;
    private String deviceMac;

    public ArrayList<AccessPoint> getAccessPoints() {
        return accessPoints;
    }

    public void setAccessPoints(ArrayList<AccessPoint> accessPoints) {
        this.accessPoints = accessPoints;
    }

    public Fingerprint(String spotId, ArrayList<AccessPoint> accessPoints, String deviceMac) {
        this.spotId = spotId;
        this.accessPoints = accessPoints;
        this.deviceMac = deviceMac;
    }

    public void addAccessPoint(AccessPoint ap){
        this.accessPoints.add(ap);
    }


    public String getSpotId() {
        return spotId;
    }

    public void setSpotId(String spotId) {
        this.spotId = spotId;
    }

    public String getDeviceMac() {
        return deviceMac;
    }

    public void setDeviceMac(String deviceMac) {
        this.deviceMac = deviceMac;
    }
}
