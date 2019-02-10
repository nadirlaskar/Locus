package com.example.artemis.wifianalyzer;

/**
 * Created by Artemis on 30-Mar-17.
 */

public class Signal {

    private int bars;
    private String dbm;
    private String time;

    Signal(int b, String d, String t) {
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

    public int getDbmInt(){ return  Integer.parseInt(dbm.split(" ")[0]); }

    public String getTime() {
        return time;
    }
}
