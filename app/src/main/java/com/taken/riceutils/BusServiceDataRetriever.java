package com.taken.riceutils;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Karin on 1/16/2016.
 */
public class BusServiceDataRetriever extends AsyncTask<String, Void, String> {

    private InputStream inputStream = null;
    private ConcurrentHashMap<Integer, String[]> trackedStops;
    private BusNotificationService service;

    public BusServiceDataRetriever(BusNotificationService service, ConcurrentHashMap<Integer, String[]> trackedStops){
        this.service = service;
        this.trackedStops = trackedStops;
    }

    @Override
    protected String doInBackground(String... url){
        try{
            URL busUrl = new URL(url[0]);
            HttpURLConnection connection = (HttpURLConnection) busUrl.openConnection();
            connection.setRequestMethod("POST");
            inputStream = new BufferedInputStream(connection.getInputStream());
        }catch (Exception e){
            Log.e("e", e.toString());
        }

        String result = "";

        try{
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sBuilder = new StringBuilder();

            String line;
            while ((line = br.readLine()) != null) {
                sBuilder.append(line).append("\n");
            }

            inputStream.close();
            result = sBuilder.toString();
        }catch(Exception e){
            Log.e("e",e.toString());
        }
        return result;
    }

    @Override
    protected void onPostExecute(String res) {
        super.onPostExecute(res);

        // holds type and location
        HashMap<String, LatLng> buses = new HashMap<>();

        try{
            JSONObject obj = new JSONObject(res);
            JSONArray jArray = obj.getJSONArray("d");

            for (int i = 0; i < jArray.length(); i++) {
                JSONObject bus = jArray.getJSONObject(i);
                LatLng pos = new LatLng(bus.getDouble("Latitude"), bus.getDouble("Longitude"));
                buses.put(bus.getString("Name"), pos);

            }
        } catch (Exception e) {
            Log.e("e",e.toString());
        }

        //find any/all buses that are in any/all target zones
        for (Map.Entry<Integer, String[]> entry : trackedStops.entrySet()) {
            for (Map.Entry<String, LatLng> bus : buses.entrySet()) {
                LatLng checkLocation = BuildingMap.busRoutes.get(entry.getValue()[0]).get(entry.getValue()[1]);
                if (bus.getKey().equals(entry.getValue()[0]) &&
                        Math.abs(checkLocation.latitude - bus.getValue().latitude) <= 0.00013 &&
                        Math.abs(checkLocation.longitude - bus.getValue().longitude) <= 0.00012) {
                    service.removeFromTrackedBuses(entry.getKey(), true);
                    break;
                }
            }
        }
    }

}
