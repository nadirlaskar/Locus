package com.example.artemis.wifianalyzer.services;

public class Constants {


    public interface ACTION {
        public static String MAIN_ACTION = "com.example.artemis.wifianalyzer.main";
        public static String LOCUS_ACTION = "com.example.artemis.wifianalyzer.locus";
        public static String STARTFOREGROUND_ACTION = "com.example.artemis.wifianalyzer.startforeground";
        public static String STOPFOREGROUND_ACTION = "com.example.artemis.wifianalyzer.stopforeground";
    }

    public interface NOTIFICATION_ID {
        public static int FOREGROUND_SERVICE = 101;
    }
}

