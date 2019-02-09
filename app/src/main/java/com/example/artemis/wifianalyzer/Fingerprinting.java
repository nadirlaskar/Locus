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
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Fingerprinting extends AppCompatActivity {

    Snackbar status;
    Timer P;
    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fingerprinting);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        wifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(status==null || !status.isShown()){
                    status = showSnackbarWithProgressBar(Fingerprinting.this,view);
                    ((FloatingActionButton) view).setImageDrawable(ContextCompat.getDrawable(Fingerprinting.this,R.drawable.stop));
                    status.show();
                }
                else {
                    resetProgressState();
                }
            }
        });
          ActionBar ab = getSupportActionBar();
          if(ab!=null)
                ab.setDisplayHomeAsUpEnabled(true);

        addSignalItems();
        addSpots();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        getApplicationContext().registerReceiver(wifiScanReceiver, intentFilter);
        if(!wifiManager.isWifiEnabled()){
            wifiManager.setWifiEnabled(true);
        }
    }

    private void addSpots() {
        List<String> spinnerArray = new ArrayList<>();
        spinnerArray.add("ODC-9-Corridor");
        spinnerArray.add("ODC-9-Center");
        spinnerArray.add("Gemini");
        spinnerArray.add("6th Floor Washroom Men's");
        spinnerArray.add("6th Floor Washroom Women's");
        spinnerArray.add("6th Floor Lift Lobby");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, R.layout.spinner_spot_list, spinnerArray);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner sItems = (Spinner) findViewById(R.id.spinner);
        if(sItems!=null)
         sItems.setAdapter(adapter);
    }

    Snackbar showSnackbarWithProgressBar(Context context,View view){
        Snackbar bar = Snackbar.make(view, "Capturing fingerprints...", Snackbar.LENGTH_INDEFINITE);
        Snackbar.SnackbarLayout snack_view = (Snackbar.SnackbarLayout) bar.getView();
        ProgressBar pbar =  new ProgressBar(context,null,android.R.attr.progressBarStyleHorizontal);
        Resources res = getResources();
        Drawable drawable = res.getDrawable(R.drawable.circular_progress);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        pbar.setLayoutParams(params );
        pbar.setProgress(0);   // Main Progress
        pbar.setSecondaryProgress(100); // Secondary Progress
        pbar.setMax(100); // Maximum Progress
        pbar.setProgressDrawable(drawable);
        updateProgressBar(pbar);
        snack_view.addView(pbar);
        return bar;
    }


    void updateProgressBar(final ProgressBar pb){
        P = new Timer();
        final int[] i = {0};
        P.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        pb.setProgress(i[0]++);
                        if(i[0]>=100){
                            resetProgressState();
                        }
                    }
                });
            }
        }, 0, 100);
    }

    private void resetProgressState() {
        P.cancel();
        status.dismiss();
        P.purge();
        fab.setImageDrawable(ContextCompat.getDrawable(Fingerprinting.this,R.drawable.record));
    }

    Timer T,T1;
    boolean newDataAvailable = true;
    WifiManager wifiManager = null;
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
                        Collections.sort(scanResults, new Fingerprinting.SignalComparator());

                        signals.clear();
                        for (ScanResult sr:scanResults) {
                            dbm = sr.level;
                            String SSID = sr.SSID +" - "+ sr.BSSID.substring(12);
                            if((SSID.startsWith("S1_GUEST")||true)){
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
                        if(signalListView!=null)
                          signalListView.setAdapter(new SignalAdapter(Fingerprinting.this, signals));
                    }
                });
            }
        }, 0, readData);

    }

}
