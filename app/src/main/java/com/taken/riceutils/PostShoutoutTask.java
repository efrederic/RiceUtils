package com.taken.riceutils;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by andrew on 1/17/16.
 */
public class PostShoutoutTask extends AsyncTask<String, Void, Void> {

    private String mText;
    private String mLat;
    private String mLng;

    public PostShoutoutTask(String text, String lat, String lng) {
        mText = text;
        mLat = lat;
        mLng = lng;
    }

    @Override
    protected Void doInBackground(String... urlStr) {
        try {
            URL url = new URL(urlStr[0]);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setDoOutput(true);
            try {
                OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
                out.write(("text=" + mText + "&latitude=" + mLat + "&longitude=" + mLng).getBytes());
                out.flush();
                out.close();

                // we need to read all the bytes for some reason
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                byte[] contents = new byte[1024];
                while (in.read(contents) != -1);
            }
            finally{
                urlConnection.disconnect();
            }
        } catch (Exception e) {
            Log.e("RICEUTILS", e.toString());
        }
        return null;
    }
}
