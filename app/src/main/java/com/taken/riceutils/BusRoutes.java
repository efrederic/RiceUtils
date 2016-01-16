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

public class BusRoutes extends AsyncTask<String, Void, JSONArray> {
	
	private InputStream inputStream = null;
	
	@Override
	protected JSONArray doInBackground(String... url){
		JSONArray jArray = null;
		try{
			URL busUrl = new URL(url[0]);
			HttpURLConnection connection = (HttpURLConnection) busUrl.openConnection();
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
            
            JSONObject obj = new JSONObject(sBuilder.toString());
            jArray = obj.getJSONArray("d");
            
            for (int i = 0; i < jArray.length(); i++) {
            	JSONObject route = jArray.getJSONObject(i);
            	String color = route.getString("Color");
            	Bitmap bmp = getBitmapFromURL("http://bus.rice.edu/img/icons/bus-" + color + ".png");
            	bmp = Bitmap.createScaledBitmap(bmp, 50, 50, false);
            	MainActivity.busIcons.put(route.getInt("ID"), bmp);
            }
            
		}catch(Exception e){
			//Log.e("e",e.toString());
		}
		return jArray;
	}
	
	public static Bitmap getBitmapFromURL(String link) {
	    /*--- this method downloads an Image from the given URL, 
	     *  then decodes and returns a Bitmap object
	     ---*/
	    try {
	        URL url = new URL(link);
	        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	        connection.setDoInput(true);
	        connection.connect();
	        InputStream input = connection.getInputStream();
	        return BitmapFactory.decodeStream(input);

	    } catch (IOException e) {
	        e.printStackTrace();
	        Log.e("getBmpFromUrl error: ", e.getMessage());
	        return null;
	    }
	}
}
