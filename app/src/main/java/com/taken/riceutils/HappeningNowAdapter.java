package com.taken.riceutils;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Nathan on 1/16/2016.
 */
public class HappeningNowAdapter extends BaseAdapter {

    private Activity activity;
    private ArrayList<HashMap<String, String>> data;
    private static LayoutInflater inflater = null;

    public HappeningNowAdapter(Activity a, ArrayList<HashMap<String, String>> d) {
        activity = a;
        data = d;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return data.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        if(convertView==null) {
            vi = inflater.inflate(R.layout.list_row, null);
        }

        TextView title = (TextView)vi.findViewById(R.id.title); // title
        TextView time = (TextView)vi.findViewById(R.id.time);
        TextView location = (TextView)vi.findViewById(R.id.location);

        HashMap<String, String> item = new HashMap<String, String>();
        item = data.get(position);

        // Setting all values in listview
        title.setText(item.get(HappeningNow.KEY_TITLE));
        time.setText(item.get(HappeningNow.KEY_TIME));
        location.setText(item.get(HappeningNow.KEY_LOCATION));

        return vi;
    }

}
