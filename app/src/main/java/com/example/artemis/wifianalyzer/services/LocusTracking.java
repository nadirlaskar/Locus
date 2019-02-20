package com.example.artemis.wifianalyzer.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
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
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.example.artemis.wifianalyzer.MainActivity;
import com.example.artemis.wifianalyzer.R;
import com.example.artemis.wifianalyzer.api.ApiResponse;
import com.example.artemis.wifianalyzer.api.TrackController;
import com.example.artemis.wifianalyzer.api.TrackMeController;
import com.example.artemis.wifianalyzer.model.AccessPoint;
import com.example.artemis.wifianalyzer.model.Fingerprint;
import com.example.artemis.wifianalyzer.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.github.nisrulz.sensey.MovementDetector;
import com.github.nisrulz.sensey.Sensey;


public class LocusTracking extends Service {
    private TrackController trackController;
    private TrackMeController trackMeController;
    private MovementDetector.MovementListener movementListener;
    int movement = 0;
    private long lastScanTime = -1;
    private WifiManager.WifiLock wifiLock;

    public LocusTracking() {
    }

    private static final String LOG_TAG = "ForegroundService";
    public static boolean IS_SERVICE_RUNNING = false;
    Timer sendSpotPrintAtMax, readResultAndSend;
    boolean newDataAvailable = true;
    WifiManager wifiManager = null;

    BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent intent) {
            lastScanTime = System.currentTimeMillis();
            newDataAvailable = true;
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiLock = wifiManager.createWifiLock("LiveTracking");
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
            Sensey.getInstance().init(this);
            if(!wifiLock.isHeld())
                wifiLock.acquire();
            startFingerprinting();
            showNotification("Unknown");
            Toast.makeText(this, "Locus is tracking your location", Toast.LENGTH_SHORT).show();

        } else if (intent.getAction().equals(
                Constants.ACTION.STOPFOREGROUND_ACTION)) {
            Log.i(LOG_TAG, "Received Stop Foreground Intent");
            stopFingerprinting();
            if(wifiLock.isHeld())
                    wifiLock.release();
            stopForeground(true);
            stopSelf();
        }
        return START_STICKY;
    }

    private void stopFingerprinting() {
        sendSpotPrintAtMax.cancel();
        sendSpotPrintAtMax.purge();
        readResultAndSend.cancel();
        readResultAndSend.purge();
        Sensey.getInstance().stopMovementDetection(movementListener);
    }

    private NotificationManager notifManager;
    private Notification notification;
    private void showNotification(String Spot) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        Bitmap icon = BitmapFactory.decodeResource(getResources(),
                R.drawable.ic_info);

        if (notifManager == null) {
            notifManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        }

        String id = Constants.ACTION.LOCUS_ACTION;
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, id);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = notifManager.getNotificationChannel(id);
            if (mChannel == null) {
                mChannel = new NotificationChannel(id, "Locus", importance);
                mChannel.enableVibration(true);
                notifManager.createNotificationChannel(mChannel);
            }

            builder = builder.setContentTitle("Locus tracking enabled")
                    .setTicker("You are sharing your location")
                    .setContentText("Active near "+ Spot)
                    .setSmallIcon(R.drawable.icon_info)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                    .setContentIntent(pendingIntent)
                    .setOngoing(true);
        } else {
            builder = builder.setContentTitle("Locus tracking enabled")
                    .setTicker("You are sharing your location")
                    .setContentText("Active near "+ Spot)
                    .setSmallIcon(R.drawable.icon_info)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                    .setContentIntent(pendingIntent)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setOngoing(true);
        }
        notification = builder.build();
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
        final long maxScanCap = 10 * 1000;

        sendSpotPrintAtMax = new Timer();
        readResultAndSend = new Timer();

        // scan if motion detected
        movementListener = new MovementDetector.MovementListener() {
            @Override
            public void onMovement() {
                movement++;
                startScan();
            }

            @Override
            public void onStationary() {
                startScan();
                movement = 0;
            }
        };

        // Start sensing with threshold 200 and 10s stationary settings
        Sensey.getInstance().startMovementDetection(100,5000,movementListener);


        // schedule upload on idle
        sendSpotPrintAtMax.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                newDataAvailable = movement == 0; // if movement zero then send old data as new data
            }
        }, 0, maxScanCap);

        // read and send scan information if available
        final long readData = 1000;
        readResultAndSend.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                scanAndSubmit();
            }
        }, 0, readData);

        // Send initial scan
        startScan();

    }
    private boolean scheduled = false;
    private void startScan() {
        long currentScanTime = System.currentTimeMillis();
        long timeElapsed = (currentScanTime - lastScanTime);
        if (lastScanTime == -1){
            wifiManager.startScan();
        }
        else if (timeElapsed > (30 * 1000))
            wifiManager.startScan();
        else if(!scheduled) {
            // Request after 5s
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    wifiManager.startScan();
                    scheduled=false;
                }
            }, timeElapsed);

            scheduled=true;
        }
    }

    private void scanAndSubmit() {
        if (!newDataAvailable) return;
        newDataAvailable = false;

        final Fingerprint fingerprint = new Fingerprint("", new ArrayList<AccessPoint>(), Util.getMacAddr());

        int img = 0, dbm, level;
        final List<ScanResult> scanResults = wifiManager.getScanResults();
        for (ScanResult sr : scanResults) {
            String SSID = sr.SSID;
            if ((true)) { //filter ssid if required
                fingerprint.addAccessPoint(new AccessPoint(sr.SSID,sr.BSSID,sr.level));
            }
        }

        submitFingerprint(fingerprint);
    }


    private boolean loadingTrackMe=false;
    private String lastLocation = "";
    private void submitFingerprint(final Fingerprint fp) {
        trackController = new TrackController(new ApiResponse<Object>() {
            @Override
            public void loading() {
            }

            @Override
            public void success(Object response) {
                trackMeController = new TrackMeController(new ApiResponse<String>() {
                    @Override
                    public void loading() {
                    }

                    @Override
                    public void success(String response) {
                        if(!lastLocation.equals(response)) {
                            lastLocation = response;
                            showNotification(response);
                        }
                        loadingTrackMe=false;
                    }

                    @Override
                    public void failure(String error) {
                        loadingTrackMe=false;
                    }
                });

                if(!loadingTrackMe){
                    // Request after 5s
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            trackMeController.start(fp.getDeviceMac());
                        }
                    }, 5000);
                    loadingTrackMe=true;
                }

            }

            @Override
            public void failure(String error) {

            }
        });

        trackController.start(fp);

    }



}
