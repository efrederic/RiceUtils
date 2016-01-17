package com.taken.riceutils;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by andrew on 1/17/16.
 */
public class GetShoutoutsTask extends AsyncTask<String, Void, String> {

    private GoogleMap mMap;

    public GetShoutoutsTask(GoogleMap map) {
        mMap = map;
    }

    @Override
    protected String doInBackground(String... urlStr) {
        String xmldata = "";
        try {
            URL url = new URL(urlStr[0]);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            try {
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                byte[] contents = new byte[1024];

                int bytesRead = 0;
                while ((bytesRead = in.read(contents)) != -1) {
                    xmldata += new String(contents, 0, bytesRead);
                }
                Log.d("RICEUTILS", xmldata);
            } finally {
                urlConnection.disconnect();
            }
        } catch (Exception e) {
            Log.e("RICEUTILS", e.toString());
        }
        return xmldata;
    }

    @Override
    protected void onPostExecute(String xmldata) {
        if (!xmldata.equals("")) {
            // parse xml, and do stuff on mMap...
            // clear pins here...
            // for now, we'll just hard-code stuff in like a boss
            String[] shoutoutData = {"Party over here","Party over there","Shoutout to pears","Shoutout to pears again","So many flavors"};
            String[] latitudes = {"29.713845", "29.714167", "29.715122", "29.715332", "29.716301"};
            String[] longitudes = {"-95.406353", "-95.406224", "-95.405237", "-95.404684", "-95.402195"};
            for (int i=0; i<latitudes.length; i++){
                LatLng latLng = new LatLng(Double.parseDouble(latitudes[i]), Double.parseDouble(longitudes[i]));
                mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(shoutoutData[i]))
                        .showInfoWindow();
            }
        }
    }
}
