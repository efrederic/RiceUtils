package com.taken.riceutils;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class BusLocator extends AsyncTask<String, Void, String> {
	
	private InputStream inputStream = null;
	
	@Override
	protected String doInBackground(String... url){
		try{
			URL busUrl = new URL(url[0]);
			HttpURLConnection connection = (HttpURLConnection) busUrl.openConnection();
			connection.setRequestMethod("POST");
			inputStream = new BufferedInputStream(connection.getInputStream());
		}catch (Exception e){
			Log.e("e",e.toString());
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
	protected void onPostExecute(String res){
		try{
			JSONObject obj = new JSONObject(res);
            JSONArray jArray = obj.getJSONArray("d");
            
            for(Marker m: MainActivity.busMarkers){
            	m.remove();
            }
            MainActivity.busMarkers.clear();
            
            for (int i = 0; i < jArray.length(); i++) {
                JSONObject bus = jArray.getJSONObject(i);
                LatLng pos = new LatLng(bus.getDouble("Latitude"), bus.getDouble("Longitude"));
                int routeID = bus.getInt("RouteID");
                
                MarkerOptions mOptions = new MarkerOptions();
                mOptions.position(pos);

                mOptions.icon(BitmapDescriptorFactory.fromBitmap(MainActivity.busIcons.get(4)));
                for (int k = 0; k < MainActivity.routes.length(); k++) {
					JSONObject route = MainActivity.routes.getJSONObject(k);
					if(routeID == route.getInt("ID")) {
						mOptions.icon(BitmapDescriptorFactory.fromBitmap(MainActivity.busIcons.get(routeID)));
						mOptions.title(route.getString("Name"));
						break;
					}
				}
                
                Marker m = MainActivity.getMap().addMarker(mOptions);
                
                MainActivity.busMarkers.add(m);
            }
        } catch (Exception e) {
        	Log.e("e",e.toString());
        }
	}
}
