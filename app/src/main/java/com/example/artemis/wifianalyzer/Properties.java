package com.example.artemis.wifianalyzer;

/**
 * Created by Artemis on 30-Mar-17.
 */

public class Properties {

    private String property;
    private String value;

    public Properties(String p, String v) {
        property = p;
        value = v;
    }

    public String getProperty() {
        return property;
    }

    public String getValue() {
        return value;
    }
}
