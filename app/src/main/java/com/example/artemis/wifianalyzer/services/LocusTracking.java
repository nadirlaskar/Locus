package com.example.artemis.wifianalyzer.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.support.design.widget.Snackbar;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.example.artemis.wifianalyzer.FingerprintAdapter;
import com.example.artemis.wifianalyzer.FingerprintListModel;
import com.example.artemis.wifianalyzer.FingerprintingActivity;
import com.example.artemis.wifianalyzer.MainActivity;
import com.example.artemis.wifianalyzer.R;
import com.example.artemis.wifianalyzer.api.ApiResponse;
import com.example.artemis.wifianalyzer.api.FingerprintController;
import com.example.artemis.wifianalyzer.api.TrackController;
import com.example.artemis.wifianalyzer.model.AccessPoint;
import com.example.artemis.wifianalyzer.model.Fingerprint;
import com.example.artemis.wifianalyzer.model.Spot;
import com.example.artemis.wifianalyzer.util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class LocusTracking extends Service {
    private TrackController trackController;

    public LocusTracking() {
    }

    private static final String LOG_TAG = "ForegroundService";
    public static boolean IS_SERVICE_RUNNING = false;
    Timer T, T1;
    boolean newDataAvailable = true;
    WifiManager wifiManager = null;

    BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent intent) {
            newDataAvailable = true;
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        getApplicationContext().registerReceiver(wifiScanReceiver, intentFilter);
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().equals(Constants.ACTION.STARTFOREGROUND_ACTION)) {
            Log.i(LOG_TAG, "Received Start Foreground Intent ");
            startFingerprinting();
            showNotification();
            Toast.makeText(this, "Locus is tracking your location", Toast.LENGTH_SHORT).show();

        } else if (intent.getAction().equals(
                Constants.ACTION.STOPFOREGROUND_ACTION)) {
            Log.i(LOG_TAG, "Received Stop Foreground Intent");
            stopFingerprinting();
            stopForeground(true);
            stopSelf();
        }
        return START_STICKY;
    }

    private void stopFingerprinting() {
        T.cancel();
        T.purge();
        T1.cancel();
        T1.purge();
    }

    private void showNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        Bitmap icon = BitmapFactory.decodeResource(getResources(),
                R.drawable.ic_info);

        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("Locus tracking enabled")
                .setTicker("You are sharing your location")
                .setContentText("Active")
                .setSmallIcon(R.drawable.icon_info)
                .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                .setContentIntent(pendingIntent)
                .setOngoing(true).build();
        startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE,
                notification);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(LOG_TAG, "In onDestroy");
        Toast.makeText(this, "Locus stopped tracking", Toast.LENGTH_SHORT).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Used only in case if services are bound (Bound Services).
        return null;
    }

    private void startFingerprinting() {
        final long msec = 30 * 1000;

        T = new Timer();
        T1 = new Timer();
        wifiManager.startScan();

        T.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                wifiManager.startScan(); //enable for force scan
                newDataAvailable = false;
            }
        }, 0, msec);

        final long readData = 1000;
        T1.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {

                if (!newDataAvailable) return;
                newDataAvailable = false;

                final Fingerprint fingerprint = new Fingerprint("",new ArrayList<AccessPoint>(), Util.getMacAddr());

                int img = 0, dbm, level;
                final List<ScanResult> scanResults = wifiManager.getScanResults();
                for (ScanResult sr : scanResults) {
                    String SSID = sr.SSID;
                    if ((SSID.startsWith("S1_Employee"))) {
                        fingerprint.addAccessPoint(new AccessPoint(sr.BSSID,sr.level));
                    }
                }

                submitFingerprint(fingerprint);

            }
        }, 0, readData);

    }


    private void submitFingerprint(Fingerprint fp) {
        trackController = new TrackController(new ApiResponse<Object>() {
            @Override
            public void loading() {
            }

            @Override
            public void success(Object response) {
            }

            @Override
            public void failure(String error) {

            }
        });

        trackController.start(fp);

    }

}
