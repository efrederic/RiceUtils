package com.taken.riceutils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Nathan on 1/16/2016.
 */
public class Events extends AsyncTask<String, Void, String> {

    private InputStream inputStream = null;

    @Override
    protected String doInBackground(String... url){
        String data = null;
        try{
            URL eventsURL = new URL(url[0]);
            HttpURLConnection connection = (HttpURLConnection) eventsURL.openConnection();
            connection.setRequestMethod("POST");
            inputStream = new BufferedInputStream(connection.getInputStream());
        }catch (Exception e){
            //Log.e("e",e.toString());
        }

        try{
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sBuilder = new StringBuilder();

            String line;
            while ((line = br.readLine()) != null) {
                sBuilder.append(line).append("\n");
            }

            inputStream.close();

            data = sBuilder.toString();

        }catch(Exception e){
            //Log.e("e",e.toString());
        }
        return data;
    }

}
