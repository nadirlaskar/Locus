package com.example.artemis.wifianalyzer;

/**
 * Created by Artemis on 30-Mar-17.
 */

public class Fingerprint {

    private String ssid;
    private String strength;
    private int res;

    Fingerprint(String SSID, String strength, int resId) {
        ssid = SSID;
        this.strength = strength;
        res = resId;
    }

    public String getSSID() {
        return ssid;
    }

    public String getStrength() {
        return strength;
    }

    public int getStrengthInt() {
        return Integer.parseInt(strength.split(" ")[0]);
    }

    public int getResId() {
        return res;
    }

}
