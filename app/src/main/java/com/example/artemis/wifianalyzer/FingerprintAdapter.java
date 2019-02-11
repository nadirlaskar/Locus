package com.example.artemis.wifianalyzer;

import android.app.Activity;
import android.support.annotation.NonNull;
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

public class FingerprintAdapter extends ArrayAdapter {

    FingerprintAdapter(Activity context, ArrayList<FingerprintListModel> properties) {
        super(context,0,properties);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Check if the existing view is being reused, otherwise inflate the view
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.strength_list_item, parent, false);
        }

        FingerprintListModel strengthItem = (FingerprintListModel) getItem(position);

        TextView ssid = (TextView)listItemView.findViewById(R.id.ssid);
        assert strengthItem != null;
        ssid.setText(strengthItem.getSSID());

        TextView strength = (TextView)listItemView.findViewById(R.id.strength);
        strength.setText(strengthItem.getStrength());

        ImageView barsView = (ImageView)listItemView.findViewById(R.id.bars);
        barsView.setImageResource(strengthItem.getResId());

        return listItemView;
    }
}
