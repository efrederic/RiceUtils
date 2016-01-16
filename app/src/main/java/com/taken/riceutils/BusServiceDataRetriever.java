package com.taken.riceutils;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Karin on 1/16/2016.
 */
public class BusServiceDataRetriever extends AsyncTask<String, Void, String> {

    private InputStream inputStream = null;
    private HashMap<Integer, String[]> trackedStops;
    private Context context;

    public BusServiceDataRetriever(Context context, HashMap<Integer, String[]> trackedStops){
        this.context = context;
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
        Log.d("BSDR","result: " + result);
        return result;
    }

    @Override
    protected void onPostExecute(String res) {
        super.onPostExecute(res);

        //find any/all buses that are in any/all target zones

        //remove any notifications

        BusNotificationService.removeFromTrackedBuses(context, 0);
    }

}
