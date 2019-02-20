package com.example.artemis.wifianalyzer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;

import com.example.artemis.wifianalyzer.api.ApiResponse;
import com.example.artemis.wifianalyzer.api.FingerprintController;
import com.example.artemis.wifianalyzer.api.SpotController;
import com.example.artemis.wifianalyzer.model.AccessPoint;
import com.example.artemis.wifianalyzer.model.Fingerprint;
import com.example.artemis.wifianalyzer.model.Spot;
import com.example.artemis.wifianalyzer.util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class FingerprintingActivity extends AppCompatActivity {

    Snackbar status, upload, info;
    Timer P;
    FloatingActionButton fab;
    HashMap<String, ArrayList<AccessPoint>> bssidFingerprintMap;
    HashMap<String, Fingerprint> spotFingerprintMap;
    Spot selectedSpot;
    Timer T, T1;
    boolean newDataAvailable = true;
    WifiManager wifiManager = null;
    ListView fingerprintList;

    SpotController spotController;
    FingerprintController fingerprintController;

    public class SignalComparator implements Comparator<ScanResult>

    {
        public int compare(ScanResult left, ScanResult right) {
            return right.level - left.level;
        }
    }

    public class AccesspointComparator implements Comparator<AccessPoint>

    {
        public int compare(AccessPoint left, AccessPoint right) {
            return right.getRss() - left.getRss();
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
        setContentView(R.layout.activity_fingerprinting);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fingerprintList = (ListView) findViewById(R.id.list);
        bssidFingerprintMap = new HashMap<>();
        spotFingerprintMap = new HashMap<>();
        upload = Snackbar.make(fab, "Captured fingerprint.", Snackbar.LENGTH_INDEFINITE)
                .setAction("Upload", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        submitFingerprint();
                    }
                });
        fab.setVisibility(View.GONE);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (status == null || !status.isShown()) {
                    status = showSnackWithProgressBar(FingerprintingActivity.this, view);
                    ((FloatingActionButton) view).setImageDrawable(ContextCompat.getDrawable(FingerprintingActivity.this, R.drawable.stop));
                    upload.dismiss();
                    status.show();
                    startFingerprinting();
                } else {
                    resetProgressState();
                }
            }
        });

        ActionBar ab = getSupportActionBar();
        if (ab != null)
            ab.setDisplayHomeAsUpEnabled(true);

        getSpots();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        getApplicationContext().registerReceiver(wifiScanReceiver, intentFilter);
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
    }

    private void submitFingerprint() {
        fingerprintController = new FingerprintController(new ApiResponse<Object>() {
            @Override
            public void loading() {
                info = Snackbar.make(fab, "Submitting fingerprint", Snackbar.LENGTH_INDEFINITE);
                info.show();
            }

            @Override
            public void success(Object response) {
                spotFingerprintMap.remove(selectedSpot.getId());
                bssidFingerprintMap = new HashMap<>();
                info = Snackbar.make(fab, "FingerprintListModel submitted successfully", Snackbar.LENGTH_LONG);
                info.show();
            }

            @Override
            public void failure(String error) {
                info = Snackbar.make(fab, "Unable to submit fingerprint", Snackbar.LENGTH_LONG);
                info.show();
                info.setCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar snackbar, int event) {
                        super.onDismissed(snackbar, event);
                        upload.show();
                    }
                });
            }
        });

        fingerprintController.start(spotFingerprintMap.get(selectedSpot.getId()));

    }

    private void getSpots() {

        spotController = new SpotController(new ApiResponse<List<com.example.artemis.wifianalyzer.model.Spot>>() {
            @Override
            public void success(final List<com.example.artemis.wifianalyzer.model.Spot> response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        info = Snackbar.make(fab, "Spots loaded.", Snackbar.LENGTH_LONG);
                        info.show();
                        addSpots(response);
                    }
                });
            }

            @Override
            public void failure(String error) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        info = Snackbar.make(fab, "Spot fetch failed loading from cache..", Snackbar.LENGTH_LONG);
                        info.show();
                        addSpots(null);
                    }
                });
            }

            @Override
            public void loading() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        info = Snackbar.make(fab, "Loading spots..", Snackbar.LENGTH_INDEFINITE);
                        info.show();
                    }
                });
            }
        });


        new Thread(new Runnable() {
            @Override
            public void run() {
                spotController.start();
            }
        }).start();

    }

    private void addSpots(List<com.example.artemis.wifianalyzer.model.Spot> spots) {

        if (spots == null) {
            spots = new ArrayList<>();
            float[] pos = {0, 0};
            spots.add(new Spot("-1", "ODC-9-Corridor"));
            spots.add(new Spot("-1", "ODC-9-Center"));
            spots.add(new Spot("-1", "Gemini"));
            spots.add(new Spot("-1", "6th Floor Washroom Men's"));
            spots.add(new Spot("-1", "6th Floor Washroom Women's"));
            spots.add(new Spot("-1", "6th Floor Lift Lobby"));
        }

        ArrayAdapter<Spot> adapter = new ArrayAdapter<>(
                this, R.layout.spinner_spot_list, spots);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        final Spinner sItems = (Spinner) findViewById(R.id.spinner);

        if (sItems != null) {
            final List<Spot> finalSpots = spots;
            sItems.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    selectedSpot = finalSpots.get(i);
                    bssidFingerprintMap = new HashMap<>();
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
            sItems.setAdapter(adapter);
            if(spots.size()>0)
                selectedSpot = spots.get(0);
            else selectedSpot = null;

            fab.setVisibility(View.VISIBLE);
        }

    }

    Snackbar showSnackWithProgressBar(Context context, View view) {
        Snackbar bar = Snackbar.make(view, "           Capturing fingerprints...", Snackbar.LENGTH_INDEFINITE);
        Snackbar.SnackbarLayout snack_view = (Snackbar.SnackbarLayout) bar.getView();
        ProgressBar pbar = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
        Resources res = getResources();
        Drawable drawable = res.getDrawable(R.drawable.circular_progress);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        pbar.setLayoutParams(params);
        pbar.setProgress(0);   // Main Progress
        pbar.setSecondaryProgress(100); // Secondary Progress
        pbar.setMax(100); // Maximum Progress
        pbar.setProgressDrawable(drawable);
        updateProgressBar(pbar);
        snack_view.addView(pbar);
        return bar;
    }

    void updateProgressBar(final ProgressBar pb) {
        P = new Timer();
        final int[] i = {0};
        P.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pb.setProgress(i[0]++);
                        if (i[0] >= 100) {
                            resetProgressState();
                        }
                    }
                });
            }
        }, 0, 300);
    }

    private void resetProgressState() {
        P.cancel();
        status.dismiss();
        P.purge();
        fab.setImageDrawable(ContextCompat.getDrawable(FingerprintingActivity.this, R.drawable.record));
        stopFingerPrinting();
        upload.show();
    }

    private void stopFingerPrinting() {
        T.cancel();
        T.purge();
        T1.cancel();
        T1.purge();

        final Fingerprint fingerprint = new Fingerprint(selectedSpot.getId(), new ArrayList<AccessPoint>(), Util.getMacAddr());
        int img = 0, dbm, level;
        final List<AccessPoint> fingerPrintAggregate = new ArrayList<>();

        for (Map.Entry<String, ArrayList<AccessPoint>> fp : bssidFingerprintMap.entrySet()) {
            int aggregateLevel = 0;
            ArrayList<AccessPoint> AccesspointArrayList = fp.getValue();
            for (AccessPoint f : AccesspointArrayList) {
                aggregateLevel += f.getRss();
            }
            fingerPrintAggregate.add(new AccessPoint(AccesspointArrayList.get(0).getSsid(),fp.getKey(),((aggregateLevel / AccesspointArrayList.size()))));
        }

        Collections.sort(fingerPrintAggregate, new AccesspointComparator());

        for (AccessPoint fp : fingerPrintAggregate) {
            dbm = fp.getRss();
            fingerprint.addAccessPoint(fp);
        }

        if (fingerprintList != null) {
            spotFingerprintMap.put(selectedSpot.getId(), fingerprint);
        }

    }

    private void startFingerprinting() {

        final ArrayList<FingerprintListModel> fingerprintListModels = new ArrayList<>();

        final long msec = 30 * 1000;

        T = new Timer();
        T1 = new Timer();
        wifiManager.startScan();
        T.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        wifiManager.startScan(); //enable for force scan
                        newDataAvailable = false;
                    }
                });
            }
        }, 0, msec);

        final long readData = 1000;
        T1.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (!newDataAvailable) return;
                        newDataAvailable = false;

                        int img = 0, dbm, level;
                        final List<ScanResult> scanResults = wifiManager.getScanResults();
                        Collections.sort(scanResults, new FingerprintingActivity.SignalComparator());

                        fingerprintListModels.clear();
                        for (ScanResult sr : scanResults) {
                            dbm = sr.level;
                            String SSID = sr.SSID + " - " + sr.BSSID.substring(12);
                            if ((true)) { //filter ssid if required
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

                                fingerprintListModels.add(new FingerprintListModel(SSID, dbm + " dBm", img));

                                if (status != null && status.isShown()) {
                                    ArrayList<AccessPoint> aFP = bssidFingerprintMap.get(sr.BSSID);
                                    if (aFP == null) {
                                        aFP = new ArrayList<>();
                                        aFP.add(new AccessPoint(sr.SSID,sr.BSSID, dbm));
                                        bssidFingerprintMap.put(sr.BSSID, aFP);
                                    } else {
                                        aFP.add(new AccessPoint(sr.SSID,sr.BSSID,dbm));
                                    }
                                }

                            }
                        }
                        if (fingerprintList != null)
                            fingerprintList.setAdapter(new FingerprintAdapter(FingerprintingActivity.this, fingerprintListModels));
                    }
                });
            }
        }, 0, readData);

    }

}
