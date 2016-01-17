package com.taken.riceutils;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Nathan on 1/16/2016.
 */
public class ShoutoutTask extends AsyncTask<String, Void, String> {

    private InputStream inputStream = null;
    private String text = "";
    private String lat = "";
    private String lng = "";

    public ShoutoutTask(String text, String lat, String lng){
        this.text = text;
        this.lat = lat;
        this.lng = lng;
    }

    @Override
    protected String doInBackground(String... url){
        InputStream inputStream = null;
        try{

            URL shoutoutURL = new URL(url[0]);
            HttpURLConnection connection = (HttpURLConnection) shoutoutURL.openConnection();
            connection.setRequestMethod("POST");
//            connection.setReadTimeout(10000);
//            connection.setConnectTimeout(15000);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            HashMap<String, String> params = new HashMap<>();
            params.put("text", text);
            params.put("latitude", lat);
            params.put("longitude", lng);

            DataOutputStream output = new DataOutputStream(connection.getOutputStream());
            output.writeBytes("test");
            output.flush();
            output.close();

//            OutputStream os = connection.getOutputStream();
//            BufferedWriter writer = new BufferedWriter(
//                    new OutputStreamWriter(os, "UTF-8"));
//            writer.write(getQuery(params));
//            writer.flush();
//            writer.close();
//            os.close();

//            connection.connect();
        }catch (Exception e){
            Log.e("e", e.toString());
        }
        return "WHAT DOES THIS EVEN DO?!?!?!?!?";
    }

    private String getQuery(HashMap<String, String> params) throws UnsupportedEncodingException
    {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (Map.Entry<String, String> entry : params.entrySet())
        {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }

}
