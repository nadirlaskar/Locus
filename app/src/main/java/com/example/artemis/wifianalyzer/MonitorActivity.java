package com.example.artemis.wifianalyzer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MonitorActivity extends AppCompatActivity {

    Timer T,T1;
    boolean newDataAvailable = true;
    WifiManager  wifiManager = null;
    public class SignalComparator implements Comparator<ScanResult>

    {
        public int compare(ScanResult left, ScanResult right) {
            return right.level - left.level;
        }
    }

    BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent intent) {
                newDataAvailable = true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        wifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Snackbar.make(view, ("Automatic scan is "+(wifiManager.isScanAlwaysAvailable()?"enabled":"disabled")), Snackbar.LENGTH_LONG)
                        .setAction("Scan Now", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                wifiManager.startScan();
                            }
                        }).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        addSignalItems();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        getApplicationContext().registerReceiver(wifiScanReceiver, intentFilter);
        if(!wifiManager.isWifiEnabled()){
            wifiManager.setWifiEnabled(true);
        }
    }

    private void addSignalItems() {

        final ArrayList<Signal> signals = new ArrayList<>();

        final ListView signalListView = (ListView) findViewById(R.id.list);

        final long msec = 30*1000;

        T = new Timer();
        T1 = new Timer();
        T.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        wifiManager.startScan(); //enable for force scan
                        newDataAvailable = false;
                    }
                });
            }
        }, 0, msec);

        final long readData = 1000;
        T.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {

                        if(!newDataAvailable) return;
                        newDataAvailable = false;

                        int img=0, dbm, level;
                        final List<ScanResult> scanResults = wifiManager.getScanResults();
                        Collections.sort(scanResults, new SignalComparator());

                        signals.clear();
                        for (ScanResult sr:scanResults) {
                            dbm = sr.level;
                            String SSID = sr.SSID +" - "+ sr.BSSID.substring(12);
                            if((SSID.startsWith("S1_Employee"))){
                                level = WifiManager.calculateSignalLevel(dbm, 5);
                                switch (level) {
                                    case 0:
                                        img = R.drawable.icon_wifi0;
                                        break;
                                    case 1:
                                        img = R.drawable.icon_wifi1;
                                        break;
                                    case 2:
                                        img = R.drawable.icon_wifi2;
                                        break;
                                    case 3:
                                        img = R.drawable.icon_wifi3;
                                        break;
                                    case 4:
                                        img = R.drawable.icon_wifi;
                                        break;
                                }
                                signals.add(new Signal(img, SSID,dbm+" dBm"));
                            }
                        }

                        signalListView.setAdapter(new SignalAdapter(MonitorActivity.this, signals));
                    }
                });
            }
        }, 0, readData);

    }
}
