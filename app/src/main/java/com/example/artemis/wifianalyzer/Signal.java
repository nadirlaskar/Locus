package com.example.artemis.wifianalyzer;

/**
 * Created by Artemis on 30-Mar-17.
 */

public class Signal {

    private int bars;
    private String dbm;
    private String time;

    public Signal(int b, String d, String t) {
        bars = b;
        dbm = d;
        time = t;
    }

    public int getBars() {
        return bars;
    }

    public String getDbm() {
        return dbm;
    }

    public String getTime() {
        return time;
    }
}
