package com.example.artemis.wifianalyzer;

/**
 * Created by Artemis on 30-Mar-17.
 */

public class Signal {

    private int bars;
    private String dbm;
    private String ssid;

    Signal(int b, String d, String t) {
        bars = b;
        dbm = t;
        ssid = d;
    }

    public int getBars() {
        return bars;
    }

    public String getDbm() {
        return dbm;
    }

    public int getDbmInt(){ return  Integer.parseInt(dbm.split(" ")[0]); }

    public String getSSID() {
        return ssid;
    }
}
