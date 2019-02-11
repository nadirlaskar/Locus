package com.example.artemis.wifianalyzer;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Artemis on 30-Mar-17.
 */

public class SignalAdapter extends ArrayAdapter {

    public SignalAdapter(Activity context, ArrayList<Signal> signals) {
        super(context,0,signals);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Check if the existing view is being reused, otherwise inflate the view
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.strength_list_item, parent, false);
        }

        // Get the {@link AndroidFlavor} object located at this position in the list
        Signal currentSignal = (Signal) getItem(position);

        ImageView barsView = (ImageView)listItemView.findViewById(R.id.bars);
        barsView.setImageResource(currentSignal.getBars());

        TextView strengthView = (TextView)listItemView.findViewById(R.id.strength);
        strengthView.setText(currentSignal.getDbm());

        TextView ssidView = (TextView)listItemView.findViewById(R.id.ssid);
        ssidView.setText(currentSignal.getSSID());

        return listItemView;
    }
}
