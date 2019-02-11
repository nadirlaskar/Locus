package com.example.artemis.wifianalyzer;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.TriggerEvent;
import android.hardware.TriggerEventListener;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.github.nisrulz.sensey.MovementDetector;
import com.github.nisrulz.sensey.Sensey;

public class ActivitySensor extends AppCompatActivity {
    TextView res;
    private MovementDetector.MovementListener movementListener;
    int movement = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);
        res =  findViewById(R.id.activity);
        Sensey.getInstance().init(this);
        startActivity();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Sensey.getInstance().startMovementDetection(movementListener);
    }

    void startActivity(){
        movementListener = new MovementDetector.MovementListener() {
            @Override public void onMovement() {
                movement++;
            }

            @Override public void onStationary() {
                if(movement>100){
                    res.setText(String.format("Moved %d", movement));
                }else{
                    res.setText(String.format("Failed Movement %d", movement));
                }

                movement = 0;
            }

        };
    }


    @Override
    protected void onPause() {
        super.onPause();
        Sensey.getInstance().stopMovementDetection(movementListener);
    }
}
