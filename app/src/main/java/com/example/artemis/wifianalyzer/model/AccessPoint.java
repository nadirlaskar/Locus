package com.example.artemis.wifianalyzer.model;

public class AccessPoint {

    private String apMac;

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    private String ssid;
    private int rss;

    public AccessPoint(String ssid, String apMac, int rss) {
        this.ssid = ssid;
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
