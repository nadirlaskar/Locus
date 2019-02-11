package com.example.artemis.wifianalyzer.model;

public class AccessPoint {

    private String apMac;
    private int rss;

    public AccessPoint(String apMac, int rss) {
        this.apMac = apMac;
        this.rss = rss;
    }

    public String getApMac() {
        return apMac;
    }

    public void setApMac(String apMac) {
        this.apMac = apMac;
    }

    public int getRss() {
        return rss;
    }

    public void setRss(int rss) {
        this.rss = rss;
    }
}
